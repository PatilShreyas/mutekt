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

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import dev.shreyaspatil.mutekt.codegen.codebuild.ModelClassFileBuilder
import dev.shreyaspatil.mutekt.codegen.codebuild.ext.getAllPublicProperties
import dev.shreyaspatil.mutekt.codegen.logging.ErrorMessages
import dev.shreyaspatil.mutekt.core.annotations.GenerateMutableModel

class MutektCodegenProcessor(
    environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val stateDeclarations = getAllModelsAnnotatedWithGenerateMutableModel(resolver)

        // Exit from the processor in case nothing is annotated with @Function.
        if (!stateDeclarations.iterator().hasNext()) return emptyList()

        for (state in stateDeclarations) {
            if (!processStateModel(state)) continue
        }

        return emptyList()
    }

    private fun processStateModel(state: KSClassDeclaration): Boolean {
        logger.info("Processing ${state.simpleName.asString()}", state)

        // Return error if annotation is applied on other than interface.
        if (state.classKind != ClassKind.INTERFACE) {
            logger.error(ErrorMessages.annotationCanNotBeAppliedToTypeError(state.classKind.type), state)
            return false
        }

        // Return error if interface on which annotation applied has not having visibility as public.
        if (!state.isPublic()) {
            logger.error(ErrorMessages.annotationAppliedOnNonPublicInterface(state.classKind.type), state)
            return false
        }

        // Make sure there are public members in the definition of model. If there are zero public members, do not
        // generate model for such class.
        val allPublicProperties = state.getAllPublicProperties().toList()
        if (allPublicProperties.isEmpty()) {
            logger.warn(ErrorMessages.canNotGenerateModelDueToZeroFields(state.simpleName.asString()), state)
            return false
        }

        // Immutable model should not have any mutable field. If existed, return an error.
        val mutableProperties = allPublicProperties.filter { it.isMutable }
        if (mutableProperties.isNotEmpty()) {
            logger.error(
                ErrorMessages.hasMutableFields(
                    state.simpleName.asString(),
                    mutableProperties.map { it.simpleName.asString() }
                ),
                state
            )
            return false
        }

        val mutableModelSpec = buildMutableModelSpec(state)
        mutableModelSpec.writeTo(codeGenerator, dependencies = Dependencies(true, state.containingFile!!))

        return true
    }

    private fun buildMutableModelSpec(state: KSClassDeclaration): FileSpec {
        return ModelClassFileBuilder(state).build()
    }

    private fun getAllModelsAnnotatedWithGenerateMutableModel(resolver: Resolver): Sequence<KSClassDeclaration> =
        resolver.getSymbolsWithAnnotation(GenerateMutableModel::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
}
