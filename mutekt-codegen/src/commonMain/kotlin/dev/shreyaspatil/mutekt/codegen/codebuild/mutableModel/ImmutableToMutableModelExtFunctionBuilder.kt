/**
 * Copyright 2023 Shreyas Patil
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
package dev.shreyaspatil.mutekt.codegen.codebuild.mutableModel

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

/**
 * Creates an extension function to transform immutable state model instance
 * into mutable instance.
 */
class ImmutableToMutableModelExtFunctionBuilder(
    private val immutableModelInterfaceName: ClassName,
    private val mutableModelInterfaceName: ClassName,
    private val mutableModelFunctionName: String,
    private val publicProperties: Sequence<KSPropertyDeclaration>,
) {
    // Extension-function name
    private val functionName = "mutable"

    fun build() = FunSpec.builder(functionName)
        .apply {
            addKdoc(
                CodeBlock.builder()
                    .addStatement("Creates a mutable [%T] instance from this state model", immutableModelInterfaceName)
                    .apply {
                        publicProperties
                            .mapNotNull { prop -> prop.docString?.let { doc -> prop.simpleName.asString() to doc } }
                            .forEach { (param, doc) -> addStatement("@param %L %L", param, doc) }
                    }.build(),
            )
        }
        .addModifiers(KModifier.PUBLIC)
        .receiver(immutableModelInterfaceName)
        .returns(mutableModelInterfaceName)
        .addStatement(
            "return %L(%L)",
            mutableModelFunctionName,
            publicProperties.joinToString { it.simpleName.asString() },
        ).build()
}
