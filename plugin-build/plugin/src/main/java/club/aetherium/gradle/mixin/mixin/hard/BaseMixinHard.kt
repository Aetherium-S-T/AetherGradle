package club.aetherium.gradle.mixin.mixin.hard

import club.aetherium.gradle.mixin.mixin.hard.annotations.field.ShadowFieldAnnotationVisitor
import club.aetherium.gradle.mixin.mixin.hard.annotations.clazz.ImplementsAnnotationVisitor
import club.aetherium.gradle.mixin.mixin.hard.annotations.clazz.MixinAnnotationVisitor
import club.aetherium.gradle.mixin.mixin.hard.annotations.method.OverwriteAnnotationVisitor
import club.aetherium.gradle.mixin.mixin.hard.annotations.method.ShadowMethodAnnotationVisitor

object BaseMixinHard {


    fun hardRemapper(hardRemapper: HardTargetRemappingClassVisitor) {

        hardRemapper.classAnnotationVisitors.addAll(listOf(
            MixinAnnotationVisitor.Companion::shouldVisit to ::MixinAnnotationVisitor,
            ImplementsAnnotationVisitor.Companion::shouldVisit to ::ImplementsAnnotationVisitor
        ))

        hardRemapper.methodAnnotationVisitors.addAll(listOf(
            OverwriteAnnotationVisitor.Companion::shouldVisit to ::OverwriteAnnotationVisitor,
            ShadowMethodAnnotationVisitor.Companion::shouldVisit to ::ShadowMethodAnnotationVisitor
        ))

        hardRemapper.fieldAnnotationVisitors.addAll(listOf(
            ShadowFieldAnnotationVisitor.Companion::shouldVisit to ::ShadowFieldAnnotationVisitor
        ))

    }


}
