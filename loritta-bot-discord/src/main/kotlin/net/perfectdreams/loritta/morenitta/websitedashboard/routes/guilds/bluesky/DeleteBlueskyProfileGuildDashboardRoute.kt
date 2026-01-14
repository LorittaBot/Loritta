package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.userAgent
import io.ktor.server.util.getOrFail
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedBlueskyAccounts
import net.perfectdreams.loritta.common.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.TrackedChangeType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfile
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfiles
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.TrackedProfile
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedBlueskyProfilesSection
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
import kotlin.collections.map

class DeleteBlueskyProfileGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/bluesky/{entryId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val channelId = call.parameters.getOrFail("entryId").toLong()

        val result = website.loritta.transaction {
            val deletedCount = TrackedBlueskyAccounts.deleteWhere {
                TrackedBlueskyAccounts.guildId eq guild.idLong and (TrackedBlueskyAccounts.id eq channelId)
            }

            if (deletedCount == 0)
                return@transaction Result.EntryNotFound

            val profiles = TrackedBlueskyAccounts.selectAll()
                .where {
                    TrackedBlueskyAccounts.guildId eq guild.idLong
                }
                .toList()

            WebAuditLogUtils.addEntry(
                guild.idLong,
                session.userId,
                call.request.trueIp,
                call.request.userAgent(),
                TrackedChangeType.DELETED_BLUESKY_TRACK
            )

            return@transaction Result.Success(profiles)
        }

        when (result) {
            is Result.Success -> {
                val blueskyProfiles = mutableMapOf<String, BlueskyProfile>()
                if (result.trackedProfiles.isNotEmpty()) {
                    val http = website.loritta.http.get("https://public.api.bsky.app/xrpc/app.bsky.actor.getProfiles") {
                        // The docs are wrong, this is a "array", as in, you need to specify multiple parameters
                        for (trackedBlueskyAccount in result.trackedProfiles.take(25)) {
                            parameter("actors", trackedBlueskyAccount[TrackedBlueskyAccounts.repo])
                        }
                    }

                    val profiles = JsonIgnoreUnknownKeys.decodeFromString<BlueskyProfiles>(http.bodyAsText(Charsets.UTF_8).also { println(it) })
                    blueskyProfiles.putAll(profiles.profiles.associateBy { it.did })
                }

                call.respondHtmlFragment(status = HttpStatusCode.OK) {
                    trackedBlueskyProfilesSection(
                        i18nContext,
                        guild,
                        result.trackedProfiles.map {
                            val profileInfo = blueskyProfiles[it[TrackedBlueskyAccounts.repo]]

                            TrackedProfile(
                                profileInfo?.handle,
                                profileInfo?.avatar,
                                it[TrackedBlueskyAccounts.repo],
                                it[TrackedBlueskyAccounts.id].value,
                                it[TrackedBlueskyAccounts.channelId]
                            )
                        }
                    )

                    blissCloseAllModals()

                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.SUCCESS,
                            "Conta deletada!"
                        )
                    )
                }
            }
            Result.EntryNotFound -> {
                call.respondHtmlFragment(status = HttpStatusCode.NotFound) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você não pode deletar um canal que não existe!"
                        )
                    )
                }
            }
        }
    }

    private sealed class Result {
        data class Success(val trackedProfiles: List<ResultRow>) : Result()
        data object EntryNotFound : Result()
    }
}