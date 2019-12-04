package net.perfectdreams.loritta.parallax

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.request.receiveText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.asCoroutineDispatcher
import mu.KotlinLogging
import net.perfectdreams.loritta.parallax.wrapper.Client
import net.perfectdreams.loritta.parallax.wrapper.Guild
import net.perfectdreams.loritta.parallax.wrapper.JSCommandContext
import net.perfectdreams.loritta.parallax.wrapper.Message
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.management.ExecutionEvent
import org.graalvm.polyglot.management.ExecutionListener
import java.io.File
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
	}
	val client = Client()

	fun start() {
		val server = embeddedServer(Netty, port = 3366) {
			routing {
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
							.option("js.nashorn-compat", "true")
							.build()


					val listener = ExecutionListener.newBuilder()
							.onEnter { e: ExecutionEvent ->
								println(e.location.characters)
							}
							.statements(true)
							// .attach(graalContext.engine)

					val executor = Executors.newSingleThreadExecutor()

					val member = guild.members.firstOrNull { it.id == message.author.id }

					if (member == null) {
						logger.error { "Member not found!" }
						return@post
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
				var message = context.message
				var channel = context.message.channel
				var guild = context.message.channel.guild
				var member = context.member
				var user = context.member.user
				var client = context.client

				var send = channel.send
				var reply = channel.reply
				
				var MessageEmbed = Java.type('net.perfectdreams.loritta.parallax.wrapper.ParallaxEmbed')
			""".trimIndent()

					executor.submit {
						logger.info { "Executing the command!" }
						try {
							val value = graalContext.eval("js", "(async function(context) { try {\n" +
									"$inlineMethods\n" +
									"$javaScriptCode\n" +
									"} catch (e) { context.jsStacktrace(e); }})")

							value.execute(context)
						} catch (e: Throwable) {
							println("Exception thrown.")
							logger.warn(e) { "Error while processing custom command!" }
						}
					}
				}
			}
		}
		server.start(wait = true)
	}
}