package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.*
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.ExecutableApplicationCommandDeclaration
import net.perfectdreams.loritta.morenitta.interactions.commands.MessageCommandDeclaration
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclaration
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandGroupDeclaration
import net.perfectdreams.loritta.morenitta.interactions.commands.UserCommandDeclaration
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.website.components.SVGIcon.svgIcon
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import java.awt.Color

class CommandsView(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String
) : NavbarView(
    loritta,
    i18nContext,
    locale,
    path
) {
    companion object {
        private const val DEFAULT_COMMAND_PREFIX = "+"
    }

    private val pocketLorittaDashboardUrl = "${loritta.config.loritta.dashboard.url.removeSuffix("/")}/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/user-app?utm_source=commands-page&utm_medium=pocket-loritta-link&utm_campaign=pocket-loritta"

    override fun getTitle() = i18nContext.get(I18nKeysData.Website.Commands.Title)

    override fun DIV.generateContent() {
        div(classes = "odd-wrapper") {
            div(classes = "media") {
                div(classes = "media-body") {
                    div(classes = "commands-page-wrapper") {
                        div(classes = "commands-hero-wrapper") {
                            div(classes = "commands-hero-text") {
                                h1 {
                                    text(i18nContext.get(I18nKeysData.Website.Commands.Title))
                                }

                                val commandExample = "${i18nContext.get(I18nKeysData.Commands.Command.Loritta.Label)} ${i18nContext.get(I18nKeysData.Commands.Command.Loritta.Info.Label)}"

                                for (str in i18nContext.language.textBundle.lists.getValue(I18nKeys.Website.Commands.Description.key)) {
                                    p {
                                        WebsiteUtils.buildAsHtml(
                                            str,
                                            { control ->
                                                when (control) {
                                                    "slashExample" -> span(classes = "discord-mention") { text("/$commandExample") }
                                                    "plusExample" -> span(classes = "discord-mention") { text("+$commandExample") }
                                                    "lorittaMention" -> span(classes = "discord-mention") { text("@" + loritta.lorittaShards.shardManager.shards.first().selfUser.name) }
                                                    else -> text("{$control}")
                                                }
                                            },
                                            { text(it) }
                                        )
                                    }
                                }
                            }

                            img(src = "https://assets.perfectdreams.media/loritta/loritta-images.png", classes = "commands-hero-image") {
                                attributes["style"] = "max-height: 100%; max-width: 100%; aspect-ratio: 1168/1580"
                                attributes["loading"] = "lazy"
                            }
                        }

                        val groupedByCategories = loritta.interactionsListener.manager
                            .applicationCommands
                            .groupBy { it.category }

                        h3 {
                            text(i18nContext.get(I18nKeysData.Website.Commands.SummaryTitle))
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
                                    style = "--commands-category-color: ${String.format("#%02x%02x%02x", color.red, color.green, color.blue)};"

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

                                        val imageBase = getCategoryImageBase(category)
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
                    }
                }
            }
        }
    }

    private fun FlowContent.generateCommand(
        targetCommand: ExecutableApplicationCommandDeclaration,
        labelDeclarations: List<Any>
    ) {
        val executor: Any? = when (targetCommand) {
            is MessageCommandDeclaration -> targetCommand.executor
            is SlashCommandDeclaration -> targetCommand.executor
            is UserCommandDeclaration -> targetCommand.executor
        }

        if (executor == null)
            return

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

        div(classes = "commands-entry") {
            val clazz = when (targetCommand) {
                is MessageCommandDeclaration -> "command-entry-label-message"
                is SlashCommandDeclaration -> "command-entry-label-slash"
                is UserCommandDeclaration -> "command-entry-label-user"
            }

            div(classes = "command-entry-label $clazz") {
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

            val otherAlternatives = mutableListOf<String>()

            if (targetCommand is SlashCommandDeclaration) {
                div(classes = "commands-entry-description") {
                    text(i18nContext.get(targetCommand.description))
                }

                if (targetCommand.enableLegacyMessageSupport) {
                    otherAlternatives.add(
                        buildString {
                            append(DEFAULT_COMMAND_PREFIX)
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
                                append(DEFAULT_COMMAND_PREFIX)
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
                        otherAlternatives.add("$DEFAULT_COMMAND_PREFIX$absolutePath")
                    }
                }
            }

            div(classes = "commands-entry-meta") {
                if (otherAlternatives.isNotEmpty()) {
                    div {
                        b {
                            text(i18nContext.get(I18nKeysData.Website.Commands.Synonyms))
                            text(" ")
                        }
                        var isFirst = true
                        for (alt in otherAlternatives) {
                            if (!isFirst)
                                text(", ")

                            text(alt)
                            isFirst = false
                        }
                    }
                }

                if (targetCommand.integrationTypes.contains(IntegrationType.USER_INSTALL)) {
                    div {
                        a(href = pocketLorittaDashboardUrl, classes = "pocket-loritta-link") {
                            text(i18nContext.get(I18nKeysData.Website.Commands.AvailableInPocketLoritta))
                            svgIcon(SVGIcons.ArrowSquareOut)
                        }
                    }
                }
            }
        }
    }

    private fun getCategoryImageBase(category: CommandCategory): String? = when (category) {
        CommandCategory.IMAGES -> "loritta-images"
        CommandCategory.FUN -> "loritta-fun"
        CommandCategory.MODERATION -> "loritta-moderation"
        CommandCategory.SOCIAL -> "loritta-social"
        CommandCategory.DISCORD -> "loritta-wumpus"
        CommandCategory.UTILS -> "loritta-utilities"
        CommandCategory.MISC -> "loritta-miscellaneous"
        CommandCategory.ROLEPLAY -> "loritta-hug"
        CommandCategory.UNDERTALE -> "loritta-sans"
        CommandCategory.POKEMON -> "loritta-pikachu"
        CommandCategory.ECONOMY -> "loritta-rich-heathecliff"
        CommandCategory.VIDEOS -> "loritta-videos"
        CommandCategory.MINECRAFT -> "loritta-minecraft"
        CommandCategory.ROBLOX -> "roblox-logo"
        CommandCategory.ANIME -> "loritta-anime"
        CommandCategory.FORTNITE -> "loritta-fortnite"
        else -> null
    }
}
