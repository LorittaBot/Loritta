package com.mrpowergamerbr.loritta.commands.nashorn

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornContext
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxContext
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import jdk.nashorn.api.scripting.ClassFilter
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.bson.types.ObjectId
import org.graalvm.polyglot.Context
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Comandos usando a Nashorn Engine
 */
class NashornCommand : AbstractCommand {
	var id = ObjectId() // Object ID único para cada comando
	var jsLabel = "loritta" // label do comando
	lateinit var javaScript: String // código em JS do comando
	var jsAliases: List<String> = ArrayList() // aliases
	var isEnabled = true // Se o comando está ativado
	// var createdDate = LocalDateTime.now() // Data criada
	// var editedDate = LocalDateTime.now() // Data editada
	var authors: List<String> = ArrayList() // Autores do comando (ou seja, quem mexeu)
	var isPublic = false // Se o comando é público no repositório de comandos
	var isForked = false // Se é uma cópia de outro comando na repo de cmds
	var upstreamId: ObjectId? = null // Caso seja forked, o upstreamId irá ter o Object ID original
	var useNewAPI: Boolean = false
	var description: String? = null

	override val label: String
		get() = jsLabel

	constructor() : super("javascript-command-label", listOf(), CommandCategory.MISC)

	constructor(label: String, javaScript: String) : super(label, listOf(), CommandCategory.MISC) {
		this.jsLabel = label
		this.javaScript = javaScript
		this.aliases = jsAliases
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		nashornRun(context, NashornContext(context))
	}

	suspend fun nashornRun(ogContext: CommandContext, context: NashornContext) {
		// Funções que jamais poderão ser usadas em comandos
		val blacklisted = "var quit=function(){throw 'Operação não suportada: quit';};var exit=function(){throw 'Operação não suportada: exit';};var print=function(){throw 'Operação não suportada: print';};var echo=function(){throw 'Operação não suportada: echo';};var readLine=function(){throw 'Operação não suportada: readLine';};var readFully=function(){throw 'Operação não suportada: readFully';};var load=function(){throw 'Operação não suportada: load';};var loadWithNewGlobal=function(){throw 'Operação não suportada: loadWithNewGlobal';};"

		val graalContext = Context.newBuilder()
				.hostClassFilter {
					it.startsWith("com.mrpowergamerbr.loritta.parallax.wrappers") || it.startsWith("com.mrpowergamerbr.loritta.commands.nashorn.NashornUtils")
				}
				.allowHostAccess(true) // Permite usar coisas da JVM dentro do GraalJS
				.option("js.nashorn-compat", "true")
				.build()

		if (!useNewAPI) {
			// Funções inline para facilitar a programação de comandos
			val inlineMethods = """var nashornUtils = Java.type("com.mrpowergamerbr.loritta.commands.nashorn.NashornUtils");
var loritta=function(){ return nashornUtils.loritta(); };
var message=function(){ return contexto.getMessage(); };
var author=function(){ return contexto.getSender(); };
var getMessage=function(){ return contexto.getMessage(); };
var getURL=function(url){ return nashornUtils.getURL(url); };
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
var downloadImage=function(url){ return nashornUtils.downloadImage(url); };
var rgb=function(r, g, b) { return nashornUtils.createColor(r, g, b); };
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
										"$javaScript\n })",
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
			// Funções inline para facilitar a programação de comandos
			val inlineMethods = """
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
			""".trimIndent()
			val executor = Executors.newSingleThreadExecutor(ThreadFactoryBuilder().setNameFormat("JavaScript (GraalJS) Evaluator Thread for Guild ${ogContext.guild.idLong} - %s").build())
			try {
				val parallaxContext = ParallaxContext(ogContext)
				val future = executor.submit(
						ParallaxTask(
								graalContext,
								"(function(context) { \n" +
										"$inlineMethods\n" +
										"$javaScript\n })",
								ogContext,
								parallaxContext
						)
				)
				future.get(15, TimeUnit.SECONDS)
			} catch (e: Throwable) {
				ParallaxUtils.sendThrowableToChannel(e, ogContext.event.channel)
			}
			executor.shutdownNow()
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

	companion object {

	}
}