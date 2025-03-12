package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.PremiumTrackTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.respondBodyAsHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.twitch.GuildConfigureTwitchChannelView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.*
import java.time.Instant
import kotlin.math.ceil

class PutTwitchPremiumTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/twitch/premium-tracks") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		// This is the route that adds a NEW instance to the configuration
		val postParams = call.receiveParameters()
		val twitchUserId = postParams.getOrFail("twitchUserId").toLong()
		val trackId = postParams["trackId"]?.toLong()

		val result = loritta.newSuspendedTransaction {
			val isAlreadyAdded = PremiumTrackTwitchAccounts.selectAll().where {
				PremiumTrackTwitchAccounts.guildId eq guild.idLong and (PremiumTrackTwitchAccounts.twitchUserId eq twitchUserId)
			}.count() == 1L

			if (isAlreadyAdded)
				return@newSuspendedTransaction EnablePremiumTrackForTwitchChannelResult.AlreadyAdded

			val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()
				.sumOf { it.value }
				.let { ceil(it) }

			val plan = ServerPremiumPlans.getPlanFromValue(valueOfTheDonationKeysEnabledOnThisGuild)

			val premiumTracksOfTheGuildCount = PremiumTrackTwitchAccounts.select(PremiumTrackTwitchAccounts.twitchUserId).where { 
				PremiumTrackTwitchAccounts.guildId eq guild.idLong
			}.orderBy(PremiumTrackTwitchAccounts.addedAt, SortOrder.ASC) // Ordered by the added at date...
				.count()

			if (premiumTracksOfTheGuildCount >= plan.maxUnauthorizedTwitchChannels)
				return@newSuspendedTransaction EnablePremiumTrackForTwitchChannelResult.TooManyPremiumTracks

			PremiumTrackTwitchAccounts.insert {
				it[PremiumTrackTwitchAccounts.guildId] = guild.idLong
				it[PremiumTrackTwitchAccounts.twitchUserId] = twitchUserId
				it[PremiumTrackTwitchAccounts.addedBy] = userIdentification.id.toLong()
				it[PremiumTrackTwitchAccounts.addedAt] = Instant.now()
			}
			return@newSuspendedTransaction EnablePremiumTrackForTwitchChannelResult.Success
		}

		when (result) {
			EnablePremiumTrackForTwitchChannelResult.Success, EnablePremiumTrackForTwitchChannelResult.AlreadyAdded -> {
				call.response.headerHXTrigger {
					playSoundEffect = "config-success"
					showSpicyToast(
						EmbeddedSpicyToast.Type.SUCCESS,
						"Acompanhamento premium criado!"
					)
				}

				data class AddNewGuildTwitchChannelTransactionResult(
					val trackedTwitchAccount: ResultRow?,
					val valueOfTheDonationKeysEnabledOnThisGuild: Double,
					val premiumTracksCount: Long,
					val state: TwitchAccountTrackState
				)

				val transactionResult = loritta.transaction {
					val tracked = if (trackId != null) {
						TrackedTwitchAccounts.selectAll()
							.where {
								TrackedTwitchAccounts.id eq trackId and (TrackedTwitchAccounts.guildId eq guild.idLong)
							}
							.firstOrNull()
					} else {
						null
					}

					val state = TwitchWebUtils.getTwitchAccountTrackState(twitchUserId)

					val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
						.toList()
						.sumOf { it.value }
						.let { ceil(it) }

					val premiumTracksCount = PremiumTrackTwitchAccounts.selectAll().where {
						PremiumTrackTwitchAccounts.guildId eq guild.idLong
					}.count()

					AddNewGuildTwitchChannelTransactionResult(
						tracked,
						valueOfTheDonationKeysEnabledOnThisGuild,
						premiumTracksCount,
						state
					)
				}

				val twitchUser = TwitchWebUtils.getCachedUsersInfoById(loritta, twitchUserId)
					.first()

				call.respondHtml(
					GuildConfigureTwitchChannelView(
						loritta.newWebsite!!,
						i18nContext,
						locale,
						getPathWithoutLocale(call),
						loritta.getLegacyLocaleById(locale.id),
						userIdentification,
						UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
						colorTheme,
						guild,
						"twitch",
						trackId,
						false, // We don't need to create a premium track because we should have, hopefully, created one already
						twitchUser,
						transactionResult.state,
						if (transactionResult.trackedTwitchAccount != null)
							GuildConfigureTwitchChannelView.TwitchTrackSettings(
								transactionResult.trackedTwitchAccount[TrackedTwitchAccounts.channelId],
								transactionResult.trackedTwitchAccount[TrackedTwitchAccounts.message],
							)
						else
							GuildConfigureTwitchChannelView.TwitchTrackSettings(
								null,
								"Estou ao vivo jogando {stream.game}! **{stream.title}** {stream.url}"
							),
						ServerPremiumPlans.getPlanFromValue(transactionResult.valueOfTheDonationKeysEnabledOnThisGuild),
						transactionResult.premiumTracksCount
					).generateHtml()
				)
				return
			}

			EnablePremiumTrackForTwitchChannelResult.TooManyPremiumTracks -> {
				call.respondBodyAsHXTrigger(
					status = HttpStatusCode.Forbidden,
				) {
					playSoundEffect = "config-error"
					showSpicyToast(
						EmbeddedSpicyToast.Type.WARN,
						"Você está no limite de acompanhamentos premium!"
					)
				}
				return
			}
		}
	}

	sealed class EnablePremiumTrackForTwitchChannelResult {
		data object Success : EnablePremiumTrackForTwitchChannelResult()
		data object AlreadyAdded : EnablePremiumTrackForTwitchChannelResult()
		data object TooManyPremiumTracks : EnablePremiumTrackForTwitchChannelResult()
	}
}