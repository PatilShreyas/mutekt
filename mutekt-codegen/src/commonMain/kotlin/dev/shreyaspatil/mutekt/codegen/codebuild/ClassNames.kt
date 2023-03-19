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
package dev.shreyaspatil.mutekt.codegen.codebuild

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import dev.shreyaspatil.mutekt.core.AtomicExecutor
import dev.shreyaspatil.mutekt.core.MutektMutableState

object ClassNames {

    fun mutektMutableState(
        immutableInterface: ClassName,
        mutableInterface: ClassName,
    ) = MutektMutableState::class.asClassName().parameterizedBy(immutableInterface, mutableInterface)

    fun stateFlowOf(clazz: ClassName) = ClassName(
        "kotlinx.coroutines.flow",
        "StateFlow",
    ).parameterizedBy(clazz)

    fun mutableStateFlowOf(property: KSPropertyDeclaration) = ClassName(
        "kotlinx.coroutines.flow",
        "MutableStateFlow",
    ).parameterizedBy(property.type.toTypeName())

    fun listOf(clazz: ClassName) = ClassName(
        "kotlin.collections",
        "List",
    ).parameterizedBy(clazz)

    fun flowCollectorOf(clazz: ClassName) = ClassName(
        "kotlinx.coroutines.flow",
        "FlowCollector",
    ).parameterizedBy(clazz)

    fun atomicExecutor() = AtomicExecutor::class.asClassName()
}
