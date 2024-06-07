package club.aetherium.gradle.mixin.jma.refmap.annotations

import net.fabricmc.tinyremapper.extension.mixin.common.data.AnnotationElement
import net.fabricmc.tinyremapper.extension.mixin.common.data.Constant
import org.objectweb.asm.AnnotationVisitor
import club.aetherium.gradle.mixin.mixin.refmap.RefmapBuilderClassVisitor
import java.util.concurrent.atomic.AtomicBoolean

class CSliceAnnotationVisitor(parent: AnnotationVisitor?, val remap: AtomicBoolean, private val refmapBuilder: RefmapBuilderClassVisitor) : AnnotationVisitor(Constant.ASM_VERSION, parent) {

    override fun visitAnnotation(name: String, descriptor: String): AnnotationVisitor {
        return if (name == AnnotationElement.FROM || name == AnnotationElement.TO) {
            CTargetAnnotationVisitor(super.visitAnnotation(name, descriptor), remap, refmapBuilder)
        } else {
            super.visitAnnotation(name, descriptor)
        }
    }

}
