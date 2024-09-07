package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.commands

import io.ktor.server.application.*
import io.ktor.server.request.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildCommandConfigs
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.GuildCommandConfigData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.commands.GuildCommandsView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class PutConfigureCommandsRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/commands") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val postParams = call.receiveParameters()

		// The POST returns all the ENABLED commands
		// We want to get the things that are MISSING from the post to disable them
		val allCommands = loritta.interactionsListener.manager.slashCommands.flatMap {
			listOf(it) + it.subcommands + it.subcommandGroups.flatMap { it.subcommands }
		} + loritta.interactionsListener.manager.userCommands + loritta.interactionsListener.manager.messageCommands

		val uniqueIds = allCommands.map { it.uniqueId }

		val guildCommandConfigs = loritta.transaction {
			GuildCommandConfigs.deleteWhere {
				GuildCommandConfigs.guildId eq guild.idLong and (GuildCommandConfigs.commandId notInList uniqueIds)
			}

			for (command in allCommands) {
				GuildCommandConfigs.upsert(GuildCommandConfigs.guildId, GuildCommandConfigs.commandId) {
					it[GuildCommandConfigs.guildId] = guild.idLong
					it[GuildCommandConfigs.commandId] = command.uniqueId
					it[GuildCommandConfigs.enabled] = postParams["command-${command.uniqueId}"] == "on"
				}
			}

			GuildCommandConfigs.selectAll()
				.where {
					GuildCommandConfigs.guildId eq guild.idLong
				}
				.toList()
				.associate {
					it[GuildCommandConfigs.commandId] to GuildCommandConfigData.fromResultRow(it)
				}
		}.let { GuildCommandsView.GuildCommandConfigs(it) }

		call.response.headerHXTrigger {
			closeSpicyModal = true
			playSoundEffect = "config-saved"
		}

		call.respondHtml(
			GuildCommandsView(
				loritta.newWebsite!!,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				colorTheme,
				guild,
				guildCommandConfigs,
				serverConfig.commandPrefix
			).generateHtml()
		)
	}
}