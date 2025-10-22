package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.PremiumTrackTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch.PutTwitchTrackRoute.AddGuildTwitchChannelResult
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch.TwitchWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.*
import java.time.Instant
import kotlin.math.ceil

class PostTwitchChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/twitch") {
    @Serializable
    data class CreateTwitchChannelTrackRequest(
        val enablePremiumTrack: Boolean,
        val twitchUserId: Long,
        val channelId: Long,
        val message: String
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val request = Json.decodeFromString<CreateTwitchChannelTrackRequest>(call.receiveText())

        // This is the route that adds a NEW instance to the configuration
        val twitchUserId = request.twitchUserId
        val createPremiumTrack = request.enablePremiumTrack
        val channelId = request.channelId
        val message = request.message

        val result = website.loritta.transaction {
            // First we need to try creating the premium track, if needed
            if (createPremiumTrack) {
                val isAlreadyAdded = PremiumTrackTwitchAccounts.selectAll().where {
                    PremiumTrackTwitchAccounts.guildId eq guild.idLong and (PremiumTrackTwitchAccounts.twitchUserId eq twitchUserId)
                }.count() == 1L

                if (!isAlreadyAdded) {
                    // We don't reeally care if there's already a premium track inserted
                    val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                        .toList()
                        .sumOf { it.value }
                        .let { ceil(it) }

                    val plan = ServerPremiumPlans.getPlanFromValue(valueOfTheDonationKeysEnabledOnThisGuild)

                    val premiumTracksOfTheGuildCount =
                        PremiumTrackTwitchAccounts.select(PremiumTrackTwitchAccounts.twitchUserId).where {
                            PremiumTrackTwitchAccounts.guildId eq guild.idLong
                        }.orderBy(
                            PremiumTrackTwitchAccounts.addedAt,
                            SortOrder.ASC
                        ) // Ordered by the added at date...
                            .count()

                    if (premiumTracksOfTheGuildCount >= plan.maxUnauthorizedTwitchChannels)
                        return@transaction AddGuildTwitchChannelResult.TooManyPremiumTracks

                    PremiumTrackTwitchAccounts.insert {
                        it[PremiumTrackTwitchAccounts.guildId] = guild.idLong
                        it[PremiumTrackTwitchAccounts.twitchUserId] = twitchUserId
                        it[PremiumTrackTwitchAccounts.addedBy] = session.userId
                        it[PremiumTrackTwitchAccounts.addedAt] = Instant.now()
                    }
                }
            }

            // Does not exist, so let's insert it!
            val now = Instant.now()
            val trackId = TrackedTwitchAccounts.insertAndGetId {
                it[TrackedTwitchAccounts.guildId] = guild.idLong
                it[TrackedTwitchAccounts.channelId] = channelId
                it[TrackedTwitchAccounts.twitchUserId] = twitchUserId
                it[TrackedTwitchAccounts.message] = message
                it[TrackedTwitchAccounts.addedAt] = now
                it[TrackedTwitchAccounts.editedAt] = now
            }

            val state = TwitchWebUtils.getTwitchAccountTrackState(twitchUserId)

            val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                .toList()
                .sumOf { it.value }
                .let { ceil(it) }

            val premiumTracksCount = PremiumTrackTwitchAccounts.selectAll().where {
                PremiumTrackTwitchAccounts.guildId eq guild.idLong
            }.count()

            return@transaction AddGuildTwitchChannelResult.Success(trackId.value, state, valueOfTheDonationKeysEnabledOnThisGuild, premiumTracksCount)
        }

        when (result) {
            is AddGuildTwitchChannelResult.Success -> {
                call.respondConfigSaved(i18nContext)
            }
            AddGuildTwitchChannelResult.TooManyPremiumTracks -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    "Você está no limite de acompanhamentos premium!"
                                )
                            )
                        }
                )
            }
        }
    }
}