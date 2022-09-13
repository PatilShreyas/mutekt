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
package dev.shreyaspatil.mutekt.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

fun main() = runBlocking { notesUi(NotesViewModel(this)) }

/**
 * Imagine this function as a UI layer which uses [NotesViewModel] for handling state of a UI.
 */
suspend fun notesUi(viewModel: NotesViewModel) = coroutineScope {
    val notesState = viewModel.state

    // Observe UI state changes
    launch {
        notesState.collect { state ->
            // Render state of UI
            println("New State: $state")
        }
    }

    // Perform UI operations: Imagine user clicks "Get notes" button
    viewModel.loadNotes()

    // Get to know about current state of a UI
    println("Current state of UI = ${notesState.value}")
}

/**
 * Example ViewModel implementation for State management
 */
class NotesViewModel(private val viewModelScope: CoroutineScope) {

    /**
     * Instance of mutable model [MutableNotesState] which is generated with mutekt.
     */
    private val _state = MutableNotesState(isLoading = false, notes = emptyList(), error = null)

    /**
     * Immutable StateFlow of a [NotesState].
     * Whenever any property of [_state] is changed, this StateFlow is updated with new value of immutable model.
     */
    val state: StateFlow<NotesState> = _state.asStateFlow()

    fun loadNotes() {
        viewModelScope.launch {
            _state.isLoading = true

            try {
                val allNotes = getNotes()
                setState {
                    isLoading = false
                    notes = allNotes
                }
            } catch (e: Throwable) {
                setState {
                    isLoading = false
                    error = e.message ?: "Error occurred"
                }
            }
        }
    }

    /**
     * Mutates state atomically
     */
    private fun setState(mutate: MutableNotesState.() -> Unit) = _state.update(mutate)

    /**
     * Randomly either returns notes or fails with Exception.
     */
    private suspend fun getNotes(): List<String> {
        delay(2000)
        return if (Random.nextInt(100) % 2 == 0) listOf("Lorem", "Ipsum") else error("Failed to retrieve notes")
    }
}
