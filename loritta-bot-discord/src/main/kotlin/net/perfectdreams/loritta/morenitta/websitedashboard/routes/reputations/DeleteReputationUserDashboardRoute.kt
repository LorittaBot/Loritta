package net.perfectdreams.loritta.morenitta.websitedashboard.routes.reputations

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import kotlinx.html.hr
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.reputations
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseAllModals
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissSoundEffect
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.StoredReputationDeletedTransaction
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll

class DeleteReputationUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/reputations/{reputationId}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val reputationId = call.parameters.getOrFail("reputationId").toLong()
        val page = (call.request.queryParameters["page"]?.toIntOrNull() ?: 1).coerceAtLeast(1)

        val result = website.loritta.transaction {
            val reputationToBeDeleted = Reputations.selectAll()
                .where { (Reputations.id eq reputationId) and (Reputations.givenById eq session.userId or (Reputations.receivedById eq session.userId)) }
                .firstOrNull()

            if (reputationToBeDeleted != null) {
                val wasGivenToSomeoneElse = reputationToBeDeleted[Reputations.givenById] == session.userId

                if (wasGivenToSomeoneElse) {
                    // Cinnamon transactions log
                    SonhosUtils.takeSonhosAndLogToTransactionLog(
                        session.userId,
                        ReputationsUtils.REMOVE_SENT_REPUTATION_PRICE,
                        TransactionType.REPUTATIONS,
                        StoredReputationDeletedTransaction(reputationToBeDeleted[Reputations.id].value),
                        {
                            return@transaction Result.NotEnoughSonhos
                        }
                    ) {
                        // We don't need to do anything on success, because the code will be handled after this is executed
                    }
                }

                Reputations.deleteWhere { Reputations.id eq reputationToBeDeleted[Reputations.id] }

                val totalReputations = Reputations
                    .selectAll()
                    .where {
                        if (wasGivenToSomeoneElse) {
                            Reputations.givenById eq session.userId
                        } else {
                            Reputations.receivedById eq session.userId
                        }
                    }
                    .count()

                val pageReps = Reputations.selectAll()
                    .where {
                        if (wasGivenToSomeoneElse) {
                            Reputations.givenById eq session.userId
                        } else {
                            Reputations.receivedById eq session.userId
                        }
                    }
                    .orderBy(Reputations.receivedAt, SortOrder.DESC)
                    .limit(100)
                    .offset((page - 1) * 100L)
                    .toList()

                return@transaction Result.Success(totalReputations, pageReps, !wasGivenToSomeoneElse)
            } else {
                return@transaction Result.NotFound
            }
        }

        when (result) {
            is Result.Success -> {
                val usersInformation = website.loritta.lorittaShards.retrieveUsersInfoById(result.reputations.map { it[Reputations.receivedById] }.toSet() + result.reputations.map { it[Reputations.givenById] }.toSet())

                call.respondHtmlFragment {
                    blissCloseAllModals()
                    blissSoundEffect("configSaved")
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.SUCCESS,
                            "Reputação deletada!"
                        )
                    )

                    goBackToPreviousSectionButton(href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/reputations") {
                        text("Voltar para a visão geral de reputações")
                    }

                    hr {}

                    reputations(
                        i18nContext,
                        result.totalReputations,
                        result.reputations,
                        result.isReceivedReputation,
                        page,
                        usersInformation
                    )
                }
            }
            Result.NotFound -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Reputação não encontrada!"
                        )
                    )
                }
            }

            Result.NotEnoughSonhos -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você não tem sonhos suficientes para deletar esta reputação!",
                        )
                    )
                }
            }
        }
    }

    private sealed class Result {
        data class Success(val totalReputations: Long, val reputations: List<ResultRow>, val isReceivedReputation: Boolean) : Result()
        data object NotFound : Result()
        data object NotEnoughSonhos : Result()
    }
}