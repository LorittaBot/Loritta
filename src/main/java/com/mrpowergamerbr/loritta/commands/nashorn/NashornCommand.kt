package com.mrpowergamerbr.loritta.commands.nashorn

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.nashorn.wrappers.NashornContext
import jdk.nashorn.api.scripting.ClassFilter
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.exception.ExceptionUtils
import org.bson.types.ObjectId

import javax.script.Invocable
import java.awt.*
import java.time.LocalDateTime
import java.util.ArrayList
import java.util.concurrent.*

/**
 * Comandos usando a Nashorn Engine
 */
class NashornCommand : CommandBase {
	var id = ObjectId() // Object ID único para cada comando
	var jsLabel = "loritta" // label do comando
	lateinit var javaScript: String // código em JS do comando
	var jsAliases: List<String> = ArrayList() // aliases
	var isEnabled = true // Se o comando está ativado
	var createdDate = LocalDateTime.now() // Data criada
	var editedDate = LocalDateTime.now() // Data editada
	var authors: List<String> = ArrayList() // Autores do comando (ou seja, quem mexeu)
	var isPublic = false // Se o comando é público no repositório de comandos
	var isForked = false // Se é uma cópia de outro comando na repo de cmds
	var upstreamId: ObjectId? = null // Caso seja forked, o upstreamId irá ter o Object ID original

	constructor() {}

	constructor(label: String, javaScript: String) {
		this.jsLabel = label
		this.javaScript = javaScript
	}

	override fun getLabel(): String {
		return jsLabel
	}

	override fun getAliases(): List<String> {
		return jsAliases
	}

	override fun run(context: CommandContext) {
		nashornRun(context, NashornContext(context))
	}

	fun nashornRun(ogContext: CommandContext, context: NashornContext) {
		val factory = NashornScriptEngineFactory()

		val engine = factory.getScriptEngine(NashornClassFilter())
		val invocable = engine as Invocable
		// Funções que jamais poderão ser usadas em comandos
		val blacklisted = "var quit=function(){throw 'Operação não suportada: quit';};var exit=function(){throw 'Operação não suportada: exit';};var print=function(){throw 'Operação não suportada: print';};var echo=function(){throw 'Operação não suportada: echo';};var readLine=function(){throw 'Operação não suportada: readLine';};var readFully=function(){throw 'Operação não suportada: readFully';};var load=function(){throw 'Operação não suportada: load';};var loadWithNewGlobal=function(){throw 'Operação não suportada: loadWithNewGlobal';};"
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
		try {
			val executor = Executors.newSingleThreadExecutor()
			val future = executor.submit(NashornTask(engine, "$blacklisted function nashornCommand(contexto) {\n$inlineMethods\n$javaScript\n}", ogContext, context))
			future.get(15, TimeUnit.SECONDS)
		} catch (e: Exception) {
			e.printStackTrace()
			val builder = EmbedBuilder()
			builder.setTitle("❌ Ih Serjão Sujou! 🤦", "https://youtu.be/G2u8QGY25eU")
			var description = "Irineu, você não sabe e nem eu!"
			if (e is ExecutionException) {
				description = "A thread que executava este comando agora está nos céus... *+angel* (Provavelmente seu script atingiu o limite máximo de memória utilizada!)"
			} else {
				if (e != null && e.cause != null && (e.cause as Throwable).message != null) {
					description =  (e.cause as Throwable).message!!.trim { it <= ' ' }
				} else if (e != null) {
					description = ExceptionUtils.getStackTrace(e).substring(0, Math.min(2000, ExceptionUtils.getStackTrace(e).length))
				}
			}
			builder.setDescription("```$description```")
			builder.setFooter(
					"Aprender a programar seria bom antes de me forçar a executar códigos que não funcionam 😢", null)
			builder.setColor(Color.RED)
			ogContext.sendMessage(builder.build())
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