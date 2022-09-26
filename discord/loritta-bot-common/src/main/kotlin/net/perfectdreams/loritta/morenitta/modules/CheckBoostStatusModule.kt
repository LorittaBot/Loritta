package net.perfectdreams.loritta.morenitta.modules

import com.github.salomonbrys.kotson.nullLong
import com.github.salomonbrys.kotson.obj
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.NitroBoostUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Payment
import net.perfectdreams.loritta.morenitta.tables.Payments
import net.perfectdreams.loritta.morenitta.utils.payments.PaymentGateway
import org.jetbrains.exposed.sql.and

class CheckBoostStatusModule(val loritta: LorittaBot) : MessageReceivedModule {
	val config = loritta.discordConfig.donatorsOstentation

	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val guildId = event.guild?.idLong ?: return false

		if (!config.boostEnabledGuilds.any { it.id == guildId })
			return false

		if (event.member?.timeBoosted == null)
			return false

		if (event.guild.boostCount > config.boostMax)
			return false

		return true
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val donations = loritta.pudding.transaction {
			Payment.find {
				(Payments.gateway eq PaymentGateway.NITRO_BOOST) and (Payments.userId eq event.author.idLong)
			}.toList()
		}

		var hasPaymentHere = false

		for (nitroBoostPayment in donations) {
			val metadata = nitroBoostPayment.metadata
			val isFromThisGuild = metadata != null && metadata.obj["guildId"].nullLong == event.guild!!.idLong

			if (isFromThisGuild) {
				hasPaymentHere = true
				break
			}
		}

		if (!hasPaymentHere)
			NitroBoostUtils.onBoostActivate(loritta, event.member!!)

		return false
	}
}