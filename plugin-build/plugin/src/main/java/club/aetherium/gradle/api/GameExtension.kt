package club.aetherium.gradle.api

open class GameExtension(configure: GameExtension.() -> Unit = {}) {
    open var dependencies = emptyList<String>()
    open var repositories = emptyList<String>()
    open var excludes = emptyList<String>()
    open var annotationProcessors = emptyList<String>()
    open var programArgs = emptyList<String>()
    open var jvmArgs = emptyList<String>()

    init {
        configure()
    }
}
