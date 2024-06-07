package club.aetherium.gradle.mixin.mixinextra.annotations.method

import net.fabricmc.tinyremapper.extension.mixin.common.data.AnnotationElement
import net.fabricmc.tinyremapper.extension.mixin.common.data.Constant
import org.objectweb.asm.AnnotationVisitor
import club.aetherium.gradle.mixin.ArrayVisitorWrapper
import club.aetherium.gradle.mixin.mixin.refmap.RefmapBuilderClassVisitor
import club.aetherium.gradle.mixin.mixin.refmap.annotations.AtAnnotationVisitor
import club.aetherium.gradle.mixin.mixin.refmap.annotations.SliceAnnotationVisitor
import club.aetherium.gradle.mixin.mixin.refmap.annotations.method.AbstractMethodAnnotationVisitor
import club.aetherium.gradle.mixin.mixinextra.MixinExtra

@Suppress("UNUSED_PARAMETER")
open class ModifyExpressionValueAnnotationVisitor(
    descriptor: String,
    visible: Boolean,
    parent: AnnotationVisitor,
    methodAccess: Int,
    methodName: String,
    methodDescriptor: String,
    methodSignature: String?,
    methodExceptions: Array<out String>?,
    refmapBuilder: RefmapBuilderClassVisitor,
)  : AbstractMethodAnnotationVisitor(
    descriptor,
    visible,
    parent,
    methodAccess,
    methodName,
    methodDescriptor,
    methodSignature,
    methodExceptions,
    refmapBuilder
) {

    override val annotationName: String = "@ModifyExpressionValue"

    companion object {
        fun shouldVisit(
            descriptor: String,
            visible: Boolean,
            methodAccess: Int,
            methodName: String,
            methodDescriptor: String,
            methodSignature: String?,
            methodExceptions: Array<out String>?,
            refmapBuilder: RefmapBuilderClassVisitor
        ): Boolean {
            return descriptor == MixinExtra.Annotation.MODIFY_EXPRESSION_VALUE
        }
    }

    override fun visitArray(name: String): AnnotationVisitor {
        return when (name) {
            AnnotationElement.AT -> {
                ArrayVisitorWrapper(Constant.ASM_VERSION, super.visitArray(name)) { AtAnnotationVisitor(it, remap, refmapBuilder) }
            }

            AnnotationElement.SLICE -> {
                ArrayVisitorWrapper(Constant.ASM_VERSION, super.visitArray(name)) { SliceAnnotationVisitor(it, remap, refmapBuilder) }
            }
            AnnotationElement.METHOD -> {
                object: AnnotationVisitor(Constant.ASM_VERSION, if (noRefmap) null else super.visitArray(name)) {
                    override fun visit(name: String?, value: Any) {
                        super.visit(name, value)
                        targetNames.add(value as String)
                    }
                }
            }
            else -> {
                super.visitArray(name)
            }
        }
    }

    override fun visitEnd() {
        val method = if (noRefmap) {
            super.visitArray(AnnotationElement.METHOD)
        } else {
            null
        }
        remapTargetNames { mappedName ->
            method?.visit(null, mappedName)
        }
        method?.visitEnd()
        super.visitEnd()
    }



}
