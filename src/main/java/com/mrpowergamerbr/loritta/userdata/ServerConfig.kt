package com.mrpowergamerbr.loritta.userdata

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandOptions
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.GiveawayCommand.Giveaway
import com.mrpowergamerbr.loritta.listeners.nashorn.NashornEventHandler
import com.mrpowergamerbr.loritta.utils.loritta
import org.mongodb.morphia.annotations.Entity
import org.mongodb.morphia.annotations.Id
import org.mongodb.morphia.annotations.IndexOptions
import org.mongodb.morphia.annotations.Indexed
import java.util.*

@Entity(value = "servers", noClassnameStored = true)
class ServerConfig(
	@Id
	@Indexed(options = IndexOptions(unique = true))
	var guildId: String // Guild ID
) {
	constructor() : this("???")

	var commandPrefix = "+" // Command Prefix (example: +help or .help or etc)
	var disabledCommands = ArrayList<String>() // Comandos desativados
	var debugOptions = DebugOptions()
	var deleteMessageAfterCommand: Boolean = false // Deletar mensagem do comando após executar ele?
	var localeId = "default"

	var commandOptions = HashMap<String, CommandOptions>() // Command Options
	// Os command options são salvos assim:
	// AbstractCommand.getClass().getSimpleName() - CommandOptions

	// boolean warnOnFail; // Avisar ao usuário quando escrever o comando errado?
	var explainOnCommandRun = true // Explicar quando rodar *comando*? (Ou quando usar *comando* :shrug:)
	var explainInPrivate = false // Caso explainOnCommandRun estiver ativado, é para explicar APENAS no privado ou mandar no global?
	var commandOutputInPrivate = false // É para mandar o output (ou seja, tudo do comando) no privado em vez de mandar no global?
	var warnOnMissingPermission = false // Avisar quando a Loritta não tem permissão para falar em um canal específico
	var mentionOnCommandOutput = true // Caso esteja ativado, a Loritta irá marcar quem executou na mensagem resposta
	var warnOnUnknownCommand = false
	var blacklistedChannels = ArrayList<String>() // Canais em que os comandos são bloqueados
	var warnIfBlacklisted = false
	var blacklistWarning = "{@user} Você não pode usar comandos no {@channel}, bobinho(a)! <:blobBlush:357977010771066890>"
	var nashornCommands = ArrayList<NashornCommand>() // Comandos customizados

	var nashornEventHandlers = ArrayList<NashornEventHandler>()

	var joinLeaveConfig = JoinLeaveConfig()
	var musicConfig = MusicConfig()
	var aminoConfig = AminoConfig()
	var youTubeConfig = YouTubeConfig()
	var livestreamConfig = LivestreamConfig()
	var starboardConfig = StarboardConfig()
	var rssFeedConfig = RssFeedConfig()
	var eventLogConfig = EventLogConfig()
	var autoroleConfig = AutoroleConfig()
	var inviteBlockerConfig = InviteBlockerConfig()
	var permissionsConfig = PermissionsConfig()
	var slowModeChannels = HashMap<String, Int>() // Canais com SlowMode ativado
	var starboardEmbeds = HashMap<String, String>() // Quais mensagens correspondem a mensagens no starboard

	var userData = HashMap<String, LorittaServerUserData>()
	var guildUserData = mutableListOf<LorittaGuildUserData>()
	var migratedUserData = false

	fun getUserData(id: String): LorittaGuildUserData {
		var userData = guildUserData.firstOrNull { it.id == id }

		if (userData == null) {
			userData = LorittaGuildUserData(id)

			this.guildUserData.add(userData)
		}

		return userData
	}

	var temporaryBans = HashMap<String, Long>()

	var giveaways = ArrayList<Giveaway>()

	fun getCommandOptionsFor(cmd: AbstractCommand): CommandOptions {
		if (cmd is NashornCommand) { // Se é um comando feito em Nashorn...
			// Vamos retornar uma configuração padrão!
			return CommandOptions()
		}

		if (commandOptions.containsKey(cmd.javaClass.simpleName)) {
			return commandOptions[cmd.javaClass.simpleName]!!
		}

		if (loritta.commandManager.defaultCmdOptions.containsKey(cmd.javaClass.simpleName)) {
			return loritta.commandManager.defaultCmdOptions[cmd.javaClass.simpleName]!!.newInstance() as CommandOptions
		} else {
			return CommandOptions()
		}
	}

	class DebugOptions : CommandOptions() {
		var enableAllModules: Boolean = false // Caso ativado, TODAS as modules estarão ativadas
	}
}