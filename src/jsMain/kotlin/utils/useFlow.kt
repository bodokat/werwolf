package utils

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import react.useEffectOnce
import react.useState

fun <T> useFlow(flow: StateFlow<T>): T {
    val (state, setState) = useState(flow.value)

    useEffectOnce {
        val job = flow.onEach { setState(it) }.launchIn(MainScope())
        cleanup { job.cancel() }
    }

    return state
}