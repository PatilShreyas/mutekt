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
package dev.shreyaspatil.mutekt.codegen.codebuild.mutableModel

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import dev.shreyaspatil.mutekt.codegen.codebuild.ClassNames
import dev.shreyaspatil.mutekt.codegen.codebuild.ext.eachToProperty
import dev.shreyaspatil.mutekt.codegen.codebuild.ext.getPublicAbstractProperties

/**
 * Builds and generates interface definition of a mutable model
 *
 * @property immutableStateInterface Immutable state interface class name
 * @property publicProperties Public properties of interface
 */
class MutableInterfaceModelBuilder(
    private val immutableStateInterface: ClassName,
    private val publicProperties: Sequence<KSPropertyDeclaration>
) {
    constructor(clazz: KSClassDeclaration) : this(clazz.toClassName(), clazz.getPublicAbstractProperties())

    private val interfaceName = "Mutable${immutableStateInterface.simpleName}"

    fun build() = TypeSpec.interfaceBuilder(interfaceName)
        .addSuperinterface(immutableStateInterface)
        .addSuperinterface(ClassNames.mutektMutableState(immutableStateInterface, thisClass()))
        .addKdoc("Mutable state model for [%L]", immutableStateInterface.simpleName)
        .addMutableStateModelFields()
        .build()

    private fun TypeSpec.Builder.addMutableStateModelFields() = apply {
        publicProperties.eachToProperty { mutable().addModifiers(KModifier.OVERRIDE) }.forEach { addProperty(it) }
    }

    private fun thisClass() = ClassName(immutableStateInterface.packageName, interfaceName)
}
