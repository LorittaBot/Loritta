package net.perfectdreams.commands.loritta

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.vanilla.misc.MagicPingCommand
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.remove
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.commands.dsl.BaseDSLCommand
import net.perfectdreams.commands.manager.CommandContinuationType
import net.perfectdreams.commands.manager.CommandManager
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class LorittaCommandManager(val loritta: Loritta) : CommandManager<CommandContext, LorittaCommand, BaseDSLCommand>() {
	val commands = mutableListOf<LorittaCommand>()

	init {
		registerCommand(MagicPingCommand())

		commandListeners.addThrowableListener { context, command, throwable ->
			if (throwable is CommandException) {
				context.reply(
						LoriReply(
								throwable.localizedMessage,
								throwable.prefix
						)
				)
				return@addThrowableListener CommandContinuationType.CANCEL
			}
			return@addThrowableListener CommandContinuationType.CONTINUE
		}
		contextManager.registerContext<BaseLocale>(
				{ clazz: KClass<*> -> clazz.isSubclassOf(BaseLocale::class) || clazz == BaseLocale::class },
				{ sender, clazz, stack ->
					sender.locale
				}
		)

		contextManager.registerContext<User>(
				{ clazz: KClass<*> -> clazz.isSubclassOf(User::class) || clazz == User::class },
				{ sender, clazz, stack ->
					val link = stack.pop() // Ok, será que isto é uma URL?

					println("user context: $link")

					// Vamos verificar por menções, uma menção do Discord é + ou - assim: <@123170274651668480>
					for (user in sender.message.mentionedUsers) {
						if (user.asMention == link.replace("!", "")) { // O replace é necessário já que usuários com nick tem ! no mention (?)
							// Diferente de null? Então vamos usar o avatar do usuário!
							return@registerContext user
						}
					}

					// Vamos tentar procurar pelo username + discriminator
					if (!sender.isPrivateChannel && !link.isEmpty()) {
						val split = link.split("#").dropLastWhile { it.isEmpty() }.toTypedArray()

						if (split.size == 2 && split[0].isNotEmpty()) {
							val matchedMember = sender.guild.getMembersByName(split[0], false).stream().filter { it -> it.user.discriminator == split[1] }.findFirst()

							if (matchedMember.isPresent) {
								return@registerContext matchedMember.get().user
							}
						}
					}

					// Ok então... se não é link e nem menção... Que tal então verificar por nome?
					if (!sender.isPrivateChannel && !link.isEmpty()) {
						val matchedMembers = sender.guild.getMembersByEffectiveName(link, true)

						if (!matchedMembers.isEmpty()) {
							return@registerContext matchedMembers[0].user
						}
					}

					// Se não, vamos procurar só pelo username mesmo
					if (!sender.isPrivateChannel && !link.isEmpty()) {
						val matchedMembers = sender.guild.getMembersByName(link, true)

						if (!matchedMembers.isEmpty()) {
							return@registerContext matchedMembers[0].user
						}
					}

					// Ok, então só pode ser um ID do Discord!
					try {
						val user = LorittaLauncher.loritta.lorittaShards.retrieveUserById(link)

						if (user != null) { // Pelo visto é!
							return@registerContext user
						}
					} catch (e: Exception) {
					}

					return@registerContext null
				}
		)
	}
	override fun getRegisteredCommands() = commands

	override fun registerCommand(command: LorittaCommand) {
		commands.add(command)
	}

	override fun unregisterCommand(command: LorittaCommand) {
		commands.remove(command)
	}

	suspend fun dispatch(ev: LorittaMessageEvent, conf: ServerConfig, locale: BaseLocale, lorittaUser: LorittaUser): Boolean {
		val rawMessage = ev.message.contentRaw

		// É necessário remover o new line para comandos como "+eval", etc
		val rawArguments = rawMessage.replace("\n", "").split(" ")

		// Primeiro os comandos vanilla da Loritta(tm)
		for (command in getRegisteredCommands()) {
			if (verifyAndDispatch(command, rawArguments, ev, conf, locale, lorittaUser))
				return true
		}

		return false
	}

	suspend fun verifyAndDispatch(command: LorittaCommand, rawArguments: List<String>, ev: LorittaMessageEvent, conf: ServerConfig, locale: BaseLocale, lorittaUser: LorittaUser): Boolean {
		for (subCommand in command.subcommands) {
			if (dispatch(subCommand as LorittaCommand, rawArguments.drop(1).toMutableList(), ev, conf, locale, lorittaUser, true))
				return true
		}

		if (dispatch(command, rawArguments, ev, conf, locale, lorittaUser, false))
			return true

		return false
	}

	suspend fun dispatch(command: LorittaCommand, rawArguments: List<String>, ev: LorittaMessageEvent, conf: ServerConfig, locale: BaseLocale, lorittaUser: LorittaUser, isSubcommand: Boolean): Boolean {
		val message = ev.message.contentDisplay
		val member = ev.message.member

		// Carregar as opções de comandos
		// val cmdOptions = conf.getCommandOptionsFor(command)
		val prefix = conf.commandPrefix

		val labels = command.labels.toMutableList()

		// println("Labels de $command: $labels")
		// if (cmdOptions.enableCustomAliases) // Adicionar labels customizadas no painel
		// 	labels.addAll(cmdOptions.aliases)

		// ignoreCase = true ~ Permite usar "+cOmAnDo"
		var valid = labels.any { rawArguments[0].equals(prefix + it, true) }
		var byMention = false

		if (!isSubcommand && rawArguments.getOrNull(1) != null && (rawArguments[0] == "<@${Loritta.config.clientId}>" || rawArguments[0] == "<@!${Loritta.config.clientId}>")) {
			// by mention
			valid = labels.any { rawArguments[1].equals(it, true) }
			byMention = true
		}

		// println("Vàlido? $valid $rawArguments[0]")

		if (valid) {
			var args = message.replace("@${ev.guild?.selfMember?.effectiveName ?: ""}", "").stripCodeMarks().split(" ").toTypedArray().remove(0)
			var rawArgs = ev.message.contentRaw.stripCodeMarks().split(" ").toTypedArray().remove(0)
			var strippedArgs = ev.message.contentStripped.stripCodeMarks().split(" ").toTypedArray().remove(0)
			if (byMention) {
				args = args.remove(0)
				rawArgs = rawArgs.remove(0)
				strippedArgs = strippedArgs.remove(0)
			}

			val context = CommandContext(conf, lorittaUser, locale, ev, command, args, rawArgs, strippedArgs)

			println("Executing $command with ${rawArgs.joinToString(", ")} ^-^")
			return execute(context, command, rawArgs)
		}
		return false
	}
}