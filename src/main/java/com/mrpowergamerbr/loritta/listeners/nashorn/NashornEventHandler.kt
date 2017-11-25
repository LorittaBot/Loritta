package com.mrpowergamerbr.loritta.listeners.nashorn

import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornGuild
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornMember
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornMessage
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornTextChannel
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornUser
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Event Handlers usando a Nashorn Engine
 */
class NashornEventHandler {
	var id = ObjectId() // Object ID único para cada comando
	var jsScriptName = "script-" + System.currentTimeMillis() // label do comando
	lateinit var javaScript: String // código em JS do comando
	var isEnabled = true // Se o comando está ativado
	var createdDate = LocalDateTime.now() // Data criada
	var editedDate = LocalDateTime.now() // Data editada
	var authors: List<String> = ArrayList() // Autores do comando (ou seja, quem mexeu)
	var isPublic = false // Se o comando é público no repositório de comandos
	var isForked = false // Se é uma cópia de outro comando na repo de cmds
	var upstreamId: ObjectId? = null // Caso seja forked, o upstreamId irá ter o Object ID original

	fun handleMessageReceived(event: MessageReceivedEvent) {
		if (!javaScript.contains("onMessageReceived"))
			return

		run("onMessageReceived", NashornMessageReceivedEvent(event))
	}

	fun handleMemberJoin(event: GuildMemberJoinEvent) {
		if (!javaScript.contains("onMemberJoin"))
			return

		run("onMemberJoin", NashornMemberJoinEvent(event))
	}

	fun handleMemberLeave(event: GuildMemberLeaveEvent) {
		if (!javaScript.contains("onMemberLeave"))
			return

		run("onMemberLeave", NashornMemberLeaveEvent(event))
	}

	fun run(call: String, vararg objects: Any) {
		val factory = NashornScriptEngineFactory()

		val engine = factory.getScriptEngine(NashornCommand.NashornClassFilter())
		// Funções que jamais poderão ser usadas em comandos
		val blacklisted = "var quit=function(){throw 'Operação não suportada: quit';};var exit=function(){throw 'Operação não suportada: exit';};var print=function(){throw 'Operação não suportada: print';};var echo=function(){throw 'Operação não suportada: echo';};var readLine=function(){throw 'Operação não suportada: readLine';};var readFully=function(){throw 'Operação não suportada: readFully';};var load=function(){throw 'Operação não suportada: load';};var loadWithNewGlobal=function(){throw 'Operação não suportada: loadWithNewGlobal';};"
		// Funções inline para facilitar a programação de comandos
		val inlineMethods = """var nashornUtils = Java.type("com.mrpowergamerbr.loritta.commands.nashorn.NashornUtils");
var loritta=function(){ return nashornUtils.loritta(); };"""

		val executor = Executors.newSingleThreadExecutor()
		val future = executor.submit(NashornEventTask(engine, "$blacklisted $inlineMethods\n$javaScript", call, *objects))
		future.get(15, TimeUnit.SECONDS)
	}

	class NashornMessageReceivedEvent(private val event: MessageReceivedEvent) {
		fun getGuild(): NashornGuild {
			return NashornGuild(event.guild)
		}

		fun getMember(): NashornMember {
			return NashornMember(event.member)
		}

		fun getMessage(): NashornMessage {
			return NashornMessage(event.message)
		}

		fun getAuthor(): NashornUser {
			return NashornUser(event.author)
		}

		fun getTextChannel(): NashornTextChannel {
			return NashornTextChannel(event.textChannel)
		}

		fun getMessageId(): String {
			return event.messageId
		}
	}

	class NashornMemberJoinEvent(private val event: GuildMemberJoinEvent) {
		fun getMember(): NashornMember {
			return NashornMember(event.member)
		}

		fun getGuild(): NashornGuild {
			return NashornGuild(event.guild)
		}
	}

	class NashornMemberLeaveEvent(private val event: GuildMemberLeaveEvent) {
		fun getMember(): NashornMember {
			return NashornMember(event.member)
		}

		fun getGuild(): NashornGuild {
			return NashornGuild(event.guild)
		}
	}
}