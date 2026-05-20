package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commands

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlan
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.utils.GuildCommandConfigData
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.SWAP_EVERYTHING_DASHBOARD
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.simpleHeroImage
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configReset
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class CommandsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/commands") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlan, member: Member) {
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
                                configReset(i18nContext)
                            }

                            heroWrapper {
                                heroText {
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

                                simpleHeroImage("https://assets.perfectdreams.media/loritta/loritta-images.png")
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

                                ul(classes = "commands-summary") {
                                    for ((category, _) in groupedByCategories) {
                                        li {
                                            a(href = "#${category.name.lowercase()}") {
                                                text(i18nContext.get(category.localizedName))
                                            }
                                        }
                                    }
                                }

                                div(classes = "commands-categories") {
                                    for ((category, commands) in groupedByCategories) {
                                        div(classes = "commands-category-section") {
                                            id = category.name.lowercase()
                                            val color = LorittaUtils.getCategoryColor(category)
                                            style = "--commands-category-color: ${
                                                String.format(
                                                    "#%02x%02x%02x",
                                                    color.red,
                                                    color.green,
                                                    color.blue
                                                )
                                            }"

                                            div(classes = "commands-category-header") {
                                                div {
                                                    h2 {
                                                        text(i18nContext.get(category.localizedName))
                                                    }

                                                    for (str in i18nContext.get(category.localizedDescription)) {
                                                        p(classes = "commands-category-description") {
                                                            text(str)
                                                        }
                                                    }
                                                }

                                                val imageBase = LorittaUtils.getCategoryImage(category)
                                                if (imageBase != null) {
                                                    img(src = "https://assets.perfectdreams.media/loritta/$imageBase.png", classes = "commands-category-header-image") {
                                                        attributes["srcset"] = listOf(160, 320, 640).joinToString(", ") {
                                                            "https://assets.perfectdreams.media/loritta/$imageBase@${it}w.png ${it}w"
                                                        }
                                                        attributes["sizes"] = "(max-width: 900px) 100vw, 200px"
                                                        attributes["loading"] = "lazy"
                                                    }
                                                }
                                            }

                                            div(classes = "commands-list") {
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
            val config = guildCommandConfigs.getCommandConfig(targetCommand.uniqueId)

            val commandLabelText = buildString {
                if (targetCommand is SlashCommandDeclaration)
                    append("/")

                for ((index, parent) in labelDeclarations.withIndex()) {
                    val isLast = labelDeclarations.size == index + 1

                    when (parent) {
                        is ExecutableApplicationCommandDeclaration -> append(i18nContext.get(parent.name))
                        is SlashCommandGroupDeclaration -> append(i18nContext.get(parent.name))
                        else -> error("Unsupported parent type $parent")
                    }

                    if (!isLast)
                        append(" ")
                }
            }

            val toggleId = "toggle-${UUID.randomUUID()}"

            div(classes = "command-entry") {
                label(classes = "toggle-wrapper") {
                    htmlFor = toggleId

                    div(classes = "command-entry-content") {
                        div(classes = "command-entry-label") {
                            when (targetCommand) {
                                is SlashCommandDeclaration -> {
                                    span(classes = "discord-mention") {
                                        style = "padding: 4px 8px 4px 8px;"
                                        text(commandLabelText)
                                    }
                                }

                                is MessageCommandDeclaration -> {
                                    svgIcon(SVGIcons.ChatText) {
                                        attr("style", "width: 1.25em; opacity: 0.75;")
                                    }
                                    svgIcon(SVGIcons.CaretRight) {
                                        attr("style", "width: 1em; opacity: 0.75;")
                                    }
                                    text(commandLabelText)
                                }

                                is UserCommandDeclaration -> {
                                    svgIcon(SVGIcons.User) {
                                        attr("style", "width: 1.25em; opacity: 0.75;")
                                    }
                                    svgIcon(SVGIcons.CaretRight) {
                                        attr("style", "width: 1em; opacity: 0.75;")
                                    }
                                    text(commandLabelText)
                                }
                            }
                        }

                        if (targetCommand is SlashCommandDeclaration) {
                            div(classes = "command-entry-description") {
                                text(i18nContext.get(targetCommand.description))
                            }
                        }

                        div(classes = "command-entry-meta") {
                            if (targetCommand is SlashCommandDeclaration) {
                                val otherAlternatives = mutableListOf(
                                    buildString {
                                        append(commandPrefix)
                                        labelDeclarations.forEach {
                                            when (it) {
                                                is SlashCommandDeclaration -> append(i18nContext.get(it.name))
                                                is SlashCommandGroupDeclaration -> append(i18nContext.get(it.name))
                                            }
                                            append(" ")
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
                                                append(" ")
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
                                            text(i18nContext.get(I18nKeysData.Website.Commands.Synonyms))
                                            text(" ")
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
                                div {
                                    i {
                                        text(i18nContext.get(I18nKeysData.Website.Commands.AvailableInPocketLoritta))
                                    }
                                }
                            }
                        }
                    }

                    div {
                        checkBoxInput {
                            attributes["loritta-config"] = "${targetCommand.uniqueId}"
                            name = "${targetCommand.uniqueId}"
                            if (config.enabled)
                                checked = true
                            id = toggleId
                        }
                        div(classes = "switch-slider round") {}
                    }
                }
            }
        }
    }

    data class GuildCommandConfigs(val entries: Map<UUID, GuildCommandConfigData>) {
        fun getCommandConfig(commandId: UUID) = entries[commandId] ?: GuildCommandConfigData(true)
    }
}