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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AtomicExecutorTest {
    private lateinit var executor: AtomicExecutor

    @BeforeTest
    fun setUp() {
        executor = AtomicExecutor()
    }

    @Test
    fun test_execute() = runTest {
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
    fun test_execute_onError_stateShouldBeUpdated() = runTest {
        // When: Error occurs while executing with AtomicExecutor
        runCatching { executor.execute { error("") } }

        // Then: The state of `executing` should be reset to false
        assertFalse(executor.executing.value)
    }

    @Test
    fun test_executing() = runTest {
        // Given: A lock for holding execution in executor
        var isLocked = true
        var isExecutionStarted = false

        // When: Executor is executing task
        val job = GlobalScope.launch {
            executor.execute {
                isExecutionStarted = true
                while (true) {
                    if (!isLocked) {
                        break
                    }
                }
            }
        }

        // Wait for executor to execute job
        awaitUntil { !isExecutionStarted }

        // Then: While task is being executed, state should be true
        assertTrue(executor.executing.value)

        // Then: On task finish (unlocking lock), state should be false
        isLocked = false
        job.join()
        assertFalse(executor.executing.value)
    }

    private fun awaitUntil(predicate: () -> Boolean) {
        while (predicate()) {}
    }
}
