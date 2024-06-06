package club.aetherium.gradle.decompiler

import org.gradle.api.logging.Logger
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger

class VineFlowerLogger(logger: Logger) : IFernflowerLogger() {
    private val logger = logger

    override fun writeMessage(p0: String, p1: Severity) {
        when(p1) {
            Severity.TRACE -> logger.trace(p0)
            Severity.INFO -> logger.info(p0)
            Severity.WARN -> logger.warn(p0)
            Severity.ERROR -> logger.error(p0)
        }
    }

    override fun writeMessage(p0: String, p1: Severity, p2: Throwable) {
        when(p1) {
            Severity.TRACE -> logger.trace(p0, p2)
            Severity.INFO -> logger.info(p0, p2)
            Severity.WARN -> logger.warn(p0, p2)
            Severity.ERROR -> logger.error(p0, p2)
        }
    }
}
