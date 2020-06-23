package net.perfectdreams.loritta.parallax

import java.io.File

class CodeAnalyzer {
    private val whitelistedNewInstanceClasses = listOf(
            "java/lang/StringBuilder",
            "java/util/NoSuchElementException"
    )

    private val whitelistedInstanceStartsWithClasses = listOf(
            "CUSTOM_COMPILED_CODEKt\$main" // Used for in code functions
    )

    private val whitelistedMethods = listOf(
            "invoke:()Ljava/lang/String;" // Strings
    )

    private val whitelistedMethodsStartsWith = listOf(
            "java/util/List.get:", // For lists
            "java/util/List.contains:", // For lists
            "java/lang/Iterable.", // For collections/iterator
            "java/util/Iterator.", // For collections/iterator
            "java/lang/StringBuilder.", // Used internally by the compiler
            "java/lang/Boolean.valueOf:;", // Used internally by the compiler for string interpolation (${owo})
            "java/lang/String.valueOf:", // Used internally by the compiler for string interpolation (${owo})
            "java/lang/Object.\"<init>\":", // Object init
            "java/util/NoSuchElementException.\"<init>\"",
            "kotlin/jvm/internal/Intrinsics.",
            "net/perfectdreams/loritta/parallax/api/ParallaxContext",
            "net/perfectdreams/loritta/parallax/api/ParallaxUser",
            "net/perfectdreams/loritta/parallax/api/ParallaxMember",
            "net/perfectdreams/loritta/parallax/api/ParallaxRole",
            "net/perfectdreams/loritta/parallax/api/ParallaxMessage",
            "net/perfectdreams/loritta/parallax/api/ParallaxMessageChannel",
            "net/perfectdreams/loritta/parallax/api/ParallaxGuild",
            "CUSTOM_COMPILED_CODEKt\$main", // Used for in code functions
            "\"<init>\"", // Used for in code functions
            "kotlin/jvm/internal/Lambda.\"<init>\"",
            "kotlin/collections/CollectionsKt.listOf:",
            "kotlin/collections/CollectionsKt.mutableListOf:",
            "kotlin/jvm/functions/Function", // Lambdas
            "invoke:", // Lambdas
            "kotlin/collections/CollectionsKt.joinToString"
    )

    fun analyzeFile(file: File): AnalysisResult {
        val javaPProcessBuilder = ProcessBuilder(
                "/usr/lib/jvm/jdk-14.0.1+7/bin/javap",
                "-c",
                "-p",
                file.toString()
        )

        val process = javaPProcessBuilder.start()
        process.waitFor()
        val lines = process.inputStream.bufferedReader().lines()

        var readingCode = false

        val blacklistedClassInstances = mutableSetOf<String>()
        val blacklistedMethods = mutableSetOf<String>()

        for (line in lines) {
            // println(line)

            if (line.isBlank()) {
                readingCode = false
            } else if (readingCode) {
                val matcher = Regex("([0-9]+): ([A-z_0-9]+)(.+\\/\\/ (.+))?").find(line)

                if (matcher != null) {
                    val instruction = matcher.groupValues[2]
                    val comment = matcher.groupValues[4]
                    if ((instruction == "new" || instruction == "ldc") && comment.startsWith("class ")) {
                        val clazz = comment.removePrefix("class ")
                        if (clazz !in whitelistedNewInstanceClasses && !whitelistedInstanceStartsWithClasses.any { clazz.startsWith(it) }) {
                            println(">>> Blacklisted class instance $clazz")
                            blacklistedClassInstances.add(clazz)
                        }
                    }

                    if (matcher.groupValues[2].startsWith("invoke")) {
                        println(line)
                        val method = comment.removePrefix("Interface").removePrefix("Method ")
                        if (method !in whitelistedMethods && !whitelistedMethodsStartsWith.any { method.startsWith(it) }) {
                            println(">>> Blacklisted method $method")
                            blacklistedMethods.add(method)
                        }
                    }
                }
            } else if (line == "    Code:") {
                readingCode = true
            }
        }

        return AnalysisResult(
                blacklistedClassInstances.isEmpty() && blacklistedMethods.isEmpty(),
                blacklistedClassInstances,
                blacklistedMethods
        )
    }

    class AnalysisResult(
            val success: Boolean,
            val blacklistedClassInstances: Set<String>,
            val blacklistedMethods: Set<String>
    )
}