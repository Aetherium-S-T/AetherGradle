package club.aetherium.gradle.mixin.jma.refmap.annotations.field

import net.fabricmc.tinyremapper.extension.mixin.common.data.AnnotationElement
import org.objectweb.asm.AnnotationVisitor
import club.aetherium.gradle.mixin.jma.JarModAgent
import club.aetherium.gradle.mixin.mixin.refmap.RefmapBuilderClassVisitor

class CShadowFieldAnnotationVisitor(
    descriptor: String,
    visible: Boolean,
    parent: AnnotationVisitor,
    fieldAccess: Int,
    fieldName: String,
    fieldDescriptor: String,
    fieldSignature: String?,
    fieldValue: Any?,
    refmapBuilder: RefmapBuilderClassVisitor
) : CAbstractFieldAnnotationVisitor(
    descriptor,
    visible,
    parent,
    fieldAccess,
    fieldName,
    fieldDescriptor,
    fieldSignature,
    fieldValue,
    refmapBuilder
) {

    override val annotationName: String = "@CShadow"

    companion object{
        fun shouldVisit(
            descriptor: String,
            visible: Boolean,
            fieldAccess: Int,
            fieldName: String,
            fieldDescriptor: String,
            fieldSignature: String?,
            fieldValue: Any?,
            refmapBuilder: RefmapBuilderClassVisitor
        ): Boolean{
            return descriptor == JarModAgent.Annotation.CSHADOW
        }
    }

    override fun visit(name: String?, value: Any) {
        if (name == AnnotationElement.VALUE) {
            targetNames.add(value as String)
            if (!noRefmap) {
                super.visit(name, value)
            }
        } else {
            super.visit(name, value)
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

    override fun getTargetNameAndDescs(targetField: String): Pair<String, Set<String?>> {
        return if (targetField.contains(":")) {
            targetField.substringBefore(":") to setOf(targetField.substringAfter(":"))
        } else {
            targetField to setOf(fieldDescriptor)
        }
    }



}
