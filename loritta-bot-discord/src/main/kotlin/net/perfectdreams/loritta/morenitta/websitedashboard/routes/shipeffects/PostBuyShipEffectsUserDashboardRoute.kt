package net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.html.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.luna.modals.EmbeddedModal
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.shipEffectsBribes
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseAllModals
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissSoundEffect
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.ShipEffect
import net.perfectdreams.loritta.serializable.StoredShipEffectSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import kotlin.time.Duration.Companion.days

class PostBuyShipEffectsUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/ship-effects/buy") {
    @Serializable
    data class BuyShipEffectsRequest(
        val receivingEffectUserId: Long,
        val shipPercentage: Int,
    )

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val request = Json.decodeFromString<BuyShipEffectsRequest>(call.receiveText())

        if (request.shipPercentage !in 0..100) {
            call.response.header("SpicyMorenitta-Use-Response-As-HXTrigger", "true")
            call.respondJson(
                buildJsonObject {
                    put("playSoundEffect", "config-error")
                    put("closeSpicyModal", null)
                    put(
                        "showSpicyToast",
                        EmbeddedSpicyModalUtils.encodeURIComponent(
                            Json.encodeToString(
                                EmbeddedSpicyToast(EmbeddedSpicyToast.Type.WARN, "O valor do ship precisa estar entre 0% e 100%!", null)
                            )
                        )
                    )
                }.toString(),
                status = HttpStatusCode.Forbidden
            )
            return
        }

        val result = website.loritta.transaction {
            val profileMoney = website.loritta.getLorittaProfile(session.userId)?.money ?: 0L

            if (3_000 > profileMoney)
                return@transaction Result.NotEnoughSonhos

            val now = Clock.System.now()

            val shipEffectId = ShipEffects.insertAndGetId {
                it[ShipEffects.buyerId] = session.userId
                it[ShipEffects.user1Id] = session.userId
                it[ShipEffects.user2Id] = request.receivingEffectUserId
                it[ShipEffects.editedShipValue] = request.shipPercentage
                it[ShipEffects.expiresAt] = (now + 7.days).toEpochMilliseconds()
            }

            // Cinnamon transaction log
            SimpleSonhosTransactionsLogUtils.insert(
                session.userId,
                now.toJavaInstant(),
                TransactionType.SHIP_EFFECT,
                3_000,
                StoredShipEffectSonhosTransaction(shipEffectId.value)
            )

            // Remove the sonhos
            Profiles.update({ Profiles.id eq session.userId }) {
                with(SqlExpressionBuilder) {
                    it.update(Profiles.money, Profiles.money - 3_000)
                }
            }

            val activeShipEffects = ShipEffects.selectAll()
                .where {
                    ShipEffects.buyerId eq session.userId and (ShipEffects.expiresAt greater System.currentTimeMillis())
                }.map { row ->
                    ShipEffect(
                        row[ShipEffects.id].value,
                        UserId(row[ShipEffects.buyerId].toULong()),
                        UserId(row[ShipEffects.user1Id].toULong()),
                        UserId(row[ShipEffects.user2Id].toULong()),
                        row[ShipEffects.editedShipValue],
                        Instant.fromEpochMilliseconds(row[ShipEffects.expiresAt])
                    )
                }

            return@transaction Result.Success(activeShipEffects)
        }

        call.response.header("SpicyMorenitta-Use-Response-As-HXTrigger", "true")

        when (result) {
            Result.NotEnoughSonhos -> {
                call.respondHtmlFragment(status = HttpStatusCode.PaymentRequired) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você não tem sonhos suficientes para comprar este item!"
                        )
                    )

                    blissCloseAllModals()
                }
            }
            is Result.Success -> {
                val resolvedUsers = result.activeShipEffects.flatMap { listOf(it.user1, it.user2, it.buyerId) }
                    .distinct()
                    .mapNotNull { website.loritta.pudding.users.getCachedUserInfoById(it) }

                call.respondHtmlFragment(status = HttpStatusCode.OK) {
                    blissCloseAllModals()
                    blissShowModal(
                        createEmbeddedModal(
                            i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.EffectApplied.Title),
                            EmbeddedModal.Size.MEDIUM,
                            true,
                            {
                                val randomPicture = listOf(
                                    "https://stuff.loritta.website/ship/pantufa.png",
                                    "https://stuff.loritta.website/ship/gabriela.png"
                                )

                                div {
                                    style = "text-align: center;"

                                    img(src = randomPicture.random()) {
                                        width = "300"
                                    }
                                }

                                text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.EffectApplied.Description))
                            },
                            listOf(
                                {
                                    defaultModalCloseButton(i18nContext, text = I18nKeysData.Website.Dashboard.ShipEffects.EffectApplied.ThanksLoveOracle)
                                }
                            )
                        )
                    )
                    blissSoundEffect("configSaved")

                    shipEffectsBribes(i18nContext, session, result.activeShipEffects, resolvedUsers)
                }
            }
        }
    }

    private sealed class Result {
        data object NotEnoughSonhos : Result()
        data class Success(val activeShipEffects: List<ShipEffect>) : Result()
    }
}