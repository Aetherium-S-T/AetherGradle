package club.aetherium.gradle.mixin.jma.refmap.annotations

import net.fabricmc.tinyremapper.extension.mixin.common.ResolveUtility
import net.fabricmc.tinyremapper.extension.mixin.common.StringUtility
import net.fabricmc.tinyremapper.extension.mixin.common.data.AnnotationElement
import net.fabricmc.tinyremapper.extension.mixin.common.data.Constant
import org.objectweb.asm.AnnotationVisitor
import club.aetherium.gradle.mixin.jma.JarModAgent
import club.aetherium.gradle.mixin.jma.dontRemap
import club.aetherium.gradle.mixin.mixin.refmap.RefmapBuilderClassVisitor
import club.aetherium.gradle.utils.orElseOptional
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class CTargetAnnotationVisitor(parent: AnnotationVisitor?, remap: AtomicBoolean, private val refmapBuilder: RefmapBuilderClassVisitor) : AnnotationVisitor(Constant.ASM_VERSION, parent) {
    private val remapAt = !refmapBuilder.dontRemap(JarModAgent.Annotation.CTARGET)
    private var targetName: String? = null

    private val resolver = refmapBuilder.resolver
    private val logger = refmapBuilder.logger
    private val existingMappings = refmapBuilder.existingMappings
    private val mapper = refmapBuilder.mapper
    private val refmap = refmapBuilder.refmap
    private val mixinName = refmapBuilder.mixinName
    private val noRefmap = refmapBuilder.mixinRemapExtension.noRefmap.contains("JarModAgent")

    override fun visit(name: String, value: Any) {
        super.visit(name, value)
        if (name == AnnotationElement.TARGET) {
            targetName = (value as String).replace(" ", "")
            if (!noRefmap) super.visit(name, value)
        } else {
            super.visit(name, value)
        }
    }

    override fun visitAnnotation(name: String, descriptor: String): AnnotationVisitor {
        logger.warn("Found annotation in target descriptor: $name $descriptor")
        return super.visitAnnotation(name, descriptor)
    }

    private val targetField = Regex("^(L[^;]+;|[^.]+?\\.)([^:]+):(.+)$")
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
        if (remapAt && targetName != null) {
            val matchFd = targetField.matchEntire(targetName!!)
            if (matchFd != null) {
                var (targetOwner, targetName, targetDesc) = matchToParts(matchFd)
                val target = resolver.resolveField(
                    targetOwner,
                    targetName,
                    targetDesc,
                    ResolveUtility.FLAG_UNIQUE or ResolveUtility.FLAG_RECURSIVE
                ).orElseOptional {
                    existingMappings[this.targetName]?.let { existing ->
                        logger.info("remapping $existing from existing refmap")
                        val matchEFd = targetField.matchEntire(existing)
                        if (matchEFd != null) {
                            val matchResult = matchToParts(matchEFd)
                            targetOwner = matchResult.first
                            val fName = matchResult.second
                            val fDesc = matchResult.third
                            resolver.resolveField(
                                targetOwner,
                                fName,
                                fDesc,
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
                        refmap.addProperty(this.targetName, "L$mappedOwner;$mappedName:$mappedDesc")
                        if (noRefmap) {
                            super.visit(AnnotationElement.TARGET, "L$mappedOwner;$mappedName:$mappedDesc")
                        }
                    }
                }
                if (!target.isPresent || !targetClass.isPresent) {
                    logger.warn(
                        "Failed to resolve At target L$targetOwner;$targetName:$targetDesc in mixin ${
                            mixinName.replace(
                                '/',
                                '.'
                            )
                        }"
                    )
                    if (noRefmap) {
                        super.visit(AnnotationElement.TARGET, this.targetName)
                    }
                }
                super.visitEnd()
                return
            }
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
                        "Failed to resolve At target L$targetOwner;$targetName$targetDesc in mixin ${
                            mixinName.replace(
                                '/',
                                '.'
                            )
                        }"
                    )
                    if (noRefmap) {
                        super.visit(AnnotationElement.TARGET, this.targetName)
                    }
                }
                super.visitEnd()
                return
            }
            if (targetName!!.startsWith("(")) {
                val existing = existingMappings[this.targetName]
                if (existing != null) {
                    logger.info("remapping $existing from existing refmap")
                    val mapped = mapper.asTrRemapper().mapDesc(existing)
                    if (mapped == existing) {
                        logger.warn("Failed to remap $existing")
                        if (noRefmap) {
                            super.visit(AnnotationElement.TARGET, this.targetName)
                        }
                        super.visitEnd()
                        return
                    }
                    refmap.addProperty(this.targetName, mapped)
                    super.visitEnd()
                    return
                } else {
                    val mapped = mapper.asTrRemapper().mapDesc(targetName)
                    if (mapped == targetName) {
                        logger.warn("Failed to remap $targetName")
                        if (noRefmap) {
                            super.visit(AnnotationElement.TARGET, this.targetName)
                        }
                        return
                    } else {
                        refmap.addProperty(this.targetName, mapped)
                        if (noRefmap) {
                            super.visit(AnnotationElement.TARGET, mapped)
                        }
                        super.visitEnd()
                        return
                    }
                }
            }

            // else is probably a class
            // we will count (NEW, <init>) as that too
            // carefully, by modifying method so it doesn't capture it
            val fixedTarget = if (targetName!!.startsWith("L")) {
                targetName!!.substring(1).substringBefore("<init>").substringBefore(";")
            } else {
                targetName!!.substringBefore(".<init>")
            }.replace('.', '/')
            if (StringUtility.isClassName(fixedTarget)) {
                val target = resolver.resolveClass(fixedTarget).orElseOptional {
                    existingMappings[this.targetName]?.let {
                        logger.info("remapping $it from existing refmap")
                        resolver.resolveClass(
                            if (it.startsWith("L") && it.endsWith(";")) {
                                it.substring(1, it.length - 1)
                            } else {
                                it
                            }.replace('.', '/')
                        )
                    } ?: Optional.empty()
                }
                target.ifPresent {
                    val mapped = mapper.mapName(it)
                    refmap.addProperty(this.targetName, "L$mapped;")
                    if (noRefmap) {
                        super.visit(AnnotationElement.TARGET, "L$mapped;")
                    }
                }
                if (target.isPresent) {
                    super.visitEnd()
                    return
                }
            }

            logger.warn(
                "Failed to parse target descriptor: $targetName ($fixedTarget) in mixin ${
                    mixinName.replace(
                        '/',
                        '.'
                    )
                }"
            )
            if (noRefmap) {
                super.visit(AnnotationElement.TARGET, this.targetName)
            }
        } else if (targetName != null) {
            if (noRefmap) {
                super.visit(AnnotationElement.TARGET, this.targetName)
            }
        }
        super.visitEnd()
    }
}
