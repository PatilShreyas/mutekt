/**
 * Copyright 2022 Shreyas Patil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.shreyaspatil.mutekt.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AtomicExecutorTest {
    private lateinit var executor: AtomicExecutor

    @BeforeEach
    fun setUp() {
        executor = AtomicExecutor()
    }

    @Test
    fun test_execute() = runBlocking {
        // Given: A sample variable to hold a value
        var counter = 0

        // When: Multiple jobs to be executed in AtomicExecutor
        withContext(Dispatchers.Default) { // Executing this in Default dispatcher for parallelism
            repeat(100) {
                launch {
                    repeat(1000) {
                        executor.execute { counter++ }
                    }
                }
            }
        }

        // Then: Count should be equal to number of executions
        assertEquals(100000, counter)
    }

    @Test
    fun test_execute_onError_stateShouldBeUpdated() = runBlocking {
        // When: Error occurs while executing with AtomicExecutor
        runCatching { executor.execute { error("") } }

        // Then: The state of `executing` should be reset to false
        assertFalse(executor.executing.value)
    }

    @Test
    fun test_executing() = runBlocking {
        // Given: A lock for holding execution in executor
        val mutex = Mutex(locked = true)

        // When: Executor is executing task
        val job = launch {
            executor.execute {
                runBlocking { mutex.lock() }
            }
        }
        // Wait for executor to execute job
        yield()

        // Then: While task is being executed, state should be true
        assertTrue(executor.executing.value)

        // Then: On task finish (unlocking lock), state should be false
        mutex.unlock()
        job.join()
        assertFalse(executor.executing.value)
    }
}
