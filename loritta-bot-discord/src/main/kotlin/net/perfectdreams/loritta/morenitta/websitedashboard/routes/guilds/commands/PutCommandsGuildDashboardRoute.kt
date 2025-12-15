package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commands

import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildCommandConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.upsert
import java.util.*

class PutCommandsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/commands") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        // This requires a bit of trickery
        // The frontend sends a "map" String -> Bool, the String is the UUID of each command
        val map = Json.decodeFromString<Map<String, Boolean>>(call.receiveText())

        val allCommands = website.loritta.interactionsListener.manager.slashCommands.flatMap {
            listOf(it) + it.subcommands + it.subcommandGroups.flatMap { it.subcommands }
        } + website.loritta.interactionsListener.manager.userCommands + website.loritta.interactionsListener.manager.messageCommands

        val allCommandsUniqueIds = allCommands.map { it.uniqueId }
        val enabledCommandsUniqueIds = map.filter { it.value }.map { UUID.fromString(it.key) }

        website.loritta.transaction {
            // Delete all configs that aren't in the allCommandsUniqueIds list
            // This way we can clean up commands that were removed from Loritta
            GuildCommandConfigs.deleteWhere {
                GuildCommandConfigs.guildId eq guild.idLong and (GuildCommandConfigs.commandId notInList allCommandsUniqueIds)
            }

            // Now go through all commands...
            for (command in allCommands) {
                // And upsert!
                GuildCommandConfigs.upsert(GuildCommandConfigs.guildId, GuildCommandConfigs.commandId) {
                    it[GuildCommandConfigs.guildId] = guild.idLong
                    it[GuildCommandConfigs.commandId] = command.uniqueId
                    it[GuildCommandConfigs.enabled] = command.uniqueId in enabledCommandsUniqueIds
                }
            }
        }

        call.respondConfigSaved(i18nContext)
    }
}