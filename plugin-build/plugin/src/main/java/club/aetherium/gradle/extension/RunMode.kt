package club.aetherium.gradle.extension

import org.gradle.api.Project

data class RunMode(
    val vmArgs: List<String>,
    val runArgs: List<String>,
    val additionalDependencies: List<String>,
    val additionalRepositories: List<String>,
    val mainClass: String
) {
    companion object {
        val Vanilla = RunMode(emptyList(), emptyList(), emptyList(), emptyList(),
            "net.minecraft.client.main.Main")
        fun tweaker(vararg tweakClasses: String) = RunMode(
            emptyList(),
            tweakClasses.map { "--tweakClass=$it" },
            listOf("com.github.heni123321:LegacyLauncher:ac106bbe00"),
            listOf("https://jitpack.io"),
            "net.minecraft.launchwrapper.Launch"
        )
    }
}
