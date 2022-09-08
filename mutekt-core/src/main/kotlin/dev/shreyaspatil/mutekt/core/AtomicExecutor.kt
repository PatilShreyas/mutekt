package dev.shreyaspatil.mutekt.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Executor which executes operation atomically.
 */
class AtomicExecutor {
    private val _executing = MutableStateFlow(false)
    val executing = _executing.asStateFlow()

    /**
     * Executes the [block] atomically.
     * If already any operation is executing, it'll wait till its completion.
     */
    fun execute(block: () -> Unit) {
        while (true) {
            if (_executing.compareAndSet(expect = false, update = true)) {
                try {
                    block()
                } finally {
                    _executing.update { false }
                }
                return
            }
        }
    }
}