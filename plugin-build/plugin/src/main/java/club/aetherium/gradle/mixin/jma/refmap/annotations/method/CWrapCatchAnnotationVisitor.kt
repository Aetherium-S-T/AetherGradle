package club.aetherium.gradle.mixin.jma.refmap.annotations.method

import net.fabricmc.tinyremapper.extension.mixin.common.ResolveUtility
import net.fabricmc.tinyremapper.extension.mixin.common.data.AnnotationElement
import net.fabricmc.tinyremapper.extension.mixin.common.data.Constant
import org.objectweb.asm.AnnotationVisitor
import club.aetherium.gradle.mixin.jma.JarModAgent
import club.aetherium.gradle.mixin.jma.refmap.annotations.CSliceAnnotationVisitor
import club.aetherium.gradle.mixin.mixin.refmap.RefmapBuilderClassVisitor
import club.aetherium.gradle.utils.orElseOptional
import java.util.*

@Suppress("UNUSED_PARAMETER")
open class CWrapCatchAnnotationVisitor(
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

    override val annotationName: String = "@CWrapCatch"

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
            return descriptor == JarModAgent.Annotation.CWRAPCATCH
        }
    }

    var targetName: String? = null

    override fun visit(name: String?, value: Any) {
        if (name == AnnotationElement.TARGET) {
            targetName = value as String
            if (!noRefmap) super.visit(name, value)
        } else {
            super.visit(name, value)
        }
    }

    override fun visitAnnotation(name: String, descriptor: String): AnnotationVisitor {
        return when (name) {

            AnnotationElement.SLICE -> {
                CSliceAnnotationVisitor(super.visitAnnotation(name, descriptor), remap, refmapBuilder)
            }

            else -> {
                super.visitAnnotation(name, descriptor)
            }
        }
    }

    override fun visitArray(name: String): AnnotationVisitor {
        return when (name) {
            AnnotationElement.VALUE -> {
                object: AnnotationVisitor(Constant.ASM_VERSION, if (noRefmap) null else super.visitArray(name)) {
                    override fun visit(name: String?, value: Any) {
                        super.visit(name, value)
                        targetNames.add(value as String)
                    }
                }
            }

            else -> {
                super.visitArray(name)
            }
        }
    }

    private val targetMethod = Regex("^(L[^;]+;|[^.]+?\\.)([^(]+)\\s*([^>]+)$")

    private fun matchToParts(match: MatchResult): Triple<String, String, String> {
        val targetOwner = match.groupValues[1].let {
            if (it.startsWith("L") && it.endsWith(";")) it.substring(
                1,
                it.length - 1
            ) else it.substring(0, it.length - 1)
        }
        return Triple(targetOwner, match.groupValues[2], match.groupValues[3])
    }

    override fun visitEnd() {
        val method = if (noRefmap) {
            super.visitArray(AnnotationElement.VALUE)
        } else {
            null
        }
        remapTargetNames {
            method?.visit(null, it)
        }
        method?.visitEnd()
        if (remap.get() && targetName != null) {
            val matchMd = targetMethod.matchEntire(targetName!!)
            if (matchMd != null) {
                var (targetOwner, targetName, targetDesc) = matchToParts(matchMd)
                val target = resolver.resolveMethod(
                    targetOwner,
                    targetName,
                    targetDesc,
                    ResolveUtility.FLAG_UNIQUE or ResolveUtility.FLAG_RECURSIVE
                ).orElseOptional {
                    existingMappings[this.targetName]?.let { existing ->
                        logger.info("remapping $existing from existing refmap")
                        val matchEMd = targetMethod.matchEntire(existing)
                        if (matchEMd != null) {
                            val matchResult = matchToParts(matchEMd)
                            targetOwner = matchResult.first
                            val mName = matchResult.second
                            val mDesc = matchResult.third
                            resolver.resolveMethod(
                                targetOwner,
                                mName,
                                mDesc,
                                ResolveUtility.FLAG_UNIQUE or ResolveUtility.FLAG_RECURSIVE
                            )
                        } else {
                            Optional.empty()
                        }
                    } ?: Optional.empty()
                }
                val targetClass = resolver.resolveClass(targetOwner)
                targetClass.ifPresent { clz ->
                    target.ifPresent {
                        val mappedOwner = mapper.mapName(clz)
                        val mappedName = mapper.mapName(it)
                        val mappedDesc = mapper.mapDesc(it)
                        refmap.addProperty(this.targetName, "L$mappedOwner;$mappedName$mappedDesc")
                        if (noRefmap) {
                            super.visit(AnnotationElement.TARGET, "L$mappedOwner;$mappedName$mappedDesc")
                        }
                    }
                }
                if (!target.isPresent || !targetClass.isPresent) {
                    logger.warn(
                        "Failed to resolve CWrapCatch target $targetName in mixin ${
                            mixinName.replace(
                                '/',
                                '.'
                            )
                        }"
                    )
                    if (noRefmap) {
                        super.visit(AnnotationElement.TARGET, targetName)
                    }
                }
                super.visitEnd()
                return
            } else {
                logger.warn("Failed to parse CWrapCatch target $targetName in mixin ${mixinName.replace('/', '.')}")
                if (noRefmap && targetName != null) {
                    super.visit(AnnotationElement.TARGET, targetName!!)
                }
            }
        } else {
            if (noRefmap && targetName != null) {
                super.visit(AnnotationElement.TARGET, targetName!!)
            }
        }
        super.visitEnd()
    }

}
