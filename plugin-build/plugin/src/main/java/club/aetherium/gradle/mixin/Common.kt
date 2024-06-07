package club.aetherium.gradle.mixin

fun splitMethodNameAndDescriptor(nameAndDescriptor: String): Pair<String, String?> {
    val mName = nameAndDescriptor.substringBefore("(").substringAfter(";")
    val desc = if (nameAndDescriptor.contains("(")) "(${nameAndDescriptor.substringAfter("(")}" else null
    return Pair(mName, desc)
}

fun splitFieldNameAndDescriptor(nameAndDescriptor: String): Pair<String, String?> {
    val fName = nameAndDescriptor.substringBefore(":").substringAfter(";")
    val desc = if (nameAndDescriptor.contains(":")) nameAndDescriptor.substringAfter(":") else null
    return Pair(fName, desc)
}
