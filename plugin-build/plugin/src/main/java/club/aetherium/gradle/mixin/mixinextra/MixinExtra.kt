package club.aetherium.gradle.mixin.mixinextra

import club.aetherium.gradle.mixin.mixin.refmap.RefmapBuilderClassVisitor
import club.aetherium.gradle.mixin.mixinextra.annotations.method.*

object MixinExtra {

    object Annotation {

        const val DEFINITION = "Lcom/llamalad7/mixinextras/expression/Definition;"
        const val MODIFY_EXPRESSION_VALUE = "Lcom/llamalad7/mixinextras/injector/ModifyExpressionValue;"
        const val MODIFY_RECIEVER = "Lcom/llamalad7/mixinextras/injector/ModifyReciever;"
        const val MODIFY_RETURN_VALUE = "Lcom/llamalad7/mixinextras/injector/ModifyReturnValue;"
        const val WRAP_WITH_CONDITION = "Lcom/llamalad7/mixinextras/injector/WrapWithCondition;"
        const val WRAP_WITH_CONDITION_V2 = "Lcom/llamalad7/mixinextras/injector/v2/WrapWithCondition;"
        const val WRAP_OPERATION = "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;"


    }

    fun refmapBuilder(refmapBuilder: RefmapBuilderClassVisitor) {

        refmapBuilder.methodAnnotationVisitors.addAll(listOf(
            DefinitionAnnotationVisitor.Companion::shouldVisit to ::DefinitionAnnotationVisitor,
            ModifyExpressionValueAnnotationVisitor.Companion::shouldVisit to ::ModifyExpressionValueAnnotationVisitor,
            ModifyRecieverAnnotationVisitor.Companion::shouldVisit to ::ModifyRecieverAnnotationVisitor,
            ModifyReturnValueAnnotationVisitor.Companion::shouldVisit to ::ModifyReturnValueAnnotationVisitor,
            WrapWithConditionAnnotationVisitor.Companion::shouldVisit to ::WrapWithConditionAnnotationVisitor,
            WrapOperationAnnotationVisitor.Companion::shouldVisit to ::WrapOperationAnnotationVisitor,
        ))

    }

}
