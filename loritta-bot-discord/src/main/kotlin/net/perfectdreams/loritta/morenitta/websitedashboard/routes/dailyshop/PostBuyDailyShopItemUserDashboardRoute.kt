package net.perfectdreams.loritta.morenitta.websitedashboard.routes.dailyshop

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.galleryofdreams.common.data.DiscordSocialConnection
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dailyShopItems
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class PostBuyDailyShopItemUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/daily-shop/buy") {
    @Serializable
    data class BuyDailyShopItemRequest(
        val type: String,
        val internalName: String,
    )

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val dreamStorageServiceNamespace = website.loritta.dreamStorageService.getCachedNamespaceOrRetrieve()

        // Hacky!
        val locale = website.loritta.localeManager.getLocaleById("default")

        // A bit hacky but hey, there's nothing a lot we can do rn
        val galleryOfDreamsResponse = website.loritta.cachedGalleryOfDreamsDataResponse!!

        val profile = website.loritta.getOrCreateLorittaProfile(session.userId)

        val request = Json.decodeFromString<BuyDailyShopItemRequest>(call.receiveText())
        val type = request.type
        val internalName = request.internalName

        val result = website.loritta.newSuspendedTransaction {
            if (type == "background") {
                val backgrounds = run {
                    val shop = DailyShops.selectAll().orderBy(DailyShops.generatedAt, SortOrder.DESC).limit(1).first()

                    (DailyShopItems innerJoin Backgrounds)
                        .selectAll()
                        .where {
                            DailyShopItems.shop eq shop[DailyShops.id]
                        }
                }

                val background = backgrounds.firstOrNull { it[Backgrounds.id].value == internalName }
                if (background == null)
                    return@newSuspendedTransaction Result.ItemNotInItemShop

                val cost = background[Backgrounds.rarity].getBackgroundPrice()
                if (cost > profile.money)
                    return@newSuspendedTransaction Result.NotEnoughSonhos

                val alreadyBoughtTheBackground = BackgroundPayments.selectAll().where {
                    BackgroundPayments.userId eq profile.userId and (BackgroundPayments.background eq background[Backgrounds.id])
                }.count() != 0L

                if (alreadyBoughtTheBackground)
                    return@newSuspendedTransaction Result.YouAlreadyHaveThisItem

                profile.takeSonhosAndAddToTransactionLogNested(
                    cost.toLong(),
                    SonhosPaymentReason.BACKGROUND
                )

                BackgroundPayments.insert {
                    it[userId] = profile.userId
                    it[BackgroundPayments.background] = background[Backgrounds.id]
                    it[boughtAt] = System.currentTimeMillis()
                    it[BackgroundPayments.cost] = cost.toLong()
                }

                // Cinnamon transaction system
                SimpleSonhosTransactionsLogUtils.insert(
                    profile.userId,
                    Instant.now(),
                    TransactionType.LORITTA_ITEM_SHOP,
                    cost.toLong(),
                    StoredLorittaItemShopBoughtBackgroundTransaction(background[Backgrounds.id].value)
                )

                val createdBy = background[Backgrounds.createdBy]
                val creatorReceived = (cost.toDouble() * 0.1).toLong()
                for (creatorId in createdBy) {
                    val author = galleryOfDreamsResponse.artists.firstOrNull { it.slug == creatorId } ?: continue

                    val discordId = author.socialConnections.filterIsInstance<DiscordSocialConnection>().firstOrNull()?.id ?: continue

                    val creator = website.loritta.getOrCreateLorittaProfile(discordId)

                    creator.addSonhosAndAddToTransactionLogNested(
                        creatorReceived,
                        SonhosPaymentReason.BACKGROUND
                    )

                    // Cinnamon transaction system
                    SimpleSonhosTransactionsLogUtils.insert(
                        creator.userId,
                        Instant.now(),
                        TransactionType.LORITTA_ITEM_SHOP,
                        creatorReceived,
                        StoredLorittaItemShopComissionBackgroundTransaction(
                            profile.userId,
                            background[Backgrounds.id].value
                        )
                    )
                }

                return@newSuspendedTransaction Result.Success(
                    DashboardDailyShopUtils.queryDailyShopResult(
                        website.loritta,
                        session.userId,
                        dreamStorageServiceNamespace
                    )
                )
            } else if (type == "profileDesign") {
                val backgrounds = run {
                    val shop = DailyShops.selectAll().orderBy(DailyShops.generatedAt, SortOrder.DESC).limit(1).first()

                    (DailyProfileShopItems innerJoin ProfileDesigns)
                        .selectAll()
                        .where {
                            DailyProfileShopItems.shop eq shop[DailyShops.id]
                        }
                }

                val background = backgrounds.firstOrNull { it[ProfileDesigns.id].value == internalName }
                if (background == null)
                    return@newSuspendedTransaction Result.ItemNotInItemShop

                val cost = background[ProfileDesigns.rarity].getProfilePrice()
                if (cost > profile.money)
                    return@newSuspendedTransaction Result.NotEnoughSonhos

                val alreadyBoughtTheBackground = ProfileDesignsPayments.selectAll().where {
                    ProfileDesignsPayments.userId eq profile.userId and (ProfileDesignsPayments.profile eq background[ProfileDesigns.id])
                }.count() != 0L

                if (alreadyBoughtTheBackground)
                    return@newSuspendedTransaction Result.YouAlreadyHaveThisItem

                profile.takeSonhosAndAddToTransactionLogNested(
                    cost.toLong(),
                    SonhosPaymentReason.PROFILE
                )

                ProfileDesignsPayments.insert {
                    it[userId] = profile.userId
                    it[ProfileDesignsPayments.profile] = background[ProfileDesigns.id]
                    it[boughtAt] = System.currentTimeMillis()
                    it[ProfileDesignsPayments.cost] = cost.toLong()
                }

                // Cinnamon transaction system
                SimpleSonhosTransactionsLogUtils.insert(
                    profile.userId,
                    Instant.now(),
                    TransactionType.LORITTA_ITEM_SHOP,
                    cost.toLong(),
                    StoredLorittaItemShopBoughtProfileDesignTransaction(background[ProfileDesigns.id].value)
                )

                val createdBy = background[ProfileDesigns.createdBy]
                val creatorReceived = (cost.toDouble() * 0.1).toLong()
                for (creatorId in createdBy) {
                    val author = galleryOfDreamsResponse.artists.firstOrNull { it.slug == creatorId } ?: continue

                    val discordId = author.socialConnections.filterIsInstance<DiscordSocialConnection>().firstOrNull()?.id ?: continue

                    val creator = website.loritta.getOrCreateLorittaProfile(discordId)

                    creator.addSonhosAndAddToTransactionLogNested(
                        creatorReceived,
                        SonhosPaymentReason.PROFILE
                    )

                    // Cinnamon transaction system
                    SimpleSonhosTransactionsLogUtils.insert(
                        creator.userId,
                        Instant.now(),
                        TransactionType.LORITTA_ITEM_SHOP,
                        creatorReceived,
                        StoredLorittaItemShopComissionProfileDesignTransaction(
                            profile.userId,
                            background[ProfileDesigns.id].value
                        )
                    )
                }

                return@newSuspendedTransaction Result.Success(
                    DashboardDailyShopUtils.queryDailyShopResult(
                        website.loritta,
                        session.userId,
                        dreamStorageServiceNamespace
                    )
                )
            } else error("Unsupported item shop type $type")
        }

        when (result) {
            Result.ItemNotInItemShop -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissCloseModal()
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Item fora de rotação",
                            {
                                text("Vixe, parece que o item saiu da rotação diária da loja bem na hora que você foi comprar!")
                            }
                        )
                    )
                }
            }
            Result.NotEnoughSonhos -> {
                call.respondHtmlFragment(status = HttpStatusCode.PaymentRequired) {
                    blissCloseModal()
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você não tem sonhos suficientes para comprar este item!",
                            null
                        )
                    )
                }
            }
            Result.YouAlreadyHaveThisItem -> {
                call.respondHtmlFragment(status = HttpStatusCode.Conflict) {
                    blissCloseModal()
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você já tem este item!",
                            null
                        )
                    )
                }
            }
            is Result.Success -> {
                call.respondHtmlFragment(status = HttpStatusCode.OK) {
                    blissCloseModal()
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.SUCCESS,
                            "Item comprado!",
                            null
                        )
                    )

                    dailyShopItems(i18nContext, locale, result.dailyShopResult, galleryOfDreamsResponse)
                }
            }
        }
    }

    private sealed class Result {
        data object ItemNotInItemShop : Result()
        data object YouAlreadyHaveThisItem : Result()
        data object NotEnoughSonhos : Result()
        data class Success(val dailyShopResult: DailyShopResult) : Result()
    }
}