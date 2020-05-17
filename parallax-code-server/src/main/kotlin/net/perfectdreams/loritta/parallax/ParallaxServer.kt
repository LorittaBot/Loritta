package net.perfectdreams.loritta.parallax

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.request.receiveText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.asCoroutineDispatcher
import mu.KotlinLogging
import net.perfectdreams.loritta.api.commands.SilentCommandException
import net.perfectdreams.loritta.parallax.wrapper.Client
import net.perfectdreams.loritta.parallax.wrapper.Guild
import net.perfectdreams.loritta.parallax.wrapper.JSCommandContext
import net.perfectdreams.loritta.parallax.wrapper.Message
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.management.ExecutionEvent
import org.graalvm.polyglot.management.ExecutionListener
import java.io.File
import java.util.*
import java.util.concurrent.Executors

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
	}
	val client = Client()

	fun start() {
		dataStoreFolder.mkdirs()

		val server = embeddedServer(Netty, port = 3366) {
			routing {
				get("/api/v1/parallax/reactions/callback/{trackingId}") {
					val trackingId = call.parameters["trackingId"]!!
					logger.info { "Received callback track ID $trackingId!" }

					cachedInteractions[UUID.fromString(trackingId)]?.apply(null)
				}

				post("/api/v1/parallax/process-command") {
					logger.info { "Received request to process command!" }
					val payload = this.call.receiveText()

					val body = jsonParser.parse(payload)

					// Construir o request
					val javaScriptCode = body["code"].string
					val guild = gson.fromJson<Guild>(body["guild"])
					logger.info { "injecting members" }
					guild.members.forEach {
						it.guild = guild
					}

					logger.info { "injecting channels" }
					guild.channels.forEach {
						it.guild = guild
					}
					logger.info { "injecting roles, actually... never mind." }
					/* guild.roles.forEach {
						it.guild = guild
					} */

					logger.info { "msg body: ${body["message"]}" }
					val message = gson.fromJson<Message>(body["message"])
					logger.info { "Message ID is ${message.id}" }
					message.channel = guild.channels.first {
						it.id == message.textChannelId
					}

					logger.info { "Code is $javaScriptCode" }

					val graalContext = Context.newBuilder()
							.hostClassFilter {
								it.startsWith("net.perfectdreams.loritta.parallax.wrapper")
							}
							.allowHostAccess(true) // Permite usar coisas da JVM dentro do GraalJS
							.allowCreateThread(true)
							.allowExperimentalOptions(true)
							.option("js.ecmascript-version", "11") // EMCAScript 2020
							.option("js.nashorn-compat", "true")
							.option("js.experimental-foreign-object-prototype", "true") // Allow array extension methods for arrays
							.build()

					val listener = ExecutionListener.newBuilder()
							.collectInputValues(true)
							.collectReturnValue(true)
							.collectExceptions(true)
							/* .onEnter { e: ExecutionEvent ->
								println(e.location.characters)
							} */
							.onReturn { e: ExecutionEvent ->
								println(e.location.characters)
								println("Input: ${e.inputValues}")
								println("Return Value: ${e.returnValue}")
								println("Exception: ${e.exception}")
							}
							.statements(true)
							.attach(graalContext.engine)

					val executor = Executors.newSingleThreadExecutor()

					val member = guild.members.firstOrNull { it.id == message.author.id }

					if (member == null) {
						logger.error { "Member not found!" }
						return@post
					}

					logger.info { "Loading data store..." }
					val guildDataStore = File(dataStoreFolder, "${guild.id}.json")
					val dataStore = if (guildDataStore.exists()) {
						gson.fromJson(guildDataStore.readText())
					} else {
						mutableMapOf<String, Any?>()
					}

					for (entry in dataStore) {
						if (entry.value is Map<*, *>) {
							println("${entry.key} is a Map Object! Wrapping as a proxy object...")
							entry.setValue(ParallaxUtils.ParallaxDataStoreProxy(entry.value as MutableMap<String, Any?>))
						}
					}

					val context = JSCommandContext(
							graalContext,
							body["lorittaClusterId"].int,
							client,
							member,
							message,
							body["args"].array.map { it.string }.toTypedArray(),
							body["clusterUrl"].string
					)

					guild.context = context

					/* val inlineMethods = """
				var guild = context.guild;
				var member = context.member;
				var user = context.member;
				var author = context.member;
				var message = context.message;
				var channel = context.message.channel;
				var client = context.client;
				var RichEmbed = Java.type('com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxEmbed')
				var Attachment = Java.type('com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxAttachment')
				var http = Java.type('com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxHttp')
			""".trimIndent() */

					val inlineMethods = """
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
				
				// ===[ HELPER FUNCTIONS ]===
				var send = channel.send
				var reply = channel.reply
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

					executor.submit {
						logger.info { "Executing the command!" }

						val source = """(async function(context) {
							|	try {
							|		$inlineMethods
							|		
							|		$javaScriptCode
							|	} catch (e) {
							|		context.jsStacktrace(e);
							|	}
							|})
						""".trimMargin()

						logger.info { "After fill $source" }
						try {
							val value = graalContext.eval(
									"js",
									source
							)

							value.execute(context)

							logger.info { "Execution finished!" }
						} catch (e: Throwable) {
							println("Exception thrown.")
							if (e is SilentCommandException) {
								logger.info { "Silent Command Exception thrown, shhh" }
								return@submit
							}
							logger.warn(e) { "Error while processing custom command!" }
						}
					}
				}
			}
		}
		server.start(wait = true)
	}
}