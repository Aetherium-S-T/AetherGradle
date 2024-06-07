package club.aetherium.gradle.mixin.jma.hard.annotations.method

import net.fabricmc.tinyremapper.extension.mixin.common.ResolveUtility
import net.fabricmc.tinyremapper.extension.mixin.common.data.AnnotationElement
import org.objectweb.asm.AnnotationVisitor
import club.aetherium.gradle.mixin.jma.JarModAgent
import club.aetherium.gradle.mixin.jma.dontRemap
import club.aetherium.gradle.mixin.mixin.hard.HardTargetRemappingClassVisitor
import club.aetherium.gradle.mixin.mixin.hard.annotations.method.AbstractMethodAnnotationVisitor
import java.util.concurrent.atomic.AtomicBoolean

class CShadowMethodAnnotationVisitor(
    descriptor: String,
    visible: Boolean,
    parent: AnnotationVisitor?,
    methodAccess: Int,
    methodName: String,
    methodDescriptor: String,
    methodSignature: String?,
    methodExceptions: Array<out String>?,
    refmapBuilder: HardTargetRemappingClassVisitor,
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

    companion object {

        fun shouldVisit(
            descriptor: String,
            visible: Boolean,
            methodAccess: Int,
            methodName: String,
            methodDescriptor: String,
            methodSignature: String?,
            methodExceptions: Array<out String>?,
            hardTargetRemapper: HardTargetRemappingClassVisitor
        ): Boolean {
            return descriptor == JarModAgent.Annotation.CSHADOW
        }

    }

    var name: String? = null
    override val remap = AtomicBoolean(!hardTargetRemapper.dontRemap(descriptor))

    override fun visit(name: String?, value: Any?) {
        if (name == AnnotationElement.VALUE) {
            this.name = value as String
        }
        super.visit(name, value)
    }

    override fun visitEnd() {
        hardTargetRemapper.addRemapTask {
            if (remap.get() && name == null) {
                for (target in targetClasses) {
                    val resolved = resolver.resolveMethod(target, methodName, methodDescriptor, ResolveUtility.FLAG_UNIQUE or ResolveUtility.FLAG_RECURSIVE)
                    resolved.ifPresent {
                        val mappedName = mapper.mapName(resolved.get())
                        propagate(hardTargetRemapper.mxClass.getMethod(methodName, methodDescriptor).asTrMember(resolver), mappedName)
                    }
                    if (resolved.isPresent) {
                        return@addRemapTask
                    }
                }
                logger.warn("Could not find target method for @Shadow $methodName$methodDescriptor in $mixinName")
            }
        }
        super.visitEnd()
    }

}
