package club.aetherium.gradle.mixin.mixin.refmap

import club.aetherium.gradle.mixin.mixin.refmap.annotations.clazz.MixinAnnotationVisitor
import club.aetherium.gradle.mixin.mixin.refmap.annotations.method.*


object BaseMixinRefmap {


    fun refmapBuilder(refmapBuilder: RefmapBuilderClassVisitor) {

        refmapBuilder.classAnnotationVisitors.addAll(listOf(
            MixinAnnotationVisitor.Companion::shouldVisit to ::MixinAnnotationVisitor
        ))

        refmapBuilder.methodAnnotationVisitors.addAll(listOf(
            AccessorAnnotationVisitor.Companion::shouldVisit to ::AccessorAnnotationVisitor,
            InvokerAnnotationVisitor.Companion::shouldVisit to ::InvokerAnnotationVisitor,
            InjectAnnotationVisitor.Companion::shouldVisit to ::InjectAnnotationVisitor,
            ModifyArgAnnotationVisitor.Companion::shouldVisit to ::ModifyArgAnnotationVisitor,
            ModifyArgsAnnotationVisitor.Companion::shouldVisit to ::ModifyArgsAnnotationVisitor,
            ModifyConstantAnnotationVisitor.Companion::shouldVisit to ::ModifyConstantAnnotationVisitor,
            ModifyVariableAnnotationVisitor.Companion::shouldVisit to ::ModifyVariableAnnotationVisitor,
            RedirectAnnotationVisitor.Companion::shouldVisit to ::RedirectAnnotationVisitor,
        ))

    }


}
