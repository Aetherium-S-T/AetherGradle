package club.aetherium.gradle.mixin.mixin.refmap.annotations.method

import net.fabricmc.tinyremapper.extension.mixin.common.ResolveUtility
import net.fabricmc.tinyremapper.extension.mixin.common.data.Annotation
import net.fabricmc.tinyremapper.extension.mixin.common.data.AnnotationElement
import org.objectweb.asm.AnnotationVisitor
import club.aetherium.gradle.mixin.mixin.refmap.RefmapBuilderClassVisitor
import club.aetherium.gradle.utils.orElseOptional
import org.gradle.configurationcache.extensions.capitalized
import java.util.*

@Suppress("UNUSED_PARAMETER")
class AccessorAnnotationVisitor(
    descriptor: String,
    visible: Boolean,
    parent: AnnotationVisitor,
    methodAccess: Int,
    methodName: String,
    methodDescriptor: String,
    methodSignature: String?,
    methodExceptions: Array<out String>?,
    refmapBuilder: RefmapBuilderClassVisitor,
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
            refmapBuilder: RefmapBuilderClassVisitor
        ): Boolean {
            return descriptor == Annotation.ACCESSOR
        }

        val validPrefixes = mutableSetOf(
            "get",
            "is",
            "set"
        )

    }

    override val annotationName: String = "@Accessor"

    override fun visit(name: String?, value: Any) {
        if (!noRefmap) {
            super.visit(name, value)
        }
        if (name == AnnotationElement.VALUE || name == null) targetNames.add(value as String)
    }

    override fun visitEnd() {
        remapTargetNames { mappedName ->
            if (noRefmap) {
                super.visit(AnnotationElement.VALUE, mappedName)
            }
        }
        super.visitEnd()
    }

    override fun remapTargetNames(noRefmapAcceptor: (String) -> Unit) {
        if (remap.get()) {
            val prefix = validPrefixes.firstOrNull { methodName.startsWith(it) }
            val targetNames = if (targetNames.isEmpty()) {
                if (prefix == null) {
                    logger.warn(
                        "Failed to resolve accessor $methodName in mixin ${
                            mixinName.replace(
                                '/',
                                '.'
                            )
                        }, unknown prefix"
                    )
                    return
                }
                listOf(methodName.substring(prefix.length).decapitalize(), methodName.substring(prefix.length))
            } else {
                targetNames
            }
            for (targetClass in targetClasses) {
                for (targetName in targetNames) {
                    val targetDesc = if (methodDescriptor.startsWith("()")) {
                        methodDescriptor.substringAfter(")")
                    } else {
                        methodDescriptor.substringBefore(")").substringAfter("(")
                    }
                    var implicitWildcard = false
                    val target = resolver.resolveField(
                        targetClass,
                        targetName,
                        targetDesc,
                        ResolveUtility.FLAG_UNIQUE or ResolveUtility.FLAG_RECURSIVE
                    ).orElseOptional {
                        existingMappings[targetName]?.let {
                            logger.info("remapping $it from existing refmap")
                            val mName = it.substringBefore(":")
                            val desc = if (it.contains(":")) {
                                it.substringAfter(":")
                            } else null
                            if (desc == null && allowImplicitWildcards) {
                                implicitWildcard = true
                            }
                            resolver.resolveField(
                                targetClass,
                                mName,
                                desc,
                                (if (implicitWildcard) ResolveUtility.FLAG_FIRST else ResolveUtility.FLAG_UNIQUE) or ResolveUtility.FLAG_RECURSIVE
                            )
                        } ?: Optional.empty()
                    }
                    target.ifPresent {
                        val mappedName = mapper.mapName(it)
                        val mappedDesc = mapper.mapDesc(it)
                        if (implicitWildcard) {
                            // BUGFIX: it appears 1 length don't lowercase when checking the refmap
                            if (targetName.length == 1) {
                                refmap.addProperty(targetName.capitalized(), mappedName)
                            }
                            refmap.addProperty(targetName, mappedName)
                            noRefmapAcceptor(mappedName)
                        } else {
                            // BUGFIX: it appears 1 length don't lowercase when checking the refmap
                            if (targetName.length == 1) {
                                refmap.addProperty(targetName.capitalized(), "$mappedName:$mappedDesc")
                            }
                            refmap.addProperty(targetName, "$mappedName:$mappedDesc")
                            noRefmapAcceptor(mappedName)
                        }
                    }
                    if (target.isPresent) return
                }
            }
            logger.warn(
                "Failed to resolve field accessor $targetNames ($methodName$methodDescriptor) in mixin ${
                    mixinName.replace(
                        '/',
                        '.'
                    )
                } targetClasses $targetClasses"
            )
            if (targetNames.isNotEmpty()) {
                noRefmapAcceptor(targetNames.first())
            }
        }
    }

}
