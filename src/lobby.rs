use std::collections::{HashMap, HashSet};

use futures::{future::select, pin_mut};
use serenity::{
    client::Context,
    model::{interactions::Interaction, user::User},
};
use tokio::sync::mpsc;
use tokio_stream::wrappers::ReceiverStream;

use crate::game::start_game;

#[derive(Clone)]
pub struct Lobby(pub mpsc::Sender<LobbyMessage>);

pub enum LobbyMessage {
    Interaction(Interaction),
    Join(User),
    Leave(User),
    Start,
}

#[derive(Default)]
struct LobbyData {
    players: HashSet<User>,
}

impl Lobby {
    pub fn new(ctx: Context) -> Self {
        let data = LobbyData::default();

        let (tx, rx) = mpsc::channel(32);

        tokio::spawn(lobby_loop(ctx, data, rx));

        Lobby(tx)
    }
}

async fn lobby_loop(ctx: Context, mut data: LobbyData, mut rx: mpsc::Receiver<LobbyMessage>) {
    'outer: while let Some(msg) = rx.recv().await {
        match msg {
            LobbyMessage::Interaction(_) => (),
            LobbyMessage::Join(u) => {
                data.players.insert(u);
            }
            LobbyMessage::Leave(u) => {
                data.players.remove(&u);
                if data.players.is_empty() {
                    println!("Deleting Lobby (no more players)");
                    break;
                }
            }
            LobbyMessage::Start => {
                let mut senders = HashMap::with_capacity(data.players.len());
                let mut recievers = Vec::with_capacity(data.players.len());

                for user in &data.players {
                    let (tx, rx) = mpsc::channel(32);
                    senders.insert(user.id, tx);
                    recievers.push((user, ReceiverStream::new(rx)));
                }

                let game = start_game(&ctx, recievers);
                pin_mut!(game);
                loop {
                    let rec = rx.recv();
                    pin_mut!(rec);
                    match select(&mut game, rec).await {
                        futures::future::Either::Left(_) => {
                            break;
                        }
                        futures::future::Either::Right((Some(LobbyMessage::Interaction(i)), _)) => {
                            if let Some(sender) = i.user.as_ref().and_then(|u| senders.get(&u.id)) {
                                let _ = sender.try_send(i);
                            }
                        }
                        futures::future::Either::Right((None, _)) => {
                            break 'outer;
                        }
                        futures::future::Either::Right(_) => (),
                    }
                }
            }
        }
    }
}
