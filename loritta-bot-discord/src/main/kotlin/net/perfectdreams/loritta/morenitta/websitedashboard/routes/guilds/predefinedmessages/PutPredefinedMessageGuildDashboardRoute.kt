package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.predefinedmessages

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import io.ktor.server.request.userAgent
import io.ktor.server.util.getOrFail
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.WebAuditLogUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class PutPredefinedMessageGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/predefined-messages/{entryId}") {
    @Serializable
    data class UpdatePredefinedMessageRequest(
        val short: String? = null,
        val message: String? = null,
        val duration: String? = null,
        val deleteDays: Int? = null
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlan, member: Member) {
        val entryId = call.parameters.getOrFail("entryId").toLong()

        val request = Json.decodeFromString<UpdatePredefinedMessageRequest>(call.receiveText())

        val short = request.short?.trim()?.lowercase()
        val message = request.message?.trim()
        val duration = request.duration?.trim()?.ifBlank { null }
        // 0 = no override
        val deleteDays = request.deleteDays?.coerceIn(0, 7)?.takeIf { it != 0 }

        if (short.isNullOrBlank() || message.isNullOrBlank()) {
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Toasts.InvalidInput)
                    )
                )
            }
            return
        }

        val result = website.loritta.transaction {
            val normalizedShort = short.lowercase()
            val duplicate = ModerationPredefinedPunishmentMessages.selectAll()
                .where {
                    ModerationPredefinedPunishmentMessages.guild eq guild.idLong and (ModerationPredefinedPunishmentMessages.id neq entryId)
                }
                .any { it[ModerationPredefinedPunishmentMessages.short].lowercase() == normalizedShort }

            if (duplicate)
                return@transaction Result.DuplicateShort

            val updated = ModerationPredefinedPunishmentMessages.update({
                ModerationPredefinedPunishmentMessages.guild eq guild.idLong and (ModerationPredefinedPunishmentMessages.id eq entryId)
            }) {
                it[ModerationPredefinedPunishmentMessages.short] = short
                it[ModerationPredefinedPunishmentMessages.message] = message
                it[ModerationPredefinedPunishmentMessages.duration] = duration
                it[ModerationPredefinedPunishmentMessages.deleteDays] = deleteDays
            }

            if (updated == 0)
                return@transaction Result.NotFound

            WebAuditLogUtils.addEntry(
                guild.idLong,
                session.userId,
                call.request.trueIp,
                call.request.userAgent(),
                TrackedChangeType.EDITED_PREDEFINED_MESSAGE
            )

            Result.Success
        }

        when (result) {
            Result.Success -> {
                call.respondConfigSaved(i18nContext)
            }
            Result.DuplicateShort -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Toasts.DuplicateShort)
                        )
                    )
                }
            }
            Result.NotFound -> {
                call.respondHtmlFragment(status = HttpStatusCode.NotFound) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Toasts.NotFoundOnEdit)
                        )
                    )
                }
            }
        }
    }

    private sealed class Result {
        data object Success : Result()
        data object DuplicateShort : Result()
        data object NotFound : Result()
    }
}
