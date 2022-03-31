package game

import kotlinx.coroutines.launch
import lobby.ToServerMessage
import mui.material.*
import react.FC
import react.Props
import react.createElement
import react.useContext

val Settings = FC<Props> {
    val state = useContext(gameContext)
    val session = useContext(sessionContext)
    val isAdmin = state.settings.admin == state.me

    Button {
        disabled = !isAdmin || state.started
        +"Start"
        onClick = {
            scope.launch {
                session.send(
                    ToServerMessage.AdminMessage.Start
                )
            }
        }
    }
    List {
        state.settings.availableRoles.forEachIndexed { index, role ->
            ListItem {
                +role
                secondaryAction = createElement<Props> {
                    if(isAdmin){
                        IconButton {
                            attrs {
                                edge = IconButtonEdge.end
                                onClick = {
                                    scope.launch {
                                        session.send(ToServerMessage.AdminMessage.ChangeRoles(
                                            state.settings.roles.mapIndexed { roleIndex, amount -> if (roleIndex == index) amount + 1 else amount }
                                        ))
                                    }
                                }
                            }
                            mui.icons.material.Add()
                        }
                    }
                    IconButton {
                        attrs {
                            edge = IconButtonEdge.end
                        }
                        +(state.settings.roles[index].toString())
                    }
                    if(isAdmin){
                        IconButton {
                            attrs {
                                edge = IconButtonEdge.end
                                onClick = {
                                    scope.launch {
                                        session.send(ToServerMessage.AdminMessage.ChangeRoles(
                                            state.settings.roles.mapIndexed { roleIndex, amount -> if (roleIndex == index) amount - 1 else amount }
                                        ))
                                    }
                                }
                            }
                            mui.icons.material.Remove()
                        }
                    }

                }
            }
        }
    }
}