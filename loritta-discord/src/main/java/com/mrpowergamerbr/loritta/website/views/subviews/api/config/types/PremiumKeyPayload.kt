package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.http.HttpStatusCode
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction

class PremiumKeyPayload : ConfigPayloadType("premium") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		val keyId = payload["keyId"].string

		val donationKey = transaction(Databases.loritta) {
			DonationKey.findById(keyId.toLong())
		} ?: throw WebsiteAPIException(HttpStatusCode.Forbidden,
				WebsiteUtils.createErrorPayload(
						LoriWebCode.FORBIDDEN,
						"loritta.errors.keyDoesntExist"
				)
		)

		if (donationKey.userId != userIdentification.id.toLong())
			throw WebsiteAPIException(HttpStatusCode.Forbidden,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.FORBIDDEN,
							"loritta.errors.tryingToApplyKeyOfAnotherUser"
					)
			)

		/* transaction(Databases.loritta) {
			// Desativar a key em outros servidores
			ServerConfigs.update({ ServerConfigs.donationKey eq donationKey.id }) {
				it[ServerConfigs.donationKey] = null
			}

			// Atualizar a key atual
			serverConfig.donationKey = donationKey
		} */
	}
}