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
package dev.shreyaspatil.mutekt.codegen.codebuild.ext

import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Transforms each [KSPropertyDeclaration].
 *
 * @param name Transformed name of a property
 * @param type Transformed type of property
 * @param transform Builder to transform a property
 */
fun Sequence<KSPropertyDeclaration>.eachToProperty(
    name: (KSPropertyDeclaration) -> String = { it.simpleName.asString() },
    type: (KSPropertyDeclaration) -> TypeName = { it.type.toTypeName() },
    transform: PropertySpec.Builder.(KSPropertyDeclaration) -> Unit = {}
) = map { PropertySpec.builder(name(it), type(it)).apply { transform(this, it) }.build() }

/**
 * Transforms each [KSPropertyDeclaration] to [ParameterSpec]
 *
 * @param name Transformed name of a parameter
 * @param type Transformed type of parameter
 * @param transform Builder to transform a parameter
 */
fun Sequence<KSPropertyDeclaration>.eachToParameter(
    name: (KSPropertyDeclaration) -> String = { it.simpleName.asString() },
    type: (KSPropertyDeclaration) -> TypeName = { it.type.toTypeName() },
    transform: ParameterSpec.Builder.(KSPropertyDeclaration) -> Unit = {}
): Sequence<ParameterSpec> = map { ParameterSpec.builder(name(it), type(it)).apply { transform(this, it) }.build() }

/**
 * Returns all public abstract properties of this class declaration.
 */
fun KSClassDeclaration.getPublicAbstractProperties() = getAllProperties().filter { it.isPublic() && it.isAbstract() }
