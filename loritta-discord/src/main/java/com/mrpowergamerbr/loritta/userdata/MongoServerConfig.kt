package com.mrpowergamerbr.loritta.userdata

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
	// var disabledCommands = ArrayList<String>() // Comandos desativados

	// Os command options são salvos assim:
	// AbstractCommand.getClass().getSimpleName() - CommandOptions

	// var nashornCommands = ArrayList<NashornCommand>() // Comandos customizados

	var nashornEventHandlers = ArrayList<NashornEventHandler>()

	// var joinLeaveConfig = WelcomerConfig()
	// var starboardConfig = StarboardConfig()
	// var eventLogConfig = EventLogConfig()
	// var autoroleConfig = AutoroleConfig()
	// var inviteBlockerConfig = InviteBlockerConfig()
	// var permissionsConfig = PermissionsConfig()
	var moderationConfig = ModerationConfig()
	// var serverListConfig = ServerListConfig()
	// var miscellaneousConfig = MiscellaneousConfig()
	// var defaultTextChannelConfig = TextChannelConfig("default")
	// var textChannelConfigs = mutableListOf<TextChannelConfig>()
}