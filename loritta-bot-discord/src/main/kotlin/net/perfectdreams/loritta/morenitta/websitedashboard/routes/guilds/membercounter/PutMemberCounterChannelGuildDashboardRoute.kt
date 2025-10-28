package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.membercounter

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.MemberCounterChannelConfigs
import net.perfectdreams.loritta.common.utils.CounterThemes
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.listeners.DiscordListener
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class PutMemberCounterChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/member-counter/{channelId}") {
    @Serializable
    data class SaveMemberCounterRequest(
        val enabled: Boolean,
        val topic: String,
        val theme: CounterThemes,
        val padding: Int,
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val channel = guild.getGuildMessageChannelById(call.parameters.getOrFail("channelId").toLong())

        if (channel == null) {
            // TODO - bliss-dash: Add a proper page!
            call.respond(HttpStatusCode.NotFound)
            return
        }

        val request = Json.decodeFromString<SaveMemberCounterRequest>(call.receiveText())

        if (request.padding !in 1..10)
            error("Too much padding! Padding must be between 1 and 10, but was ${request.padding}")

        val serverConfig = website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            MemberCounterChannelConfigs.deleteWhere {
                MemberCounterChannelConfigs.guild eq guild.idLong and (MemberCounterChannelConfigs.channelId eq channel.idLong)
            }

            if (request.enabled) {
                MemberCounterChannelConfigs.insert {
                    it[MemberCounterChannelConfigs.guild] = guild.idLong
                    it[MemberCounterChannelConfigs.channelId] = channel.idLong
                    it[MemberCounterChannelConfigs.topic] = request.topic
                    it[MemberCounterChannelConfigs.theme] = request.theme
                    it[MemberCounterChannelConfigs.padding] = request.padding
                }
            }

            serverConfig
        }

        if (request.enabled)
            DiscordListener.queueTextChannelTopicUpdates(website.loritta, guild, serverConfig)

        call.respondConfigSaved(i18nContext)
    }
}