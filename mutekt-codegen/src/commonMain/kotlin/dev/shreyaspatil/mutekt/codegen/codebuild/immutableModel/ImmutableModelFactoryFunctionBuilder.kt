package dev.shreyaspatil.mutekt.codegen.codebuild.immutableModel

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import dev.shreyaspatil.mutekt.codegen.codebuild.ext.eachToParameter

/**
 * Creates a factory function to create an instance of immutable state model
 */
class ImmutableModelFactoryFunctionBuilder(
    private val immutableModelInterfaceName: ClassName,
    private val immutableModelImplClassName: ClassName,
    private val publicProperties: Sequence<KSPropertyDeclaration>
) {
    // Function name should be same as interface name
    private val functionName = immutableModelInterfaceName.simpleName

    fun build() = FunSpec.builder(functionName)
        .apply {
            addKdoc(
                CodeBlock.builder()
                    .addStatement("Creates an instance of state model [%T]", immutableModelInterfaceName)
                    .apply {
                        publicProperties
                            .mapNotNull { prop -> prop.docString?.let { doc -> prop.simpleName.asString() to doc } }
                            .forEach { (param, doc) -> addStatement("@param %L %L", param, doc) }
                    }.build()
            )
        }
        .addModifiers(KModifier.PUBLIC)
        .addParameters(publicProperties.eachToParameter().toList())
        .returns(immutableModelInterfaceName)
        .addStatement(
            "return %L(%L)",
            immutableModelImplClassName.simpleName,
            publicProperties.joinToString { it.simpleName.asString() }
        ).build()
}
