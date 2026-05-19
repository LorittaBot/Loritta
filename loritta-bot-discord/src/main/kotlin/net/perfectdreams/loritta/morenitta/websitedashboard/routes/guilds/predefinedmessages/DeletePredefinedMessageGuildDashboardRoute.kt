package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.predefinedmessages

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.userAgent
import io.ktor.server.util.getOrFail
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationPredefinedPunishmentMessages
import net.perfectdreams.loritta.common.utils.ServerPremiumPlan
import net.perfectdreams.loritta.common.utils.TrackedChangeType
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.predefinedMessagesSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.WebAuditLogUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseAllModals
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

class DeletePredefinedMessageGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/predefined-messages/{entryId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlan, member: Member) {
        val entryId = call.parameters.getOrFail("entryId").toLong()

        val result = website.loritta.transaction {
            val deletedCount = ModerationPredefinedPunishmentMessages.deleteWhere {
                ModerationPredefinedPunishmentMessages.guild eq guild.idLong and (ModerationPredefinedPunishmentMessages.id eq entryId)
            }

            if (deletedCount == 0)
                return@transaction Result.NotFound

            WebAuditLogUtils.addEntry(
                guild.idLong,
                session.userId,
                call.request.trueIp,
                call.request.userAgent(),
                TrackedChangeType.DELETED_PREDEFINED_MESSAGE
            )

            val remaining = ModerationPredefinedPunishmentMessages.selectAll()
                .where { ModerationPredefinedPunishmentMessages.guild eq guild.idLong }
                .toList()

            Result.Success(remaining)
        }

        when (result) {
            is Result.Success -> {
                call.respondHtmlFragment {
                    predefinedMessagesSection(i18nContext, guild, result.remaining)

                    blissCloseAllModals()

                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.SUCCESS,
                            i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Toasts.DeletedSuccessfully)
                        )
                    )
                }
            }
            Result.NotFound -> {
                call.respondHtmlFragment(status = HttpStatusCode.NotFound) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Toasts.NotFoundOnDelete)
                        )
                    )
                }
            }
        }
    }

    private sealed class Result {
        data class Success(val remaining: List<ResultRow>) : Result()
        data object NotFound : Result()
    }
}
