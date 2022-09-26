package net.perfectdreams.loritta.legacy

import java.io.File

fun main() {
    val base = File("C:\\Users\\Leonardo\\IdeaProjects\\LorittaBot\\LorittaPhoenix\\loritta-serializable-commons\\src")

    val commonMain = File(base, "commonMain/kotlin")
    recursiveSearch(commonMain, commonMain)
}

fun recursiveSearch(base: File, input: File) {
    for (file in input.listFiles()) {
        if (file.isDirectory) {
            recursiveSearch(base, file)
        } else if (file.isFile) {
            val fullyQualifiedName = file.toString()
                .removePrefix(base.toString())
                .removeSuffix(".kt")
                .replace("\\", ".")
                .removePrefix(".")

            val newFullyQualifiedName = fullyQualifiedName.replace("lorittaserializable", "loritta.serializable")
            val lines = file.readLines().toMutableList()
            lines[0] = "package $newFullyQualifiedName"
            file.writeText(lines.joinToString("\n"))
        }
    }
}