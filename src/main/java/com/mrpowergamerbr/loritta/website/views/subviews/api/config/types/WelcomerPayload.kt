package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.dv8tion.jda.core.entities.Guild

class WelcomerPayload : ConfigPayloadType("welcomer") {
	override fun process(payload: JsonObject, serverConfig: ServerConfig, guild: Guild) {
		val serverListConfig = serverConfig.joinLeaveConfig
		serverListConfig.isEnabled = payload["isEnabled"].bool
		serverListConfig.tellOnJoin = payload["tellOnJoin"].bool
		serverListConfig.tellOnLeave = payload["tellOnLeave"].bool
		serverListConfig.tellOnPrivate = payload["tellOnPrivate"].bool
		serverListConfig.tellOnKick = payload["tellOnKick"].bool
		serverListConfig.tellOnBan = payload["tellOnBan"].bool
		serverListConfig.canalJoinId = payload["canalJoinId"].nullString
		serverListConfig.canalLeaveId = payload["canalLeaveId"].nullString
		serverListConfig.joinMessage = payload["joinMessage"].string
		serverListConfig.leaveMessage = payload["leaveMessage"].string
		serverListConfig.joinPrivateMessage = payload["joinPrivateMessage"].string
		serverListConfig.kickMessage = payload["kickMessage"].string
		serverListConfig.banMessage = payload["banMessage"].string
	}
}