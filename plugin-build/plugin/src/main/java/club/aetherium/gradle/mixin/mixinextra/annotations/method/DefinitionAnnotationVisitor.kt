package club.aetherium.gradle.mixin.mixinextra.annotations.method

import net.fabricmc.tinyremapper.extension.mixin.common.data.AnnotationElement
import net.fabricmc.tinyremapper.extension.mixin.common.data.Constant
import org.objectweb.asm.AnnotationVisitor
import club.aetherium.gradle.mixin.ArrayVisitorWrapper
import club.aetherium.gradle.mixin.mixin.refmap.RefmapBuilderClassVisitor
import club.aetherium.gradle.mixin.mixin.refmap.annotations.AtAnnotationVisitor
import club.aetherium.gradle.mixin.mixin.refmap.annotations.method.AbstractMethodAnnotationVisitor
import club.aetherium.gradle.mixin.mixinextra.MixinExtra

class DefinitionAnnotationVisitor(
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

    override val annotationName: String = "@Definition"

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
            return descriptor == MixinExtra.Annotation.DEFINITION
        }
    }

    override fun visitArray(name: String): AnnotationVisitor {
        return when (name) {
            AnnotationElement.AT -> {
                ArrayVisitorWrapper(Constant.ASM_VERSION, super.visitArray(name)) { AtAnnotationVisitor(it, remap, refmapBuilder) }
            }
            else -> {
                super.visitArray(name)
            }
        }
    }

}
