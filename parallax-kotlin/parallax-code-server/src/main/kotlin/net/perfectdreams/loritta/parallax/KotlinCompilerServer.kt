package net.perfectdreams.loritta.parallax

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import net.perfectdreams.loritta.parallax.api.packet.*
import net.perfectdreams.loritta.parallax.compiler.KotlinCompiler
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    val jsonParser = JsonParser()

    val server = embeddedServer(Netty, port = 8080) {
        routing {
            post("/api/v1/parallax/process-command") {
                val payload = this.call.receiveText()

                val body = jsonParser.parse(payload)

                // Construir o request
                val kotlinCode = body["code"].string

                val compiler = KotlinCompiler()
                val result = compiler.kotlinc(
                        """
import net.perfectdreams.loritta.parallax.api.ParallaxContext

fun main(context: ParallaxContext) {
    $kotlinCode
}
            """.trimIndent()
                )!!

                val processBuilder = ProcessBuilder(
                        "java",
                        "-Xmx32M",
                        "-Dparallax.executeClazz=CUSTOM_COMPILED_CODEKt",
                        "-cp",
                        "C:\\Users\\Leonardo\\Documents\\IdeaProjects\\LorittaBot\\Loritta\\parallax-kotlin\\parallax-code-executor\\build\\libs\\parallax-code-executor-fat-2020-SNAPSHOT.jar;L:\\kotlin-compiler-1.3.72\\temp\\;C:\\Users\\Leonardo\\Documents\\IdeaProjects\\LorittaBot\\Loritta\\libs\\*",
                        "net.perfectdreams.loritta.parallax.executor.ParallaxCodeExecutor"
                )

                val process = processBuilder.start()
                val outputStream = process.outputStream.bufferedWriter()
                val output = StringBuilder()
                val errorOutput = StringBuilder()

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
                                    println("Sending message: ${packet.content}")
                                    outputStream.write(ParallaxSerializer.toJson(ParallaxAckSendMessagePacket("successfully received :3"), packetWrapper.uniqueId) + "\n")
                                    outputStream.flush()
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

