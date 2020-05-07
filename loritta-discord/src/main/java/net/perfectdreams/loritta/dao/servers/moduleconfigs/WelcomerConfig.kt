package net.perfectdreams.loritta.dao.servers.moduleconfigs

import net.perfectdreams.loritta.tables.servers.moduleconfigs.WelcomerConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class WelcomerConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, WelcomerConfig>(WelcomerConfigs)

	var tellOnJoin by WelcomerConfigs.tellOnJoin
	var channelJoinId by WelcomerConfigs.channelJoinId
	var joinMessage by WelcomerConfigs.joinMessage
	var deleteJoinMessagesAfter by WelcomerConfigs.deleteJoinMessagesAfter

	var tellOnRemove by WelcomerConfigs.tellOnRemove
	var channelRemoveId by WelcomerConfigs.channelRemoveId
	var removeMessage by WelcomerConfigs.removeMessage
	var deleteRemoveMessagesAfter by WelcomerConfigs.deleteRemoveMessagesAfter

	var tellOnPrivateJoin by WelcomerConfigs.tellOnPrivateJoin
	var joinPrivateMessage by WelcomerConfigs.joinPrivateMessage

	var tellOnBan by WelcomerConfigs.tellOnBan
	var bannedMessage by WelcomerConfigs.bannedMessage
}