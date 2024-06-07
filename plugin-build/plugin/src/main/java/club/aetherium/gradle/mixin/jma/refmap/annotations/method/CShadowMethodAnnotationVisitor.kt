package club.aetherium.gradle.mixin.jma.refmap.annotations.method

import net.fabricmc.tinyremapper.extension.mixin.common.data.AnnotationElement
import org.objectweb.asm.AnnotationVisitor
import club.aetherium.gradle.mixin.jma.JarModAgent
import club.aetherium.gradle.mixin.mixin.refmap.RefmapBuilderClassVisitor

@Suppress("UNUSED_PARAMETER")
class CShadowMethodAnnotationVisitor(
    descriptor: String,
    visible: Boolean,
    parent: AnnotationVisitor,
    methodAccess: Int,
    methodName: String,
    methodDescriptor: String,
    methodSignature: String?,
    methodExceptions: Array<out String>?,
    refmapBuilder: RefmapBuilderClassVisitor,
)  : CAbstractMethodAnnotationVisitor(
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

    override val annotationName: String = "@CShadow"

    companion object{
        fun shouldVisit(
            descriptor:String,
            visible:Boolean,
            methodAccess:Int,
            methodName:String,
            methodDescriptor:String,
            methodSignature:String?,
            methodExceptions:Array<out String>?,
            refmapBuilder:RefmapBuilderClassVisitor
        ): Boolean{
            return descriptor == JarModAgent.Annotation.CINJECT
        }
    }

    override fun visit(name: String?, value: Any) {
        if (name == AnnotationElement.VALUE) {
            targetNames.add(value as String)
            if (!noRefmap) super.visit(name, value)
        } else {
            super.visit(name, value)
        }
    }

    override fun getTargetNameAndDescs(targetMethod: String, wildcard: Boolean): Pair<String, Set<String?>> {
        return if (targetMethod.contains("(")) {
            targetMethod.substringBefore("(") to setOf("(" + targetMethod.substringAfter("("))
        } else {
            targetMethod to setOf(methodDescriptor)
        }
    }

    override fun visitEnd() {
        remapTargetNames {
            if (noRefmap) {
                super.visit(AnnotationElement.VALUE, it)
            }
        }
        super.visitEnd()
    }

}
