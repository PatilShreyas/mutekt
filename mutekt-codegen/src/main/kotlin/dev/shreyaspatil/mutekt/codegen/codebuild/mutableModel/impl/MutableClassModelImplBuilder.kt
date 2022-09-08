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
package dev.shreyaspatil.mutekt.codegen.codebuild.mutableModel.impl

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.withIndent
import dev.shreyaspatil.mutekt.codegen.codebuild.ClassNames
import dev.shreyaspatil.mutekt.codegen.codebuild.MemberNames
import dev.shreyaspatil.mutekt.codegen.codebuild.ext.eachToParameter

/**
 * Generates a mutable model class implementation
 */
class MutableClassModelImplBuilder(
    private val immutableStateInterface: ClassName,
    private val publicProperties: Sequence<KSPropertyDeclaration>,
    private val mutableModelInterfaceName: ClassName,
    private val immutableDataClassName: ClassName
) {
    private val className = "Mutable${immutableStateInterface.simpleName}Impl"

    fun build() = TypeSpec.classBuilder(className)
        .addModifiers(KModifier.PRIVATE)
        .addSuperinterface(mutableModelInterfaceName)
        .primaryConstructor()
        .privateAtomicExecutor()
        .addPrivateStateFlowAndGetterSetterFields()
        .immutableStateFlowImpl()
        .overrideFunAsStateFlow()
        .overrideFunUpdate()
        .build()

    private fun TypeSpec.Builder.primaryConstructor() = apply {
        primaryConstructor(
            FunSpec
                .constructorBuilder()
                .addParameters(publicProperties.eachToParameter().toList())
                .build()
        )
    }

    private fun TypeSpec.Builder.privateAtomicExecutor() = apply {
        addProperty(
            PropertySpec.builder("_atomicExecutor", ClassNames.atomicExecutor())
                .initializer("%T()", ClassNames.atomicExecutor())
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
    }

    /**
     * Creates MutableStateFlow property from [property]
     */
    private fun privateStateFlowField(property: KSPropertyDeclaration): PropertySpec {
        val stateFlowPropertyName = "_${property.simpleName.asString()}"
        return PropertySpec.builder(stateFlowPropertyName, ClassNames.mutableStateFlowOf(property))
            .initializer("MutableStateFlow(${property.simpleName.asString()})")
            .addModifiers(KModifier.PRIVATE)
            .build()
    }

    private fun TypeSpec.Builder.addPrivateStateFlowAndGetterSetterFields() = apply {
        publicProperties.forEach {
            val stateFlowProperty = privateStateFlowField(it)
            val overriddenField = overrideMutableField(stateFlowProperty.name, it)

            addProperty(stateFlowProperty)
            addProperty(overriddenField)
        }
    }

    /**
     * Overrides mutable field which delegates getter and setter to MutableStateFlow fields
     */
    private fun overrideMutableField(stateFlowDelegateProperty: String, property: KSPropertyDeclaration): PropertySpec {
        val type = property.type.toTypeName()
        return PropertySpec.builder(property.simpleName.asString(), type)
            .mutable()
            .addModifiers(KModifier.OVERRIDE)
            .getter(FunSpec.getterBuilder().addStatement("return %L.value", stateFlowDelegateProperty).build())
            .setter(
                FunSpec.setterBuilder()
                    .addParameter("value", type)
                    .addStatement("%L.value = %L", stateFlowDelegateProperty, "value")
                    .build()
            ).build()
    }

    private fun TypeSpec.Builder.immutableStateFlowImpl() = apply {
        val stateFlowClass = ClassNames.stateFlowOf(immutableStateInterface)
        addProperty(
            PropertySpec.builder("_immutableStateFlowImpl", stateFlowClass)
                .addModifiers(KModifier.PRIVATE)
                .initializer(
                    StateFlowImplBuilder(
                        immutableStateInterface,
                        publicProperties,
                        immutableDataClassName
                    ).build()
                ).build()
        )
    }

    private fun TypeSpec.Builder.overrideFunAsStateFlow() = apply {
        addFunction(
            FunSpec.builder("asStateFlow")
                .addModifiers(KModifier.OVERRIDE)
                .returns(ClassNames.stateFlowOf(immutableStateInterface))
                .addStatement("return _immutableStateFlowImpl")
                .build()
        )
    }

    private fun TypeSpec.Builder.overrideFunUpdate() = apply {
        addFunction(
            FunSpec.builder("update")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    ParameterSpec.builder(
                        name = "mutate",
                        type = LambdaTypeName.get(
                            receiver = mutableModelInterfaceName,
                            returnType = Unit::class.asClassName()
                        )
                    ).build()
                )
                .addStatement("_atomicExecutor.execute { mutate() }")
                .build()
        )
    }
}

/**
 * Generates anonymous implementation of StateFlow
 */
class StateFlowImplBuilder(
    private val type: ClassName,
    private val publicProperties: Sequence<KSPropertyDeclaration>,
    private val immutableDataClassName: ClassName
) {
    fun build() = buildCodeBlock {
        addStatement(
            "%L",
            TypeSpec.anonymousClassBuilder()
                .addSuperinterface(ClassNames.stateFlowOf(type))
                .overrideReplayCache()
                .overrideValue()
                .overrideCollect()
                .build()
        )
    }

    private fun TypeSpec.Builder.overrideReplayCache() = apply {
        addProperty(
            PropertySpec.builder("replayCache", ClassNames.listOf(type))
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement("return listOf(value)")
                        .build()
                )
                .build()
        )
    }

    private fun TypeSpec.Builder.overrideValue() = apply {
        addProperty(
            PropertySpec.builder("value", type, KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement(
                            "return ${immutableDataClassName.simpleName}(%L)",
                            buildCodeBlock {
                                addStatement("")
                                withIndent {
                                    publicProperties
                                        .map { it.simpleName.asString() }
                                        .forEach { field ->
                                            addStatement("%L = _%L.value,", field, field)
                                        }
                                }
                            }
                        )
                        .build()
                ).build()
        )
    }

    private fun TypeSpec.Builder.overrideCollect() = apply {
        addFunction(
            FunSpec
                .builder("collect")
                .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
                .addParameter("collector", ClassNames.flowCollectorOf(type))
                .returns(NOTHING)
                .addStatement(
                    "return %L",
                    buildCodeBlock {
                        beginControlFlow("%M", MemberNames.COROUTINE_SCOPE)
                        withIndent {
                            beginControlFlow(
                                "%M(_atomicExecutor.executing, %L) { params ->",
                                MemberNames.COMBINE,
                                publicProperties.joinToString { "_${it.simpleName.asString()}" }
                            )

                            withIndent {
                                addStatement("val isUpdating = params[0] as Boolean")

                                beginControlFlow("if (!isUpdating)")
                                addStatement("value")
                                endControlFlow()

                                beginControlFlow("else")
                                addStatement("null")
                                endControlFlow()
                            }
                            endControlFlow()
                            addStatement(".%M()", MemberNames.FILTER_NOT_NULL)
                            addStatement(".%M(this)", MemberNames.STATE_IN)
                            addStatement(".collect(collector)")
                        }
                        endControlFlow()
                    }
                ).build()
        )
    }
}
