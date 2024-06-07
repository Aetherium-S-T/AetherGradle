package club.aetherium.gradle.api.impl

import club.aetherium.gradle.api.GameExtension

class MixinExtension : GameExtension() {
    override var dependencies = listOf(
        "net.fabricmc:sponge-mixin:0.12.5+mixin.0.8.5",
        "org.ow2.asm:asm:9.4",
        "org.ow2.asm:asm-commons:9.4",
        "org.ow2.asm:asm-tree:9.4",
        "org.ow2.asm:asm-util:9.4",
    )

    override var excludes = listOf(
        "launchwrapper",
        "guava",
        "gson",
    )

    override var repositories = listOf(
        "https://repo.spongepowered.org/repository/maven-public/",
        "https://maven.fabricmc.net/"
    )

    companion object {
        fun mixin() = MixinExtension()
    }
}
