package com.mrpowergamerbr.loritta.userdata

import com.mrpowergamerbr.loritta.commands.nashorn.LegacyNashornCommand
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.listeners.nashorn.NashornEventHandler
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import java.util.*

class MongoServerConfig @BsonCreator constructor(
		@BsonProperty("_id")
		@get:[BsonIgnore]
		val guildId: String // Guild ID
) {
	// Migrated
	var disabledCommands = ArrayList<String>() // Comandos desativados

	// Os command options são salvos assim:
	// AbstractCommand.getClass().getSimpleName() - CommandOptions

	var nashornCommands = ArrayList<LegacyNashornCommand>() // Comandos customizados

	var nashornEventHandlers = ArrayList<NashornEventHandler>()

	// Migrated
	var joinLeaveConfig = WelcomerConfig()
	// Migrated
	var starboardConfig = StarboardConfig()
	// Migrated
	var eventLogConfig = EventLogConfig()
	// Migrated
	var autoroleConfig = AutoroleConfig()
	// Migrated
	var inviteBlockerConfig = InviteBlockerConfig()
	// Migrated
	var permissionsConfig = PermissionsConfig()
	// Migrated
	var moderationConfig = ModerationConfig()
	// Migrated
	var miscellaneousConfig = MiscellaneousConfig()
	// Migrated
	var textChannelConfigs = mutableListOf<TextChannelConfig>()
}