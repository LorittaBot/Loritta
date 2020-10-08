package net.perfectdreams.loritta.parallax

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.ktor.application.call
import io.ktor.client.HttpClient
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
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.parallax.api.packet.*
import net.perfectdreams.loritta.parallax.compiler.KotlinCompiler
import net.perfectdreams.loritta.parallax.executors.*
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.callSuspendBy

class ParallaxServer {
	companion object {
		val http = HttpClient {
			this.expectSuccess = false
		}

		const val USER_AGENT = "Parallax"
		val gson = Gson()
		val jsonParser = JsonParser()

		val logger = KotlinLogging.logger {}
		val executors = Executors.newFixedThreadPool(8)
		val coroutineDispatchers = executors.asCoroutineDispatcher()
		val authKey = File("./auth-key.txt").readText()
		val dataStoreFolder = File("./datastore")
		val cachedInteractions = mutableMapOf<UUID, java.util.function.Function<Void?, Any?>>()
		val MAX_INSTRUCTIONS = 1000
		val INLINE_METHODS = """
				// ===[ HELPER VARIABLES ]===
				var message = context.message
				var channel = context.message.channel
				var guild = context.message.channel.guild
				var member = context.member
				var user = context.member.user
				var client = context.client
				var args = context.args
				var c = context
				var utils = context.utils
				var u = context.utils
				
				let MessageEmbed = Java.type('net.perfectdreams.loritta.parallax.wrapper.ParallaxEmbed')
				let LorittaReply = Java.type('net.perfectdreams.loritta.parallax.wrapper.JSLorittaReply')
				
				// ===[ HELPER FUNCTIONS ]===
				var send = channel.send
				var reply = message.reply
				var fail = context.utils.fail
				
				// Random for Arrays
				Array.prototype.random = function () {
				  return this[Math.floor((Math.random()*this.length))];
				}
				
				// Check if it is a valid Discord snowflake
				function isValidSnowflake(input) {
					return Number(input) != NaN
				}
			""".trimIndent()

		val SHIFT_STACKTRACE_BY = INLINE_METHODS.lines().size + 3 // 3 = header
		val compilationAndAnalysisMutexes = Caffeine.newBuilder()
				.expireAfterAccess(1, TimeUnit.HOURS)
				.build<Long, Mutex>()
				.asMap()
		val packetExecutors = listOf(
				ParallaxLogPacket::class to LogExecutor,
				ParallaxSendMessagePacket::class to SendMessageExecutor,
				ParallaxPutRolePacket::class to PutRoleExecutor,
				ParallaxDeleteRolePacket::class to DeleteRoleExecutor,
				ParallaxThrowablePacket::class to ThrowableExecutor
		)
	}

