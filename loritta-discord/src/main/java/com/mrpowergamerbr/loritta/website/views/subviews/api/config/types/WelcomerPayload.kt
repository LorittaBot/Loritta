package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.nullString
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.dao.servers.moduleconfigs.WelcomerConfig
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction

class WelcomerPayload : ConfigPayloadType("welcomer") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		val isEnabled = payload["isEnabled"].bool
		val tellOnJoin = payload["tellOnJoin"].bool
		val channelJoinId = payload["canalJoinId"].long
		val joinMessage = payload["joinMessage"].nullString
		val deleteJoinMessagesAfter = payload["deleteJoinMessagesAfter"].long

		val tellOnRemove = payload["tellOnLeave"].bool
		val channelRemoveId = payload["canalLeaveId"].long
		val removeMessage = payload["leaveMessage"].nullString
		val deleteRemoveMessagesAfter = payload["deleteLeaveMessagesAfter"].long

		val tellOnBan = payload["tellOnBan"].bool
		val banMessage = payload["banMessage"].nullString

		val tellOnPrivate = payload["tellOnBan"].bool
		val joinPrivateMessage = payload["joinPrivateMessage"].nullString

		transaction(Databases.loritta) {
			val welcomerConfig = serverConfig.welcomerConfig

			if (!isEnabled) {
				serverConfig.welcomerConfig = null
				welcomerConfig?.delete()
			} else {
				val newConfig = welcomerConfig ?: WelcomerConfig.new {
					this.tellOnJoin = tellOnJoin
					this.channelJoinId = channelJoinId
					this.joinMessage = joinMessage
					this.deleteJoinMessagesAfter = deleteJoinMessagesAfter
					this.tellOnRemove = tellOnRemove
					this.channelRemoveId = channelRemoveId
					this.removeMessage = removeMessage
					this.deleteRemoveMessagesAfter = deleteRemoveMessagesAfter
					this.tellOnBan = tellOnBan
					this.bannedMessage = banMessage
					this.tellOnPrivateJoin = tellOnPrivate
					this.joinPrivateMessage = joinPrivateMessage
				}

				newConfig.tellOnJoin = tellOnJoin
				newConfig.channelJoinId = channelJoinId
				newConfig.joinMessage = joinMessage
				newConfig.deleteJoinMessagesAfter = deleteJoinMessagesAfter
				newConfig.tellOnRemove = tellOnRemove
				newConfig.channelRemoveId = channelRemoveId
				newConfig.removeMessage = removeMessage
				newConfig.deleteRemoveMessagesAfter = deleteRemoveMessagesAfter
				newConfig.tellOnBan = tellOnBan
				newConfig.bannedMessage = banMessage
				newConfig.tellOnPrivateJoin = tellOnPrivate
				newConfig.joinPrivateMessage = joinPrivateMessage

				serverConfig.welcomerConfig = newConfig
			}
		}
	}
}