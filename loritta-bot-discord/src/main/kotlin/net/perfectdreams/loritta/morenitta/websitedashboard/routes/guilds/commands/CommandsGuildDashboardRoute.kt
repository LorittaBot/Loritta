package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commands

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.utils.GuildCommandConfigData
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.SWAP_EVERYTHING_DASHBOARD
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll
import java.awt.Color
import java.util.*

class CommandsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/commands") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val (config, commandConfigs) = website.loritta.transaction {
            val config = website.loritta.getOrCreateServerConfig(guild.idLong)

            val commandConfigs = net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildCommandConfigs.selectAll()
                .where {
                    net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildCommandConfigs.guildId eq guild.idLong
                }
                .toList()
                .associate {
                    it[net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildCommandConfigs.commandId] to GuildCommandConfigData.fromResultRow(it)
                }
                .let { GuildCommandConfigs(it) }

            Pair(config, commandConfigs)
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.CustomCommands.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.COMMANDS)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                blissEvent("resyncState", "[bliss-component='save-bar']")
                                blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                            }

                            div(classes = "hero-wrapper") {
                                div(classes = "hero-text") {
                                    h1 {
                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.Commands.Title))
                                    }

                                    for (str in i18nContext.language
                                        .textBundle
                                        .lists
                                        .getValue(I18nKeys.Website.Dashboard.Commands.DescriptionWithoutLegacyCommands.key)
                                    ) {
                                        p {
                                            handleI18nString(
                                                str,
                                                appendAsFormattedText(i18nContext, mapOf()),
                                            ) {
                                                when (it) {
                                                    "pocketLoritta" -> {
                                                        TextReplaceControls.ComposableFunctionResult {
                                                            a("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/user-app") {
                                                                attributes["bliss-get"] = "[href]"
                                                                attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
                                                                attributes["bliss-push-url:200"] = "true"
                                                                attributes["bliss-replace-load"] = "#loading"
                                                                attributes["bliss-sync"] = "#left-sidebar"
                                                                attributes["bliss-indicator"] = "#right-sidebar-wrapper"

                                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.PocketLoritta.Title))
                                                            }
                                                        }
                                                    }

                                                    "useExternalApplications" -> {
                                                        TextReplaceControls.ComposableFunctionResult {
                                                            text(i18nContext.get(I18nKeysData.Permissions.UseExternalApplications))
                                                        }
                                                    }

                                                    else -> TextReplaceControls.AppendControlAsIsResult
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            hr {}

                            div {
                                id = "section-config"

                                val groupedByCategories = website.loritta.interactionsListener.manager
                                    .applicationCommands
                                    .groupBy {
                                        it.category
                                    }

                                h3 {
                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.Commands.SummaryTitle))
                                }

                                ul {
                                    for ((category, _) in groupedByCategories) {
                                        li {
                                            a(href = "#${category.name}") {
                                                text(i18nContext.get(category.localizedName))
                                            }
                                        }
                                    }
                                }

                                fieldWrappers {
                                    for ((category, commands) in groupedByCategories) {
                                        fieldWrapper {
                                            id = category.name
                                            val color = getCategoryColor(category)
                                            style = "--loritta-blue: ${
                                                String.format(
                                                    "#%02x%02x%02x",
                                                    color.red,
                                                    color.green,
                                                    color.blue
                                                )
                                            }"

                                            h2 {
                                                text(i18nContext.get(category.localizedName))
                                            }

                                            fieldWrappers {
                                                for (command in commands) {
                                                    generateCommand(i18nContext, config.commandPrefix, commandConfigs, command, listOf(command))

                                                    if (command is SlashCommandDeclaration) {
                                                        for (subcommand in command.subcommands) {
                                                            generateCommand(i18nContext, config.commandPrefix, commandConfigs, subcommand, listOf(command, subcommand))
                                                        }

                                                        for (subcommandGroup in command.subcommandGroups) {
                                                            for (subcommand in subcommandGroup.subcommands) {
                                                                generateCommand(
                                                                    i18nContext,
                                                                    config.commandPrefix,
                                                                    commandConfigs,
                                                                    subcommand,
                                                                    listOf(command, subcommandGroup, subcommand)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        {
                            genericSaveBar(
                                i18nContext,
                                false,
                                guild,
                                "/commands"
                            )
                        }
                    )
                }
            )
        }
    }

    private fun FlowContent.generateCommand(
        i18nContext: I18nContext,
        commandPrefix: String,
        guildCommandConfigs: GuildCommandConfigs,
        targetCommand: ExecutableApplicationCommandDeclaration,
        labelDeclarations: List<Any>
    ) {
        val executor: Any? = when (targetCommand) {
            is MessageCommandDeclaration -> targetCommand.executor
            is SlashCommandDeclaration -> targetCommand.executor
            is UserCommandDeclaration -> targetCommand.executor
        }

        if (executor != null) {
            fieldWrapper {
                val config = guildCommandConfigs.getCommandConfig(targetCommand.uniqueId)

                // println("UUID: ${targetCommand.uniqueId}; Enabled? ${config.enabled}")

                toggleableSection(
                    {
                        if (targetCommand is SlashCommandDeclaration)
                            text("/")

                        for ((index, parent) in labelDeclarations.withIndex()) {
                            val isLast = labelDeclarations.size == index + 1

                            when (parent) {
                                is ExecutableApplicationCommandDeclaration -> text(i18nContext.get(parent.name))
                                is SlashCommandGroupDeclaration -> text(i18nContext.get(parent.name))
                                else -> error("Unsupported parent type $parent")
                            }

                            if (!isLast) {
                                text(" ")
                            }
                        }
                    },
                    {
                        if (targetCommand is SlashCommandDeclaration) {
                            div {
                                text(i18nContext.get(targetCommand.description))
                            }

                            val otherAlternatives = mutableListOf(
                                buildString {
                                    this.append(commandPrefix)
                                    labelDeclarations.forEach {
                                        when (it) {
                                            is SlashCommandDeclaration -> append(i18nContext.get(it.name))
                                            is SlashCommandGroupDeclaration -> append(i18nContext.get(it.name))
                                        }
                                        this.append(" ")
                                    }
                                }.trim()
                            )

                            for (alternativeLabel in targetCommand.alternativeLegacyLabels) {
                                otherAlternatives.add(
                                    buildString {
                                        append(commandPrefix)
                                        labelDeclarations.dropLast(1).forEach {
                                            when (it) {
                                                is SlashCommandDeclaration -> append(i18nContext.get(it.name))
                                                is SlashCommandGroupDeclaration -> append(i18nContext.get(it.name))
                                            }
                                            this.append(" ")
                                        }
                                        append(alternativeLabel)
                                    }
                                )
                            }

                            for (absolutePath in targetCommand.alternativeLegacyAbsoluteCommandPaths) {
                                otherAlternatives.add("$commandPrefix$absolutePath")
                            }

                            if (otherAlternatives.isNotEmpty()) {
                                div {
                                    b {
                                        text("Sinônimos: ")
                                    }
                                    var isFirst = true
                                    otherAlternatives.forEach {
                                        if (!isFirst)
                                            text(", ")

                                        text(it)
                                        isFirst = false
                                    }
                                }
                            }
                        }

                        if (targetCommand.integrationTypes.contains(IntegrationType.USER_INSTALL)) {
                            i {
                                text("Disponível na Loritta de Bolso")
                            }
                        }
                    },
                    config.enabled,
                    "${targetCommand.uniqueId}",
                    true
                )
            }
        }
    }

    private fun getCategoryColor(category: CommandCategory) = when (category) {
        // Photoshop Logo Color
        CommandCategory.IMAGES -> Color(49, 197, 240)
        CommandCategory.FUN -> Color(254, 120, 76)
        CommandCategory.ECONOMY -> Color(47, 182, 92)
        // Discord Blurple
        CommandCategory.DISCORD -> Color(114, 137, 218)
        // Discord "Ban User" background
        CommandCategory.MODERATION -> Color(240, 71, 71)
        // Roblox Logo Color
        CommandCategory.ROBLOX -> Color(226, 35, 26)
        CommandCategory.ROLEPLAY -> Color(243, 118, 166)
        CommandCategory.UTILS -> Color(113, 147, 188)
        // Grass Block
        CommandCategory.MINECRAFT -> Color(124, 87, 58)
        // Pokémon (Pikachu)
        CommandCategory.POKEMON -> Color(244, 172, 0)
        // Undertale
        CommandCategory.UNDERTALE -> Color.BLACK
        // Vídeos
        CommandCategory.VIDEOS -> Color(163, 108, 253)
        // Social
        CommandCategory.SOCIAL -> Color(235, 0, 255)
        CommandCategory.MISC -> Color(121, 63, 166)
        CommandCategory.ANIME -> Color(132, 224, 212)
        else -> Color(0, 193, 223)
    }

    data class GuildCommandConfigs(val entries: Map<UUID, GuildCommandConfigData>) {
        fun getCommandConfig(commandId: UUID) = entries[commandId] ?: GuildCommandConfigData(true)
    }
}