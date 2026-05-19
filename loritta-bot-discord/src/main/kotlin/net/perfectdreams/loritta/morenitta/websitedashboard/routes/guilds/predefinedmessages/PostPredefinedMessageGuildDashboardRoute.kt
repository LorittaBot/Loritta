package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.predefinedmessages

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import io.ktor.server.request.userAgent
import io.ktor.server.response.header
import kotlinx.html.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationPredefinedPunishmentMessages
import net.perfectdreams.loritta.common.utils.ServerPremiumPlan
import net.perfectdreams.loritta.common.utils.TrackedChangeType
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.predefinedMessageEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.saveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.WebAuditLogUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

class PostPredefinedMessageGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/predefined-messages") {
    @Serializable
    data class CreatePredefinedMessageRequest(
        val short: String? = null,
        val message: String? = null,
        val duration: String? = null,
        val deleteDays: Int? = null
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlan, member: Member) {
        val request = Json.decodeFromString<CreatePredefinedMessageRequest>(call.receiveText())

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
            val existing = ModerationPredefinedPunishmentMessages.selectAll()
                .where { ModerationPredefinedPunishmentMessages.guild eq guild.idLong }
                .toList()

            if (existing.size >= PredefinedMessagesUtils.MAX_PREDEFINED_MESSAGES)
                return@transaction Result.LimitReached

            val normalizedShort = short.lowercase()
            if (existing.any { it[ModerationPredefinedPunishmentMessages.short].lowercase() == normalizedShort })
                return@transaction Result.DuplicateShort

            val insertedId = ModerationPredefinedPunishmentMessages.insertAndGetId {
                it[ModerationPredefinedPunishmentMessages.guild] = guild.idLong
                it[ModerationPredefinedPunishmentMessages.short] = short
                it[ModerationPredefinedPunishmentMessages.message] = message
                it[ModerationPredefinedPunishmentMessages.duration] = duration
                it[ModerationPredefinedPunishmentMessages.deleteDays] = deleteDays
            }

            WebAuditLogUtils.addEntry(
                guild.idLong,
                session.userId,
                call.request.trueIp,
                call.request.userAgent(),
                TrackedChangeType.CREATED_PREDEFINED_MESSAGE
            )

            Result.Created(insertedId.value)
        }

        when (result) {
            is Result.Created -> {
                call.response.header("Bliss-Push-Url", "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/predefined-messages/${result.entryId}")
                call.respondHtmlFragment {
                    configSaved(i18nContext)

                    div {
                        id = "section-config"

                        predefinedMessageEditor(
                            i18nContext,
                            guild,
                            short,
                            message,
                            duration,
                            deleteDays
                        )
                    }

                    hr {}

                    saveBar(
                        i18nContext,
                        false,
                        {
                            attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/predefined-messages/${result.entryId}"
                            attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
                            attributes["bliss-headers"] = buildJsonObject {
                                put("Loritta-Configuration-Reset", "true")
                            }.toString()
                        }
                    ) {
                        attributes["bliss-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/predefined-messages/${result.entryId}"
                        attributes["bliss-include-json"] = "#section-config"
                    }
                }
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
            Result.LimitReached -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Toasts.LimitReached)
                        )
                    )
                }
            }
        }
    }

    private sealed class Result {
        data class Created(val entryId: Long) : Result()
        data object DuplicateShort : Result()
        data object LimitReached : Result()
    }
}
