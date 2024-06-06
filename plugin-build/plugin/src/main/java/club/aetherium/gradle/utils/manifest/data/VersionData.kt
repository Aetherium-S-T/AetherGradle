package club.aetherium.gradle.utils.manifest.data

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class VersionData(
    val arguments: VersionArguments? = null,
    val assetIndex: AssetIndex,
    val assets: String,
    val complianceLevel: Int,
    val downloads: Downloads,
    val id: String,
    val javaVersion: JavaVersion,
    val libraries: MutableList<LibraryX>,
    val logging: Logging,
    var mainClass: String,
    val minecraftArguments: String? = null,
    val minimumLauncherVersion: Int,
    val releaseTime: String,
    val time: String,
    val type: String
)


data class VersionArguments(
    val game: MutableList<JsonElement>,
    val jvm: MutableList<JsonElement>
)


data class AssetIndex(
    val id: String,
    val sha1: String,
    val size: Long,
    val totalSize: Int,
    val url: String
)


data class Downloads(
    val client: Client
)


data class JavaVersion(
    val component: String,
    val majorVersion: Int
)


data class LibraryX(
    val downloads: DownloadsX,
    val name: String,
    val rules: List<Rule>? = null,
    val extract: Extract? = null,
    val natives: Natives? = null
) {
    override fun toString(): String {
        return "Library($name,$rules,$extract,$natives,$downloads)"
    }
}


data class Logging(
    val client: ClientX
)


data class Client(
    val sha1: String,
    val size: Long,
    val url: String
)


data class DownloadsX(
    val artifact: Artifact? = null,
    val classifiers: Classifiers? = null
) {
    override fun toString(): String {
        return "club.aetherium.gradle.utils.manifest.data.Downloads($artifact,$classifiers)"
    }
}


data class Rule(
    val action: String,
    val os: Os? = null,
    val features: Features? = null
)


data class Features(val ele: JsonElement)


data class Extract(
    val exclude: List<String>
)


data class Natives(
    val linux: String? = null,
    val osx: String? = null,
    val windows: String? = null
)


data class Artifact(
    val path: String,
    val sha1: String,
    val size: Long,
    val url: String
) {
    override fun toString(): String {
        return "club.aetherium.gradle.utils.manifest.data.Artifact(path=$path,sha1=$sha1,size=$size,url=$url)"
    }
}


data class Classifiers(
    @SerializedName("natives-linux") val nativesLinux: NativesX? = null,
    @SerializedName("natives-osx") val nativesOsx: NativesX? = null,
    @SerializedName("natives-windows") val nativesWindows: NativesX? = null
)


data class NativesX(
    val path: String,
    val sha1: String,
    val size: Long,
    val url: String
)


data class Os(
    val name: String
)


data class ClientX(
    val argument: String,
    val file: FileX,
    val type: String
)


data class FileX(
    val id: String,
    val sha1: String,
    val size: Long,
    val url: String
)
