package net.perfectdreams.loritta.legacy.modules

import com.github.salomonbrys.kotson.nullLong
import com.github.salomonbrys.kotson.obj
import net.perfectdreams.loritta.legacy.dao.Profile
import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.legacy.events.LorittaMessageEvent
import net.perfectdreams.loritta.legacy.network.Databases
import net.perfectdreams.loritta.legacy.utils.LorittaUser
import net.perfectdreams.loritta.legacy.utils.NitroBoostUtils
import net.perfectdreams.loritta.legacy.utils.config.DonatorsOstentationConfig
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.dao.Payment
import net.perfectdreams.loritta.legacy.tables.Payments
import net.perfectdreams.loritta.legacy.utils.payments.PaymentGateway
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class CheckBoostStatusModule(val m: DonatorsOstentationConfig) : MessageReceivedModule {
	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val guildId = event.guild?.idLong ?: return false

		if (!m.boostEnabledGuilds.any { it.id == guildId })
			return false

		if (event.member?.timeBoosted == null)
			return false

		if (event.guild.boostCount > m.boostMax)
			return false

		return true
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val donations = transaction(Databases.loritta) {
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
			NitroBoostUtils.onBoostActivate(event.member!!)

		return false
	}
}