package net.perfectdreams.loritta.plugin.donatorsostentation.modules

import com.github.salomonbrys.kotson.nullLong
import com.github.salomonbrys.kotson.obj
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.plugin.donatorsostentation.DonatorsOstentationConfig
import net.perfectdreams.loritta.plugin.donatorsostentation.NitroBoostUtils
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.payments.PaymentGateway
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class CheckBoostStatusModule(val m: DonatorsOstentationConfig) : MessageReceivedModule {
	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val guildId = event.guild?.idLong ?: return false

		if (!m.boostEnabledGuilds.any { it.id == guildId })
			return false

		if (event.member?.timeBoosted == null)
			return false

		if (event.guild!!.boostCount > m.boostMax)
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