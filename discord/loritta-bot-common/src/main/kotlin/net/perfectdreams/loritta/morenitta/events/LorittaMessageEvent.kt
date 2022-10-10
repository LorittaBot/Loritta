package net.perfectdreams.loritta.morenitta.events

import dev.kord.common.entity.ChannelType
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class LorittaMessageEvent(
    val gateway: DeviousGateway,
    val author: User,
    val member: Member?,
    val message: Message,
    val messageId: String,
    val guild: Guild?,
    val channel: Channel,
    val serverConfig: ServerConfig,
    val locale: BaseLocale,
    val lorittaUser: LorittaUser
) {
	val deviousFun: DeviousFun get() = author.deviousFun

	fun isFromType(type: ChannelType): Boolean {
		return this.channel.type == type
	}
}