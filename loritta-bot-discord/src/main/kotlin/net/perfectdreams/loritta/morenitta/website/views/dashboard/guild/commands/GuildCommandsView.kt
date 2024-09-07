package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.commands

import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildCommandConfigs
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.utils.GuildCommandConfigData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.DashboardSaveBar.lorittaSaveBar
import net.perfectdreams.loritta.morenitta.website.components.DiscordLikeToggles.toggleableSection
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildDashboardView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import java.awt.Color
import java.util.*

class GuildCommandsView(
    loritta: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    guild: Guild,
    private val guildCommandConfigs: GuildCommandConfigs,
    private val commandPrefix: String
) : GuildDashboardView(
    loritta,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
    guild,
    "commands"
) {
    override fun DIV.generateRightSidebarContents() {
        div {
            div {
                id = "form-stuff-wrapper"

                div(classes = "hero-wrapper") {
                    // img(src = "https://stuff.loritta.website/monica-ata-bluetero.jpeg", classes = "hero-image") {}

                    div(classes = "hero-text") {
                        h1 {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Commands.Title))
                        }

                        for (str in i18nContext.language
                            .textBundle
                            .lists
                            .getValue(I18nKeys.Website.Dashboard.Commands.Description.key)
                        ) {
                            p {
                                handleI18nString(
                                    str,
                                    appendAsFormattedText(i18nContext, mapOf()),
                                ) {
                                    when (it) {
                                        "pocketLoritta" -> {
                                            TextReplaceControls.ComposableFunctionResult {
                                                a("/${locale.path}/dashboard/user-app") {
                                                    // Currently we don't use htmx here yet due to CORS issues

                                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.PocketLoritta.Title))
                                                }
                                            }
                                        }

                                        "useExternalApplications" -> {
                                            TextReplaceControls.ComposableFunctionResult {
                                                text(i18nContext.get(I18nKeysData.Permissions.UseExternalApplications))
                                            }
                                        }

                                        "legacyCommandList" -> {
                                            TextReplaceControls.ComposableFunctionResult {
                                                a("/${locale.path}/guild/${guild.idLong}/configure/legacy-commands") {
                                                    // Currently we don't use htmx here yet due to CORS issues

                                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.Commands.LegacyCommands))
                                                }
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
                    id = "module-config-wrapper"
                    form {
                        id = "module-config"
                        attributes["loritta-synchronize-with-save-bar"] = "#save-bar"
                        val groupedByCategories = lorittaWebsite.loritta.interactionsListener.manager
                            .applicationCommands
                            .groupBy {
                                it.category
                            }

                        for ((category, commands) in groupedByCategories) {
                            div {
                                val color = getCategoryColor(category)
                                style =
                                    "--loritta-blue: ${
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

                                for (command in commands) {
                                    generateCommand(command, listOf(command))

                                    if (command is SlashCommandDeclaration) {
                                        for (subcommand in command.subcommands) {
                                            generateCommand(subcommand, listOf(command, subcommand))
                                        }

                                        for (subcommandGroup in command.subcommandGroups) {
                                            for (subcommand in subcommandGroup.subcommands) {
                                                generateCommand(
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

                // We don't need this hr becausee technically the "generateCommand" generates it for us
                // hr {}

                lorittaSaveBar(
                    i18nContext,
                    false,
                    {}
                ) {
                    attributes["hx-put"] = ""
                }
            }
        }
    }

    private fun DIV.generateCommand(
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

            // println("UUID: ${targetCommand.uniqueId}; Enabled? ${config.enabled}")

            div {
                toggleableSection(
                    "command-${targetCommand.uniqueId}",
                    "command-${targetCommand.uniqueId}",
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
                )
            }

            hr {}
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