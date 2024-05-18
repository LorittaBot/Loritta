package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.DailyShopView
import net.perfectdreams.loritta.serializable.*
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class DailyShopRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedDashboardRoute(loritta, "/dashboard/daily-shop") {
	override suspend fun onDashboardAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, colorTheme: ColorTheme) {
		val dreamStorageServiceNamespace = loritta.dreamStorageService.getCachedNamespaceOrRetrieve()

		val (profile, activeBackgroundId, shopId, generatedAt, backgroundsInShop, profileDesignsInShop, backgroundsWrapper, boughtProfileDesigns) = loritta.transaction {
			val shop = DailyShops.selectAll().orderBy(DailyShops.generatedAt, SortOrder.DESC).limit(1).first()

			val profile = loritta.getLorittaProfile(userIdentification.id.toLong())
			val activeBackgroundId = profile?.settings?.activeBackgroundInternalName?.value ?: Background.DEFAULT_BACKGROUND_ID

			val backgroundsInShopResults = (DailyShopItems innerJoin Backgrounds)
				.selectAll()
				.where {
					DailyShopItems.shop eq shop[DailyShops.id]
				}
				.toList()

			val backgroundsInShop = backgroundsInShopResults.map {
				DailyShopBackgroundEntry(
					BackgroundWithVariations(
						Background.fromRow(it),
						loritta.pudding.backgrounds.getBackgroundVariations(it[Backgrounds.internalName])
					),
					it[DailyShopItems.tag]
				)
			}

			val profileDesignsInShop = (DailyProfileShopItems innerJoin ProfileDesigns)
				.selectAll()
				.where {
					DailyProfileShopItems.shop eq shop[DailyShops.id]
				}
				.map {
					WebsiteUtils.fromProfileDesignToSerializable(loritta, it).also { profile ->
						profile.tag = it[DailyProfileShopItems.tag]
					}
				}
				.toList()

			val boughtBackgroundsResults = Backgrounds.selectAll().where {
				Backgrounds.internalName inList BackgroundPayments.selectAll().where {
					BackgroundPayments.userId eq userIdentification.id.toLong()
				}.map { it[BackgroundPayments.background].value }
			}.map {
				Background.fromRow(it)
			} + loritta.pudding.backgrounds.getBackground(net.perfectdreams.loritta.serializable.Background.DEFAULT_BACKGROUND_ID)!!.data // The default background should always exist

			// OPTIMIZATION: Bulk get user bought background variations to avoid multiple selects
			val queriedBackgroundVariations = BackgroundVariations
				.selectAll()
				.where { BackgroundVariations.background inList boughtBackgroundsResults.map { it.id } }
				.toList()

			val backgroundsWrapper = ProfileSectionsResponse.BackgroundsWrapper(
				loritta.dreamStorageService.baseUrl,
				dreamStorageServiceNamespace,
				loritta.config.loritta.etherealGambiService.url,
				boughtBackgroundsResults.map {
					BackgroundWithVariations(
						it,
						queriedBackgroundVariations.filter { variation -> variation[BackgroundVariations.background].value == it.id }
							.map { BackgroundVariation.fromRow(it) }
					)
				}
			)

			val boughtProfileDesignsResults = ProfileDesigns.select {
				ProfileDesigns.internalName inList ProfileDesignsPayments.select {
					ProfileDesignsPayments.userId eq userIdentification.id.toLong()
				}.map { it[ProfileDesignsPayments.profile].value }
			}

			val boughtProfileDesigns = boughtProfileDesignsResults.map {
				WebsiteUtils.toSerializable(
					loritta,
					ProfileDesign.wrapRow(it)
				)
			} + WebsiteUtils.toSerializable(
				loritta,
				ProfileDesign.findById(
					ProfileDesign.DEFAULT_PROFILE_DESIGN_ID
				)!!
			)

			Result(profile, activeBackgroundId, shop[DailyShops.id].value, shop[DailyShops.generatedAt], backgroundsInShop, profileDesignsInShop, backgroundsWrapper, boughtProfileDesigns)
		}

		val view = DailyShopView(
			loritta.newWebsite!!,
			i18nContext,
			locale,
			getPathWithoutLocale(call),
			loritta.getLegacyLocaleById(locale.id),
			userIdentification,
			UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
			colorTheme,
			profile,
			activeBackgroundId,
			shopId,
			DailyShopResult(
				loritta.dreamStorageService.baseUrl,
				loritta.dreamStorageService.getCachedNamespaceOrRetrieve(),
				loritta.config.loritta.etherealGambiService.url,
				backgroundsInShop,
				profileDesignsInShop,
				generatedAt
			),
			backgroundsWrapper,
			boughtProfileDesigns
		)

		call.respondHtml(
			view.generateHtml()
		)
	}

	override suspend fun onUnauthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
		if (call.request.header("User-Agent") == Constants.DISCORD_CRAWLER_USER_AGENT) {
			call.respondHtml(
				createHTML().html {
					head {
						fun setMetaProperty(property: String, content: String) {
							meta(content = content) { attributes["property"] = property }
						}
						title("Login • Loritta")
						setMetaProperty("og:site_name", "Loritta")
						setMetaProperty("og:title", "Loja Diária")
						setMetaProperty("og:description", "Bem-vind@ a loja diária de itens! O lugar para comprar itens para o seu \"+perfil\" da Loritta!\n\nTodo o dia as 00:00 UTC (21:00 no horário do Brasil) a loja é atualizada com novos itens! Então volte todo o dia para verificar ^-^")
						setMetaProperty("og:image", loritta.config.loritta.website.url + "assets/img/loritta_daily_shop.png")
						setMetaProperty("og:image:width", "320")
						setMetaProperty("og:ttl", "660")
						setMetaProperty("og:image:width", "320")
						setMetaProperty("theme-color", LorittaColors.LorittaAqua.toHex())
						meta("twitter:card", "summary_large_image")
					}
					body {
						p {
							+"Parabéns, você encontrou um easter egg!"
						}
					}
				}
			)
		}
		return super.onUnauthenticatedRequest(call, locale, i18nContext)
	}

	private data class Result(
		val profile: Profile?,
		val activeBackgroundId: String,
		val shopId: Long,
		val generatedAt: Long,
		val backgroundsInShop: List<DailyShopBackgroundEntry>,
		val profileDesignsInShop: List<net.perfectdreams.loritta.serializable.ProfileDesign>,
		val boughtBackgrounds: ProfileSectionsResponse.BackgroundsWrapper,
		val boughtProfileDesigns: List<net.perfectdreams.loritta.serializable.ProfileDesign>
	)
}