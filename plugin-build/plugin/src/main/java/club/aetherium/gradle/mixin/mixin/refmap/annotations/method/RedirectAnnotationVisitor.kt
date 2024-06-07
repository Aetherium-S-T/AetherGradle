package club.aetherium.gradle.mixin.mixin.refmap.annotations.method

import net.fabricmc.tinyremapper.extension.mixin.common.data.Annotation
import org.objectweb.asm.AnnotationVisitor
import club.aetherium.gradle.mixin.mixin.refmap.RefmapBuilderClassVisitor

@Suppress("UNUSED_PARAMETER")
class RedirectAnnotationVisitor(
    descriptor: String,
    visible: Boolean,
    parent: AnnotationVisitor,
    methodAccess: Int,
    methodName: String,
    methodDescriptor: String,
    methodSignature: String?,
    methodExceptions: Array<out String>?,
    refmapBuilder: RefmapBuilderClassVisitor,
)  : ModifyArgAnnotationVisitor(
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

    override val annotationName: String = "@Redirect"

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
            return descriptor == Annotation.REDIRECT
        }

    }

}
