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
package dev.shreyaspatil.mutekt.codegen.logging

import dev.shreyaspatil.mutekt.core.annotations.GenerateMutableModel

object ErrorMessages {
    fun annotationCanNotBeAppliedToTypeError(type: String): String {
        return "${GenerateMutableModel::class.simpleName} can't be applied to $type: must be a `interface` type"
    }

    fun hasMutableFields(modelClass: String, mutableProperties: List<String>): String {
        return "Mutekt is unable to generate state model: $modelClass because it promises to be immutable but it has mutable properties: ${mutableProperties.joinToString()}"
    }

    fun canNotGenerateModelDueToZeroFields(modelClass: String): String {
        return "Mutekt will not generate mutable model for $modelClass: because there are no public members declared."
    }

    fun annotationAppliedOnNonPublicInterface(modelClass: String): String {
        return "Mutekt is unable generate mutable model for $modelClass: because interface visibility is not public"
    }
}
