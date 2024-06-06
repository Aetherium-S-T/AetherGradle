package club.aetherium.gradle.utils.manifest.data

// Version manifest from `piston-meta.mojang.com`
data class VersionManifest(
    val latest: Latest,
    val versions: List<MVersion>,
)

data class Latest(
    val release: String,
    val snapshot: String,
)

data class MVersion(
    val id: String,
    val type: String,
    val url: String,
    val time: String,
    val releaseTime: String,
) {
    val versionType = VersionType.from(type)
}

enum class VersionType(val jsonValue: String) {
    Release("release"),
    OldBeta("old_beta"),
    OldAlpha("old_alpha"),
    Snapshot("snapshot");

    companion object {
        fun from(jsonValue: String): VersionType {
            return VersionType.values().find { it.jsonValue == jsonValue }!!
        }
    }
}
