package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.p
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.PremiumTrackTwitchAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXPushURL
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.respondBodyAsHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.twitch.GuildConfigureTwitchChannelView
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import kotlin.math.ceil

class GetAddByLoginTwitchTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/twitch/add-login") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		data class AddNewGuildTwitchChannelTransactionResult(
			val valueOfTheDonationKeysEnabledOnThisGuild: Double,
			val premiumTracksCount: Long,
			val state: TwitchAccountTrackState
		)

		val login = call.parameters.getOrFail("login")
		// Attempting to add someone via their login!
		val loginStripped = login.removePrefix("https://")
			.removePrefix("http://")
			.removePrefix("www.")
			.removePrefix("twitch.tv/")

		val twitchUser = TwitchWebUtils.getCachedUsersInfoByLogin(loritta, loginStripped)
			.firstOrNull()

		if (twitchUser == null) {
			call.respondBodyAsHXTrigger(
				status = HttpStatusCode.BadRequest
			) {
				playSoundEffect = "config-error"
				showSpicyToast(
					EmbeddedSpicyToast.Type.WARN,
					"Canal não existe!",
				)
			}
			return
		}

		val transactionResult = loritta.transaction {
			val state = TwitchWebUtils.getTwitchAccountTrackState(twitchUser.id)

			val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()
				.sumOf { it.value }
				.let { ceil(it) }

			val premiumTracksCount = PremiumTrackTwitchAccounts.selectAll().where {
				PremiumTrackTwitchAccounts.guildId eq guild.idLong
			}.count()

			AddNewGuildTwitchChannelTransactionResult(
				valueOfTheDonationKeysEnabledOnThisGuild,
				premiumTracksCount,
				state
			)
		}
		when (transactionResult.state) {
			TwitchAccountTrackState.AUTHORIZED, TwitchAccountTrackState.ALWAYS_TRACK_USER, TwitchAccountTrackState.PREMIUM_TRACK_USER -> {
				call.response.headerHXPushURL("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/twitch/tracks/add?twitchUserId=${twitchUser.id}&createPremiumTrack=false")

				call.response.headerHXTrigger {
					closeSpicyModal = true
					showSpicyToast(
						EmbeddedSpicyToast.Type.SUCCESS,
						"Canal encontrado!",
					)
				}

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
						null,
						false,
						twitchUser,
						transactionResult.state,
						GuildConfigureTwitchChannelView.TwitchTrackSettings(
							null,
							"Estou ao vivo jogando {stream.game}! **{stream.title}** {stream.url}"
						),
						ServerPremiumPlans.getPlanFromValue(transactionResult.valueOfTheDonationKeysEnabledOnThisGuild),
						transactionResult.premiumTracksCount
					).generateHtml()
				)
			}

			TwitchAccountTrackState.UNAUTHORIZED -> {
				val plan = ServerPremiumPlans.getPlanFromValue(transactionResult.valueOfTheDonationKeysEnabledOnThisGuild)
				val premiumTrackTwitchAccounts = transactionResult.premiumTracksCount

				if (plan.maxUnauthorizedTwitchChannels > premiumTrackTwitchAccounts) {
					call.respondBodyAsHXTrigger(
						status = HttpStatusCode.PaymentRequired
					) {
						showSpicyModal(
							"Conta não autorizada, mas...",
							true,
							{
								div {
									p {
										text("A conta que você deseja adicionar não está autorizada na Loritta, mas você tem plano premium!")
									}

									p {
										text("Você pode seguir até ${plan.maxUnauthorizedTwitchChannels} contas que não foram autorizadas. Ao autorizar uma conta, outras pessoas podem seguir a conta sem precisar de plano premium, até você remover a conta da sua lista de acompanhamentos premium.")
									}
								}
							},
							listOf(
								{
									defaultModalCloseButton(i18nContext)
								},
								{
									classes += "primary"

									attributes["hx-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/twitch/add"
									attributes["hx-push-url"] = "true"
									attributes["hx-disabled-elt"] = "this"
									attributes["hx-vals"] = buildJsonObject {
										put("twitchUserId", twitchUser.id.toString())
										put("createPremiumTrack", true)
									}.toString()
									// show:top - Scroll to the top
									// settle:0ms - We don't want the settle animation beccause it is a full page swap
									// swap:0ms - We don't want the swap animation because it is a full page swap
									attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
									attributes["hx-select"] = "#right-sidebar-contents"
									attributes["hx-target"] = "#right-sidebar-contents"

									htmxDiscordLikeLoadingButtonSetup(
										i18nContext,
									) {
										text("Acompanhar de Forma Premium")
									}
								}
							)
						)
					}
				} else {
					call.respondBodyAsHXTrigger(
						HttpStatusCode.BadRequest
					) {
						playSoundEffect = "config-error"
						showSpicyModal(
							"Conta não autorizada",
							true,
							{
								div {
									p {
										text("A conta que você deseja adicionar não está autorizada na Loritta!")
									}

									p {
										text("Devido a limitações da Twitch, cada solicitação de notificação de livestream custa pontos, exceto se o dono da conta autorizar. Se fosse possível adicionar qualquer conta sem autorização, nós iriamos chegar no limite de solicitações rapidinho, assim não seria possível adicionar novas contas no painel...")
									}

									p {
										text("Peça para o dono da conta autorizar a conta dela na Loritta, ou compre plano premium na Loritta para poder adicionar contas não autorizadas!")
									}
								}
							},
							listOf {
								defaultModalCloseButton(i18nContext)
							}
						)
					}
				}
			}
		}
	}
}