	fun start() {
		dataStoreFolder.mkdirs()

		val jsonParser = JsonParser()

		val server = embeddedServer(Netty, port = 3366) {
			routing {
				post("/api/v1/parallax/process-command") {
					val payload = this.call.receiveText()

					val body = jsonParser.parse(payload)
					logger.info { body.toString() }

					// Construir o request
					val kotlinCode = body["code"].string
					val commandLabel = body["label"].string
					val guildId = body["guild"]["id"].long
					val clusterUrl = body["clusterUrl"].string
					val channelId = body["message"]["textChannelId"].long
					val labelHash = commandLabel.hashCode()

					val mutex = compilationAndAnalysisMutexes.getOrPut(guildId) { Mutex() }
					mutex.withLock {
						val outputDirectory = File("/home/parallax/compiled/$guildId/$labelHash/")
						outputDirectory.mkdirs()

						val sourceCodeFile = File(outputDirectory, "source_code")
						var requiresCompilation = true

						if (sourceCodeFile.exists()) {
							// To avoid recompiling the code for no reason, we are going to check if the code is identical to the last time
							// we compiled the code
							requiresCompilation = sourceCodeFile.readText() != kotlinCode
						}

						val compiler = KotlinCompiler()

						if (requiresCompilation) {
							try {
								compiler.kotlinc(
										"""
import net.perfectdreams.loritta.parallax.api.ParallaxContext

fun ParallaxContext.main() {
    $kotlinCode
}
            """.trimIndent(),
										outputDirectory
								)
							} catch (e: Exception) {
								e.printStackTrace()

								// If it failed, then let's delete the output directory
								outputDirectory.delete()

								val response = http.post<HttpResponse>("$clusterUrl/api/v1/parallax/channels/$channelId/messages") {
									this.userAgent(USER_AGENT)
									this.header("Authorization", ParallaxServer.authKey)

									this.body = jsonObject(
											"content" to "Compilation failure!"
									).toString()
								}

								call.respondText("")
								return@post
							}
						}

						val requiresCodeCheck = requiresCompilation

						if (requiresCodeCheck) {
							val codeAnalyzer = CodeAnalyzer()

							outputDirectory.listFiles().forEach {
								if (it.extension == "class") {
									logger.info { "Analyzing ${it.name}..." }
									val result = codeAnalyzer.analyzeFile(it)

									if (!result.success) {
										// If it was rejected, then let's delete the output directory
										outputDirectory.delete()

										logger.warn { "Code was rejected" }
										logger.warn { "Blacklisted class instances: ${result.blacklistedClassInstances}" }
										logger.warn { "Blacklisted methods: ${result.blacklistedMethods}" }

										val response = http.post<HttpResponse>("$clusterUrl/api/v1/parallax/channels/$channelId/messages") {
											this.userAgent(USER_AGENT)
											this.header("Authorization", ParallaxServer.authKey)

											this.body = jsonObject(
													"content" to "Using blacklisted class/methods!"
											).toString()
										}

										call.respondText("")
										return@post
									}
								}
							}
						}

						// Write the source code to a file, then the next time we run the command we can check if the contents
						// have been changed and, if yes, recompile it!
						sourceCodeFile.writeText(kotlinCode)
					}

					val processBuilder = ProcessBuilder(
							"/usr/lib/jvm/jdk-14.0.1+7/bin/java",
							"-Xmx32M",
							"-Dparallax.executeClazz=CUSTOM_COMPILED_CODEKt",
							"-Dfile.encoding=UTF-8",
							"-cp",
							"/home/parallax/code-executor/parallax-code-executor-fat-2020-SNAPSHOT.jar:/home/parallax/compiled/$guildId/$labelHash/:/home/parallax/code-executor/libs/*",
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
							logger.info { it }
							/* output.append(it)
                        output.append("\n") */
							try {
								val packetWrapper = ParallaxSerializer.json.parse(PacketWrapper.serializer(), it)
								val packet = packetWrapper.m

								runBlocking {
									val packetExecutor = packetExecutors.firstOrNull { it.first == packet::class }
									if (packetExecutor != null) {
										logger.info { "Executing $packetExecutor packet executor!" }
										packetExecutor.second::class.members.first { it.name == "executes" }
												.callSuspend(
														packetExecutor.second,
														packetWrapper.uniqueId,
														packet,
														guildId,
														channelId,
														clusterUrl,
														outputStream
												)
									} else {
										logger.warn { "Unknown packet: ${packet}" }
										outputStream.write(ParallaxSerializer.toJson(ParallaxAckPacket(), packetWrapper.uniqueId) + "\n")
										outputStream.flush()
									}
								}
							} catch (e: Exception) {
								logger.warn(e) { "Failed to parse packet" }
							}
						}
					}

					thread {
						process.errorStream.bufferedReader().forEachLine {
							logger.warn { it }
							errorOutput.append(it)
							errorOutput.append("\n")
							// output.append(it)
							// output.append("\n")
						}
					}

					val processResult = process.waitFor(15, TimeUnit.SECONDS)

					logger.info { "Finished: $processResult" }
					process.destroy()

					if (!processResult) {
						call.respondText("Error:\n" + "Timeout")
					} else if (errorOutput.isEmpty()) {
						call.respondText("Output:\n" + output)
					} else {
						call.respondText("Error:\n" + errorOutput)
					}

					// result.getMethod("execute").invoke(null)
					Thread.sleep(2500)
					logger.info { thread }
					logger.info { thread.isAlive }
					if (thread.isAlive) {
						logger.warn { "Why the thread is still alive? Bug??" }
					}
				}
			}
		}
		server.start(wait = true)
	}
}