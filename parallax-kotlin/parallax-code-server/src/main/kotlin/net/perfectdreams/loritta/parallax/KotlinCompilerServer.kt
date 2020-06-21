package net.perfectdreams.loritta.parallax

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import io.ktor.application.call
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.userAgent
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.parallax.api.packet.*
import net.perfectdreams.loritta.parallax.compiler.KotlinCompiler
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object KotlinCompilerServer {
    fun main(args: Array<String>) {
        val jsonParser = JsonParser()

        val server = embeddedServer(Netty, port = 3366) {
            routing {
                post("/api/v1/parallax/process-command") {
                    val payload = this.call.receiveText()

                    val body = jsonParser.parse(payload)
                    println(body.toString())

                    // Construir o request
                    val kotlinCode = body["code"].string
                    val clusterUrl = body["clusterUrl"].string
                    val channelId = body["message"]["textChannelId"].long

                    val compiler = KotlinCompiler()
                    val result = compiler.kotlinc(
                            """
import net.perfectdreams.loritta.parallax.api.ParallaxContext

fun main(context: ParallaxContext) {
    $kotlinCode
}
            """.trimIndent()
                    )!!
                    File("/home/parallax/compiled/").listFiles().forEach {
                        if (it.extension == "class") {
                            try {
                                println("Analyzing ${it.name}...")
                                analyzeFile(it)
                            } catch (e: RuntimeException) {
                                e.printStackTrace()
                            }
                        }
                    }

                    val processBuilder = ProcessBuilder(
                            "/usr/lib/jvm/jdk-14.0.1+7/bin/java",
                            "-Xmx32M",
                            "-Dparallax.executeClazz=CUSTOM_COMPILED_CODEKt",
                            "-Dfile.encoding=UTF-8",
                            "-cp",
                            "/home/parallax/code-executor/parallax-code-executor-fat-2020-SNAPSHOT.jar:/home/parallax/compiled/:/home/parallax/code-executor/libs/*",
                            "net.perfectdreams.loritta.parallax.executor.ParallaxCodeExecutor"
                    )

                    val process = processBuilder.start()
                    val outputStream = process.outputStream.bufferedWriter()
                    val output = StringBuilder()
                    val errorOutput = StringBuilder()

                    outputStream.write(body.toString() + "\n")
                    outputStream.flush()

                    val thread = thread {
                        process.inputStream.bufferedReader().forEachLine {
                            println(it)
                            /* output.append(it)
                        output.append("\n") */
                            try {
                                val packetWrapper = ParallaxSerializer.json.parse(PacketWrapper.serializer(), it)
                                val packet = packetWrapper.m

                                when (packet) {
                                    is ParallaxLogPacket -> {
                                        println(packet.message)
                                        outputStream.write(ParallaxSerializer.toJson(ParallaxAckPacket(), packetWrapper.uniqueId) + "\n")
                                        outputStream.flush()
                                    }
                                    is ParallaxSendMessagePacket -> {
                                        runBlocking {
                                            println("Sending message: ${packet.content}")

                                            val response = ParallaxServer.http.post<HttpResponse>("$clusterUrl/api/v1/parallax/channels/$channelId/messages") {
                                                this.userAgent(ParallaxServer.USER_AGENT)
                                                this.header("Authorization", ParallaxServer.authKey)

                                                this.body = jsonObject(
                                                        "content" to packet.content
                                                ).toString()
                                            }

                                            outputStream.write(ParallaxSerializer.toJson(ParallaxAckSendMessagePacket("successfully received :3"), packetWrapper.uniqueId) + "\n")
                                            outputStream.flush()
                                        }
                                    }
                                    else -> {
                                        println("Unknown packet: ${packet}")
                                        outputStream.write(ParallaxSerializer.toJson(ParallaxAckPacket(), packetWrapper.uniqueId) + "\n")
                                        outputStream.flush()
                                    }
                                }
                            } catch (e: Exception) {
                                println("Failed to parse packet")
                                e.printStackTrace()
                            }
                        }
                    }

                    thread {
                        process.errorStream.bufferedReader().forEachLine {
                            println(it)
                            errorOutput.append(it)
                            errorOutput.append("\n")
                            // output.append(it)
                            // output.append("\n")
                        }
                    }

                    val processResult = process.waitFor(15, TimeUnit.SECONDS)

                    println("Finished: $processResult")
                    process.destroy()

                    if (!processResult) {
                        call.respondText("Error:\n" + "Timeout")
                    } else if (errorOutput.isEmpty()) {
                        call.respondText("Output:\n" + output)
                    } else {
                        call.respondText("Error:\n" + errorOutput)
                    }
                    /* result.methods.forEach {
                    println(it)
                } */

                    // result.getMethod("execute").invoke(null)
                    Thread.sleep(2500)
                    println(thread)
                    println(thread.isAlive)
                }
            }
        }
        server.start(wait = true)
    }

    val whitelistedNewInstanceClasses = listOf(
            "java/lang/StringBuilder"

    )

    val whitelistedInstanceStartsWithClasses = listOf(
            "CUSTOM_COMPILED_CODEKt\$main" // Used for in code functions
    )

    val whitelistedMethods = listOf(
            "invoke:()Ljava/lang/String;" // Strings
    )

    val whitelistedMethodsStartsWith = listOf(
            "kotlin/jvm/internal/Intrinsics.",
            "net/perfectdreams/loritta/parallax/api/ParallaxContext",
            "CUSTOM_COMPILED_CODEKt\$main", // Used for in code functions
            "\"<init>\"", // Used for in code functions
            "kotlin/jvm/internal/Lambda.\"<init>\"",
            "java/lang/StringBuilder.",
            "kotlin/collections/CollectionsKt.listOf:",
            "kotlin/collections/CollectionsKt.mutableListOf:",
            "java/util/List.get", // For lists
            "kotlin/jvm/functions/Function", // Lambdas
            "invoke:", // Lambdas
            "java/lang/Boolean.valueOf:(Z)Ljava/lang/Boolean;",
            "java/lang/String.valueOf:(Z)Ljava/lang/String;",
            "kotlin/collections/CollectionsKt.joinToString"
    )

    fun analyzeFile(file: File) {
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
                        if (clazz !in whitelistedNewInstanceClasses && !whitelistedInstanceStartsWithClasses.any { clazz.startsWith(it) })
                            println(">>> Blacklisted class instance $clazz")
                    }

                    if (matcher.groupValues[2].startsWith("invoke")) {
                        println(line)
                        val method = comment.removePrefix("Interface").removePrefix("Method ")
                        if (method !in whitelistedMethods && !whitelistedMethodsStartsWith.any { method.startsWith(it) })
                            println(">>> Blacklisted method $method")
                    }
                }
            } else if (line == "    Code:") {
                readingCode = true
            }
        }
    }
}