package com.mrpowergamerbr.loritta.commands.nashorn

import com.github.salomonbrys.kotson.addAll
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornContext
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import io.ktor.client.request.post
import io.ktor.http.userAgent
import jdk.nashorn.api.scripting.ClassFilter
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.NetAddressUtils
import org.graalvm.polyglot.Context
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Comandos usando a Nashorn Engine
 */
class NashornCommand(label: String, val javaScriptCode: String) : AbstractCommand(label, category = CommandCategory.MISC) {
	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		context.reply(
				LoriReply(
						"Comandos personalizados estão desativados devido a problemas de segurança, desculpe pela inconveniência!"
				),
				LoriReply(
						"Custom commands are disabled due to security reasons, sorry for the inconvenience!"
				)
		)
	}

	private suspend fun nashornRun(ogContext: CommandContext) {
		// Funções que jamais poderão ser usadas em comandos
		val blacklisted = "var quit=function(){throw 'Operação não suportada: quit';};var exit=function(){throw 'Operação não suportada: exit';};var print=function(){throw 'Operação não suportada: print';};var echo=function(){throw 'Operação não suportada: echo';};var readLine=function(){throw 'Operação não suportada: readLine';};var readFully=function(){throw 'Operação não suportada: readFully';};var load=function(){throw 'Operação não suportada: load';};var loadWithNewGlobal=function(){throw 'Operação não suportada: loadWithNewGlobal';};"

		val graalContext = Context.newBuilder()
				.hostClassFilter {
					it.startsWith("com.mrpowergamerbr.loritta.parallax.wrappers") || it.startsWith("com.mrpowergamerbr.loritta.commands.nashorn.NashornUtils")
				}
				.allowHostAccess(true) // Permite usar coisas da JVM dentro do GraalJS
				.option("js.nashorn-compat", "true")
				.build()

		if (!javaScriptCode.contains("// USE NEW API")) {
			// Funções inline para facilitar a programação de comandos
			val inlineMethods = """
var message=function(){ return contexto.getMessage(); };
var author=function(){ return contexto.getSender(); };
var getMessage=function(){ return contexto.getMessage(); };
var reply=function(mensagem){ return contexto.reply(mensagem); };
var sendMessage=function(mensagem){ return contexto.sendMessage(mensagem); };
var sendImage=function(imagem, mensagem){ return contexto.sendImage(imagem, mensagem || " "); };
var getArgument=function(index){ return contexto.getArgument(index); };
var getRawArgument=function(index){ return contexto.getRawArgument(index); };
var getStrippedArgument=function(index){ return contexto.getStrippedArgument(index); };
var getArguments=function(){ return contexto.getArguments(); };
var getRawArguments=function(){ return contexto.getRawArguments(); };
var getStrippedArguments=function(){ return contexto.getStrippedArguments(); };
var joinArguments=function(delimitador){ return contexto.joinArguments(delimitador || " "); };
var createImage=function(x, y){ return contexto.createImage(x, y); };
var getImageFromContext=function(argumento) { return contexto.pegarImagemDoContexto(argumento); };
var getGuild=function() { return contexto.getGuild(); };"""
			val executor = Executors.newSingleThreadExecutor(ThreadFactoryBuilder().setNameFormat("JavaScript (GraalJS (Old)) Evaluator Thread for Guild ${ogContext.guild.idLong} - %s").build())
			try {
				val nashornContext = NashornContext(ogContext)
				val future = executor.submit(
						NashornTask(
								graalContext,
								"(function(contexto) { \n" +
										"$blacklisted\n" +
										"$inlineMethods\n" +
										"$javaScriptCode\n })",
								ogContext,
								nashornContext
						)
				)
				future.get(15, TimeUnit.SECONDS)
			} catch (e: Throwable) {
				ParallaxUtils.sendThrowableToChannel(e, ogContext.event.channel)
			}
			executor.shutdownNow()
		} else {
			val guild = ogContext.guild

			val members = JsonArray()

			members.add(ParallaxUtils.transformToJson(ogContext.guild.selfMember))
			members.add(ParallaxUtils.transformToJson(ogContext.message.member!!))
			members.addAll(ogContext.message.mentionedMembers.map { ParallaxUtils.transformToJson(it) })

			val channels = JsonArray()

			guild.channels.forEach {
				channels.add(
						jsonObject(
								"id" to it.idLong,
								"name" to it.name
						)
				)
			}

			val roles = JsonArray()

			guild.roles.forEach {
				roles.add(
						jsonObject(
								"id" to it.idLong,
								"name" to it.name

						)
				)
			}

			val jsonGuild = jsonObject(
					"id" to ogContext.guild.idLong,
					"name" to ogContext.guild.name,
					"members" to members,
					"channels" to channels,
					"roles" to roles
			)

			val commandRequest = jsonObject(
					"code" to javaScriptCode,
					"guild" to jsonGuild,
					"message" to ParallaxUtils.transformToJson(ogContext.message),
					"lorittaClusterId" to loritta.lorittaCluster.id,
					"args" to ogContext.rawArgs.toList().toJsonArray(),
					"clusterUrl" to "https://${loritta.lorittaCluster.getUrl()}"
			)

			logger.info { "Sending code to the Parallax Server Executor..." }

			loritta.http.post<io.ktor.client.statement.HttpResponse>("http://${NetAddressUtils.fixIp(loritta.config.parallaxCodeServer.url)}/api/v1/parallax/process-command") {
				userAgent(loritta.lorittaCluster.getUserAgent())

				body = gson.toJson(commandRequest)
			}

			logger.info { "Parallax code sent to the server executor!" }
		}
	}

	internal class NashornClassFilter : ClassFilter {
		override fun exposeToScripts(s: String): Boolean {
			if (s.compareTo("com.mrpowergamerbr.loritta.commands.nashorn.NashornUtils") == 0) {
				return true
			}
			return false
		}
	}

	@Target(AnnotationTarget.FUNCTION)
	annotation class NashornDocs(
			val description: String = "",
			val arguments: String = "",
			val example: String = ""
	)
}