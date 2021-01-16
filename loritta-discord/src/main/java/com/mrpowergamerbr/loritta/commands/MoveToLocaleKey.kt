package com.mrpowergamerbr.loritta.commands

import java.io.File

fun main() {
    analyze(
            File("C:\\Users\\Leonardo\\Documents\\IdeaProjects\\LorittaBot\\Loritta\\loritta-discord\\src\\main\\java\\com\\mrpowergamerbr\\loritta\\commands\\vanilla")
                    .listFiles()
    )
}

fun analyze(files: Array<File>) {
    for (file in files) {
        if (file.isDirectory) {
            analyze(file.listFiles())
        } else if (file.extension == "kt") {
            val lines = file.readLines()

            val indexOfLine = lines.indexOfFirst { it.contains("getDescription(") && it.contains("BaseLocale") }
            if (indexOfLine != -1) {
                println(file)

                // Find the description if possible
                val regex = Regex("return locale\\[\"([\$A-z0-9\\.]+)\\\"]")
                val probablyTheLocale = (lines[indexOfLine + 1])
                val probablyTheEndOfTheBlock = (lines[indexOfLine + 2])

                println(probablyTheLocale)
                println(probablyTheEndOfTheBlock)

                val findMatch = regex.find(probablyTheLocale)

                if (findMatch != null && probablyTheEndOfTheBlock == "\t}" && probablyTheLocale.contains("$")) {
                    println(findMatch.groupValues)

                    // And, if it is found, try to rewrite the file with the new values
                    val newContent = lines.toMutableList()
                    // Remove three lines
                    newContent.removeAt(indexOfLine)
                    newContent.removeAt(indexOfLine)
                    newContent.removeAt(indexOfLine)

                    newContent.add(indexOfLine, "\toverride fun getDescriptionKey() = LocaleKeyData(\"${findMatch.groupValues[1]}\")")

                    println(newContent.joinToString("\n"))

                    file.writeText(newContent.joinToString("\n"))
                }
            }
        }
    }
}