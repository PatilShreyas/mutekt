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
                    }.build()
            )
        }
        .addModifiers(KModifier.PUBLIC)
        .receiver(immutableModelInterfaceName)
        .returns(mutableModelInterfaceName)
        .addStatement(
            "return %L(%L)",
            mutableModelFunctionName,
            publicProperties.joinToString { it.simpleName.asString() }
        ).build()
}