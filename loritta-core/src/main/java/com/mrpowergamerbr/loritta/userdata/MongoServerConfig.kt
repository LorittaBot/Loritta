package com.mrpowergamerbr.loritta.userdata

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandManager
import com.mrpowergamerbr.loritta.commands.CommandOptions
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.listeners.nashorn.NashornEventHandler
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.entities.TextChannel
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.*

class MongoServerConfig @BsonCreator constructor(
		@BsonProperty("_id")
		@get:[BsonIgnore]
		val guildId: String // Guild ID
) {
	var commandPrefix = "+" // Command Prefix (example: +help or .help or etc)
	var disabledCommands = ArrayList<String>() // Comandos desativados
	var deleteMessageAfterCommand = false // Deletar mensagem do comando após executar ele?
	var localeId = "default"

	var commandOptions = HashMap<String, CommandOptions>() // Command Options
	// Os command options são salvos assim:
	// AbstractCommand.getClass().getSimpleName() - CommandOptions

	var explainOnCommandRun = true // Explicar quando rodar *comando*? (Ou quando usar *comando* :shrug:)
	var explainInPrivate = false // Caso explainOnCommandRun estiver ativado, é para explicar APENAS no privado ou mandar no global?
	var commandOutputInPrivate = false // É para mandar o output (ou seja, tudo do comando) no privado em vez de mandar no global?
	var warnOnMissingPermission = false // Avisar quando a Loritta não tem permissão para falar em um canal específico
	var mentionOnCommandOutput = true // Caso esteja ativado, a Loritta irá marcar quem executou na mensagem resposta
	var warnOnUnknownCommand = false
	var blacklistedChannels = ArrayList<String>() // Canais em que os comandos são bloqueados
	var warnIfBlacklisted = false
	var deleteMessagesAfter: Long? = null
	var blacklistWarning = "{@user} Você não pode usar comandos no {@channel}, bobinho(a)!"
	var nashornCommands = ArrayList<NashornCommand>() // Comandos customizados

	var nashornEventHandlers = ArrayList<NashornEventHandler>()

	var joinLeaveConfig = WelcomerConfig()
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
	var moderationConfig = ModerationConfig()
	var serverListConfig = ServerListConfig()
	// var economyConfig = EconomyConfig()
	var miscellaneousConfig = MiscellaneousConfig()
	var defaultTextChannelConfig = TextChannelConfig("default")
	var textChannelConfigs = mutableListOf<TextChannelConfig>()

	var lastCommandReceivedAt = 0L
	var apiKey: String? = null
	var premiumKey: String? = null

	fun getUserData(id: Long): GuildProfile {
		val t = this
		return transaction(Databases.loritta) {
			GuildProfile.find { (GuildProfiles.guildId eq guildId.toLong()) and (GuildProfiles.userId eq id) }.firstOrNull() ?: GuildProfile.new {
				this.guildId = t.guildId.toLong()
				this.userId = id
				this.money = BigDecimal(0)
				this.quickPunishment = false
				this.xp = 0
			}
		}
	}

	fun getTextChannelConfig(textChannel: TextChannel): TextChannelConfig {
		return getTextChannelConfig(textChannel.id)
	}

	fun getTextChannelConfig(id: String): TextChannelConfig {
		return textChannelConfigs.firstOrNull { it.id == id } ?: defaultTextChannelConfig
	}

	fun hasTextChannelConfig(textChannel: TextChannel): Boolean {
		return hasTextChannelConfig(textChannel.id)
	}

	fun hasTextChannelConfig(id: String): Boolean {
		return textChannelConfigs.firstOrNull { it.id == id } != null
	}

	fun getCommandOptionsFor(cmd: AbstractCommand): CommandOptions {
		if (cmd is NashornCommand) { // Se é um comando feito em Nashorn...
			// Vamos retornar uma configuração padrão!
			return CommandManager.DEFAULT_COMMAND_OPTIONS
		}

		val simpleName = cmd.javaClass.simpleName
		return when {
			// Se a configuração do servidor tem opções de comandos...
			commandOptions.containsKey(simpleName) -> commandOptions[simpleName]!!
			// Se as opções padrões de comandos possui uma opção "específica" para o comando
			loritta.legacyCommandManager.defaultCmdOptions.containsKey(simpleName) -> loritta.legacyCommandManager.defaultCmdOptions[simpleName]!!.newInstance() as CommandOptions
			// Se não, retorne as opções padrões
			else -> CommandManager.DEFAULT_COMMAND_OPTIONS
		}
	}
}