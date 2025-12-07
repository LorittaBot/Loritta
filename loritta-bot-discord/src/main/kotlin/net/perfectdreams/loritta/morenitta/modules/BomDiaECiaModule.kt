package net.perfectdreams.loritta.morenitta.modules

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.BomDiaECiaConfigs
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.BomDiaECia
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.MiscellaneousConfig
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class BomDiaECiaModule(val loritta: LorittaBot) : MessageReceivedModule {
	override suspend fun matches(
        event: LorittaMessageEvent,
        lorittaUser: LorittaUser,
        lorittaProfile: Profile?,
        serverConfig: ServerConfig,
        locale: BaseLocale,
        i18nContext: I18nContext
    ): Boolean {
        val bomDiaECiaConfig = loritta.transaction {
            BomDiaECiaConfigs.selectAll()
                .where { BomDiaECiaConfigs.id eq event.guild!!.idLong and (BomDiaECiaConfigs.enabled eq true) }
                .firstOrNull()
        }
		return bomDiaECiaConfig != null
	}

	override suspend fun handle(
		event: LorittaMessageEvent,
		lorittaUser: LorittaUser,
		lorittaProfile: Profile?,
		serverConfig: ServerConfig,
		locale: BaseLocale,
		i18nContext: I18nContext
	): Boolean {
        val bomDiaECiaConfig = loritta.transaction {
            BomDiaECiaConfigs.selectAll()
                .where { BomDiaECiaConfigs.id eq event.guild!!.idLong and (BomDiaECiaConfigs.enabled eq true) }
                .firstOrNull()
        }
        if (bomDiaECiaConfig == null) return false

        if (bomDiaECiaConfig[BomDiaECiaConfigs.useBlockedChannelsAsAllowedChannels]) {
            if (event.channel.idLong !in bomDiaECiaConfig[BomDiaECiaConfigs.blockedChannels])
                return false
        } else {
            if (event.channel.idLong in bomDiaECiaConfig[BomDiaECiaConfigs.blockedChannels])
                return false
        }

		val activeTextChannelInfo = loritta.bomDiaECia.activeTextChannels.getOrDefault(event.channel.id, BomDiaECia.YudiTextChannelInfo(serverConfig.commandPrefix))
		activeTextChannelInfo.lastMessageSent = System.currentTimeMillis()
		activeTextChannelInfo.users.add(event.author)
		loritta.bomDiaECia.activeTextChannels[event.channel.id] = activeTextChannelInfo

		return false
	}
}