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

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import dev.shreyaspatil.mutekt.codegen.codebuild.ext.getPublicAbstractProperties
import dev.shreyaspatil.mutekt.codegen.codebuild.immutableModel.ImmutableDataClassModelBuilder
import dev.shreyaspatil.mutekt.codegen.codebuild.mutableModel.MutableInterfaceModelBuilder
import dev.shreyaspatil.mutekt.codegen.codebuild.mutableModel.impl.MutableClassModelImplBuilder
import dev.shreyaspatil.mutekt.codegen.codebuild.mutableModel.impl.MutableModelFactoryFunctionBuilder

/**
 * Builds a class containing required interfaces and implementations for Mutekt.
 */
class ModelClassFileBuilder(private val state: KSClassDeclaration) {
    private val packageName = state.packageName.asString()
    private val generatedClassFilename = "${state.simpleName.asString()}_Generated"

    fun build(): FileSpec {
        val mutableInterfaceSpec = MutableInterfaceModelBuilder(state).build()
        val immutableDataClassSpec = ImmutableDataClassModelBuilder(state).build()

        val mutableModelClassImplSpec = MutableClassModelImplBuilder(
            immutableStateInterface = state.toClassName(),
            mutableModelInterfaceName = mutableInterfaceSpec.toClassName(),
            immutableDataClassName = immutableDataClassSpec.toClassName(),
            publicProperties = state.getPublicAbstractProperties()
        ).build()

        val mutableModelFactoryFunSpec = MutableModelFactoryFunctionBuilder(
            mutableInterfaceName = mutableInterfaceSpec.toClassName(),
            mutableImplClassName = mutableModelClassImplSpec.toClassName(),
            publicProperties = state.getPublicAbstractProperties()
        ).build()

        return FileSpec.builder(
            packageName = state.packageName.asString(),
            fileName = generatedClassFilename
        ).addFileComment("This is auto-generated file by Mutekt(https://github.com/PatilShreyas/mutekt)")
            .addType(mutableInterfaceSpec)
            .addType(immutableDataClassSpec)
            .addType(mutableModelClassImplSpec)
            .addFunction(mutableModelFactoryFunSpec)
            .build()
    }

    private fun TypeSpec.toClassName() = ClassName(packageName, name!!)
}
