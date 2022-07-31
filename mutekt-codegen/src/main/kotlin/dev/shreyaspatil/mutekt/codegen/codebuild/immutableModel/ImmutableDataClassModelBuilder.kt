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
package dev.shreyaspatil.mutekt.codegen.codebuild.immutableModel

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import dev.shreyaspatil.mutekt.codegen.codebuild.ext.eachToParameter
import dev.shreyaspatil.mutekt.codegen.codebuild.ext.eachToProperty
import dev.shreyaspatil.mutekt.codegen.codebuild.ext.getPublicAbstractProperties

/**
 * Generates private immutable data class for a state model.
 */
class ImmutableDataClassModelBuilder(
    private val immutableStateInterface: ClassName,
    private val publicProperties: Sequence<KSPropertyDeclaration>
) {
    constructor(clazz: KSClassDeclaration) : this(clazz.toClassName(), clazz.getPublicAbstractProperties())

    private val className = "Immutable${immutableStateInterface.simpleName}"

    fun build() = TypeSpec.classBuilder(className)
        .addSuperinterface(immutableStateInterface)
        .addModifiers(KModifier.PRIVATE, KModifier.DATA)
        .primaryConstructor()
        .overrideFields()
        .build()

    private fun TypeSpec.Builder.primaryConstructor() = apply {
        primaryConstructor(
            FunSpec.constructorBuilder().apply {
                publicProperties.eachToParameter().forEach { addParameter(it) }
            }.build()
        )
    }

    private fun TypeSpec.Builder.overrideFields() = apply {
        publicProperties.eachToProperty {
            addModifiers(KModifier.OVERRIDE).initializer(it.simpleName.asString())
        }.forEach {
            addProperty(it)
        }
    }
}
