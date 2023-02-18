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
package dev.shreyaspatil.mutekt.codegen

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class MutektCodegenProcessorTest {

    @TempDir
    lateinit var temporaryFolder: Path

    @Test
    fun shouldCompile_whenModelIsValid() {
        // Given
        val source = """
                    package dev.shreyaspatil.mutekt.example

                    import dev.shreyaspatil.mutekt.core.annotations.GenerateMutableModel

                    @GenerateMutableModel
                    interface NotesState {
                        val isLoading: Boolean
                        val notes: List<String>
                        val error: String?
                    }
        """.trimIndent()

        // When
        val kotlinSource = kotlin(name = "NotesState.kt", contents = source)
        val result = compile(kotlinSource)

        // Then
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun shouldNotCompile_whenModelHavingMutableFields() {
        // Given
        val source = """
                    package dev.shreyaspatil.mutekt.example

                    import dev.shreyaspatil.mutekt.core.annotations.GenerateMutableModel

                    @GenerateMutableModel
                    interface NotesState {
                        var isLoading: Boolean
                        val notes: List<String>
                        val error: String?
                    }
        """.trimIndent()

        // When
        val kotlinSource = kotlin(name = "NotesState.kt", contents = source)
        val result = compile(kotlinSource)

        // Then
        assert(result.messages.contains("Mutekt is unable to generate state model: NotesState because it promises to be immutable but it has mutable properties: isLoading"))
    }

    @Test
    fun shouldNotCompile_whenModelIsNotInterface() {
        // Given
        val source = """
                    package dev.shreyaspatil.mutekt.example

                    import dev.shreyaspatil.mutekt.core.annotations.GenerateMutableModel

                    @GenerateMutableModel
                    class NotesState {
                        val isLoading: Boolean = false
                        val notes: List<String> = emptyList()
                        val error: String? = null
                    }
        """.trimIndent()

        // When
        val kotlinSource = kotlin(name = "NotesState.kt", contents = source)
        val result = compile(kotlinSource)

        // Then
        assert(result.messages.contains("GenerateMutableModel can't be applied to class: must be a `interface` type"))
    }

    @Test
    fun shouldNotCompile_whenModelNotHavingPublicVisibility() {
        // Given
        val source = """
                    package dev.shreyaspatil.mutekt.example

                    import dev.shreyaspatil.mutekt.core.annotations.GenerateMutableModel

                    @GenerateMutableModel
                    internal interface NotesState {
                        var isLoading: Boolean
                        val notes: List<String>
                        val error: String?
                    }
        """.trimIndent()

        // When
        val kotlinSource = kotlin(name = "NotesState.kt", contents = source)
        val result = compile(kotlinSource)

        // Then
        assert(result.messages.contains("Mutekt is unable generate mutable model for interface: because interface visibility is not public"))
    }

    @Test
    fun shouldNotCompile_whenModelHavingZeroPublicProperties() {
        // Given
        val source = """
                    package dev.shreyaspatil.mutekt.example

                    import dev.shreyaspatil.mutekt.core.annotations.GenerateMutableModel

                    @GenerateMutableModel
                    interface NotesState {
                    }
        """.trimIndent()

        // When
        val kotlinSource = kotlin(name = "NotesState.kt", contents = source)
        val result = compile(kotlinSource)

        // Then
        assert(result.messages.contains("Mutekt will not generate mutable model for NotesState: because there are no public members declared."))
    }

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation = KotlinCompilation().apply {
        workingDir = temporaryFolder.toFile()
        inheritClassPath = true
        symbolProcessorProviders = listOf(MutektCodegenProcessorProvider())
        sources = sourceFiles.asList()
        verbose = false
        kspWithCompilation = true
    }

    private fun compile(vararg sourceFiles: SourceFile): KotlinCompilation.Result =
        prepareCompilation(*sourceFiles).compile()
}
