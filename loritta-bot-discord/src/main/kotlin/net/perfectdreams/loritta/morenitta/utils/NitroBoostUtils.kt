package net.perfectdreams.loritta.morenitta.utils

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserPremiumKeys
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.*
import java.time.OffsetDateTime
import java.time.ZoneOffset

object NitroBoostUtils {
	private val logger by HarmonyLoggerFactory.logger {}

	suspend fun onBoostActivate(loritta: LorittaBot, member: Member) {
		val guild = member.guild

		logger.info { "Enabling donation features via boost for $member in $guild!" }

		loritta.newSuspendedTransaction {
			// Gerar key de doação (UserPremiumKeys)
			UserPremiumKeys.insert {
				it[UserPremiumKeys.userId] = member.idLong
				it[UserPremiumKeys.value] = 13
				// We don't use MAX because PostgreSQL hates it >:(
				it[UserPremiumKeys.expiresAt] = OffsetDateTime.of(9999, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)
				it[UserPremiumKeys.metadata] = jsonObject(
					"guildId" to member.guild.idLong
				).toString()
			}
		}

		// Fim!
		try {
			loritta.getOrRetrievePrivateChannelForUser(member.user).sendMessageEmbeds(
				EmbedBuilder()
					.setTitle("Obrigada por ativar o seu boost! ${Emotes.LORI_HAPPY}")
					.setDescription(
						"Obrigada por ativar o seu Nitro Boost no meu servidor! ${Emotes.LORI_NITRO_BOOST}\n\nA cada dia eu estou mais próxima de virar uma digital influencer de sucesso, graças a sua ajuda! ${Emotes.LORI_HAPPY}\n\nAh, e como agradecimento por você ter ativado o seu boost no meu servidor, você irá receber todas as minhas vantagens de quem doa 19,99 reais! (Até você desativar o seu boost... espero que você não desative... ${Emotes.LORI_CRYING})\n\nContinue sendo incrível!"
					)
					.setImage("https://stuff.loritta.website/loritta-boost-raspoza.png")
					.setColor(Constants.LORITTA_AQUA)
					.build()
			).await()
		} catch (e: Exception) {}
	}

	suspend fun onBoostDeactivate(loritta: LorittaBot, member: Member) {
		val guild = member.guild

		logger.info { "Disabling donation features via boost for $member in $guild!"}

		loritta.newSuspendedTransaction {
			// Find the UserPremiumKey for this boost
			val boostKey = UserPremiumKeys.selectAll().where {
				(UserPremiumKeys.userId eq member.idLong) and (UserPremiumKeys.value eq 13)
			}.toList().firstOrNull {
				val metadata = it[UserPremiumKeys.metadata]?.let { JsonParser.parseString(it) }
				metadata != null && metadata.obj["guildId"].nullLong == guild.idLong
			}

			if (boostKey != null) {
				UserPremiumKeys.update({ UserPremiumKeys.id eq boostKey[UserPremiumKeys.id] }) {
					it[UserPremiumKeys.expiresAt] = OffsetDateTime.now()
				}
			}
		}
	}
}
