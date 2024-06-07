package club.aetherium.gradle.mixin.jma.hard

import CShadowFieldAnnotationVisitor
import net.fabricmc.tinyremapper.extension.mixin.common.data.Constant
import club.aetherium.gradle.mixin.jma.DontRemapAnnotationVisitor
import club.aetherium.gradle.mixin.jma.hard.annotations.clazz.CTransformerAnnotationVisitor
import club.aetherium.gradle.mixin.jma.hard.annotations.method.COverrideAnnotationVisitor
import club.aetherium.gradle.mixin.jma.hard.annotations.method.CShadowMethodAnnotationVisitor
import club.aetherium.gradle.mixin.mixin.hard.HardTargetRemappingClassVisitor

object JMAHard {


    fun hardRemapper(hardRemapper: HardTargetRemappingClassVisitor) {

        hardRemapper.insertVisitor {
            DontRemapAnnotationVisitor.DontRemapClassVisitor(Constant.ASM_VERSION, it, hardRemapper.extraData)
        }

        hardRemapper.classAnnotationVisitors.addAll(listOf(
            DontRemapAnnotationVisitor.Companion::shouldVisitHardClass to DontRemapAnnotationVisitor.Companion::visitHardClass,
            CTransformerAnnotationVisitor.Companion::shouldVisit to ::CTransformerAnnotationVisitor
        ))

        hardRemapper.methodAnnotationVisitors.addAll(listOf(
            DontRemapAnnotationVisitor.Companion::shouldVisitHardMethod to DontRemapAnnotationVisitor.Companion::visitHardMethod,
            CShadowMethodAnnotationVisitor.Companion::shouldVisit to ::CShadowMethodAnnotationVisitor,
            COverrideAnnotationVisitor.Companion::shouldVisit to ::COverrideAnnotationVisitor
        ))

        hardRemapper.fieldAnnotationVisitors.addAll(listOf(
            DontRemapAnnotationVisitor.Companion::shouldVisitHardField to DontRemapAnnotationVisitor.Companion::visitHardField,
            CShadowFieldAnnotationVisitor.Companion::shouldVisit to ::CShadowFieldAnnotationVisitor
        ))
    }


}
