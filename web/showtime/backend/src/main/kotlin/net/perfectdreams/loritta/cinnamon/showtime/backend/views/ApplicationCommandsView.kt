package net.perfectdreams.loritta.cinnamon.showtime.backend.views

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.FlowOrInteractiveContent
import kotlinx.html.HTMLTag
import kotlinx.html.HtmlBlockTag
import kotlinx.html.TagConsumer
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.details
import kotlinx.html.div
import kotlinx.html.fieldSet
import kotlinx.html.hr
import kotlinx.html.img
import kotlinx.html.input
import kotlinx.html.legend
import kotlinx.html.p
import kotlinx.html.source
import kotlinx.html.style
import kotlinx.html.summary
import kotlinx.html.unsafe
import kotlinx.html.video
import kotlinx.html.visit
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.showtime.backend.PublicApplicationCommands
import net.perfectdreams.loritta.cinnamon.showtime.backend.ShowtimeBackend
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.ImageUtils
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.NitroPayAdGenerator
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.NitroPayAdSize
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.generateNitroPayAd
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.imgSrcSetFromResources
import java.awt.Color
import java.time.LocalDate
import java.time.ZoneId

class ApplicationCommandsView(
    showtimeBackend: ShowtimeBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String,
    val commands: List<net.perfectdreams.loritta.cinnamon.showtime.backend.PublicApplicationCommands.InteractionCommand>,
    val filterByCategory: CommandCategory? = null
) : SidebarsView(
    showtimeBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    companion object {
        // We don't want to show commands in the "MAGIC" category
        private val PUBLIC_CATEGORIES = CommandCategory.values().filterNot { it == CommandCategory.MAGIC }
    }

    override val sidebarAdId = "commands"

    val publicCommands = commands.filter { it.category in PUBLIC_CATEGORIES }

    override fun getTitle() = locale["modules.sectionNames.commands"]

    override fun HtmlBlockTag.leftSidebarContents() {
        // Add search bar
        // Yes, you need to wrap it into a div. Setting "width: 100%;" in the input just causes
        // it to overflow for *some reason*.
        div(classes = "side-content") {
            div {
                style = "text-align: center;"

                a(href = "/${locale.path}/commands/legacy") {
                    attributes["data-preload-link"] = "true"

                    button(classes = "button-discord button-discord-info pure-button") {
                        +i18nContext.get(I18nKeysData.Website.Commands.ViewLegacyCommands)
                    }
                }
            }

            div {
                style = "margin: 6px 10px;\n" +
                        "display: flex;\n" +
                        "align-items: center;\n" +
                        "justify-content: center;\n" +
                        "column-gap: 0.5em;"
                input(classes = "search-bar") {
                    style = "width: 100%;"
                }

                // And we also need to wrap this into a div to avoid the icon resizing automatically due to the column-gap
                div {
                    iconManager.search.apply(this)
                }
            }

            hr {}
        }

        // The first entry is "All"
        a(href = "/${locale.path}/commands/slash", classes = "entry") {
            if (filterByCategory == null)
                classes = classes + "selected"

            // Yay workarounds!
            commandCategory {
                attributes["data-command-category"] = "ALL"

                div {
                    +"Todos"
                }

                div {
                    +publicCommands.size.toString()
                }
            }
        }

        for (category in PUBLIC_CATEGORIES.sortedByDescending { category -> commands.count { it.category == category } }) {
            val commandsInThisCategory = commands.count { it.category == category }

            // Register a redirect, the frontend will cancel this event if JS is enabled and filter the entries manually
            a(href = "/${locale.path}/commands/slash/${category.name.lowercase()}", classes = "entry") {
                if (filterByCategory == category)
                    classes = classes + "selected"

                commandCategory {
                    attributes["data-command-category"] = category.name

                    div {
                        + i18nContext.get(category.localizedName)
                    }

                    div {
                        +"$commandsInThisCategory"
                    }
                }
            }
        }
    }

    override fun HtmlBlockTag.rightSidebarContents() {
        fun generateAllCommandsInfo(
            visible: Boolean,
            imagePath: String,
            sizes: String
        ) {
            val categoryName = "ALL"

            div(classes = "media") {
                style = "width: 100%;"
                attributes["data-category-info"] = categoryName

                if (!visible)
                    style += "display: none;"

                div(classes = "media-figure") {
                    style = "width: 250px;\n" +
                            "height: 250px;\n" +
                            "display: flex;\n" +
                            "align-items: center; justify-content: center;"

                    imgSrcSetFromResources(imagePath, sizes) {
                        // Lazy load the images, because *for some reason* it loads all images even tho the div is display none.
                        attributes["loading"] = "lazy"
                        style = "max-height: 100%; max-width: 100%;"
                    }
                }

                div(classes = "media-body") {
                    for (entry in i18nContext.get(I18nKeysData.Website.Commands.WelcomeToMyCommandListIntro)) {
                        p {
                            + entry
                        }
                    }
                }
            }
        }

        fun generateCategoryInfo(
            category: CommandCategory,
            visible: Boolean,
            imagePath: String,
            sizes: String
        ) {
            val categoryName = category.name

            div(classes = "media") {
                style = "width: 100%;"
                attributes["data-category-info"] = categoryName

                if (!visible)
                    style += "display: none;"

                div(classes = "media-figure") {
                    style = "width: 250px;\n" +
                            "height: 250px;\n" +
                            "display: flex;\n" +
                            "align-items: center; justify-content: center;"

                    imgSrcSetFromResources(imagePath, sizes) {
                        // Lazy load the images, because *for some reason* it loads all images even tho the div is display none.
                        attributes["loading"] = "lazy"
                        style = "max-height: 100%; max-width: 100%;"
                    }
                }

                div(classes = "media-body") {
                    for (entry in i18nContext.get(category.localizedDescription)) {
                        p {
                            + entry
                        }
                    }
                }
            }
        }

        generateAllCommandsInfo(
            filterByCategory == null,
            "${versionPrefix}/assets/img/support/lori_support.png",
            "(max-width: 800px) 50vw, 15vw"
        )

        generateCategoryInfo(
            CommandCategory.IMAGES,
            filterByCategory == CommandCategory.IMAGES,
            "/v3/assets/img/categories/images.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.FUN,
            filterByCategory == CommandCategory.FUN,
            "/v3/assets/img/categories/fun.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.MODERATION,
            filterByCategory == CommandCategory.MODERATION,
            "/v3/assets/img/categories/moderation.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.SOCIAL,
            filterByCategory == CommandCategory.SOCIAL,
            "/v3/assets/img/categories/social.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.DISCORD,
            filterByCategory == CommandCategory.DISCORD,
            "/v3/assets/img/categories/loritta_wumpus.png",
            "(max-width: 1366px) 250px",
        )

        generateCategoryInfo(
            CommandCategory.UTILS,
            filterByCategory == CommandCategory.UTILS,
            "/v3/assets/img/categories/utilities.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.MISC,
            filterByCategory == CommandCategory.MISC,
            "/v3/assets/img/categories/miscellaneous.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.ROLEPLAY,
            filterByCategory == CommandCategory.ROLEPLAY,
            "/v3/assets/img/categories/hug.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.UNDERTALE,
            filterByCategory == CommandCategory.UNDERTALE,
            "/v3/assets/img/categories/lori_sans.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.POKEMON,
            filterByCategory == CommandCategory.POKEMON,
            "/v3/assets/img/categories/lori_pikachu.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.ECONOMY,
            filterByCategory == CommandCategory.ECONOMY,
            "/v3/assets/img/categories/money.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.FORTNITE,
            filterByCategory == CommandCategory.FORTNITE,
            "/v3/assets/img/categories/loritta_fortnite.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.VIDEOS,
            filterByCategory == CommandCategory.VIDEOS,
            "/v3/assets/img/categories/videos.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.ANIME,
            filterByCategory == CommandCategory.ANIME,
            "/v3/assets/img/categories/anime.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.MINECRAFT,
            filterByCategory == CommandCategory.MINECRAFT,
            "/v3/assets/img/categories/minecraft.png",
            "(max-width: 1366px) 250px"
        )

        generateCategoryInfo(
            CommandCategory.ROBLOX,
            filterByCategory == CommandCategory.ROBLOX,
            "/v3/assets/img/categories/roblox.png",
            "(max-width: 1366px) 250px"
        )

        // Generate ads below the <hr> tag
        // Desktop
        fieldSet {
            legend {
                style = "margin-left: auto;"

                iconManager.ad.apply(this)
            }

            val zoneId = ZoneId.of("America/Sao_Paulo")
            val now = LocalDate.now(zoneId)

            // Discords.com
            // TODO: Proper sponsorship impl
            if (now.isBefore(LocalDate.of(2021, 9, 10))) {
                // A kinda weird workaround, but it works
                unsafe {
                    raw("""<a href="/sponsor/discords" target="_blank" class="sponsor-wrapper">
<div class="sponsor-pc-image"><img src="https://loritta.website/assets/img/sponsors/discords_pc.png?v2" class="sponsor-banner"></div>
<div class="sponsor-mobile-image"><img src="https://loritta.website/assets/img/sponsors/discords_mobile.png" class="sponsor-banner"></div>
</a>""")
                }
            } else {
                // Desktop Large
                generateNitroPayAd(
                    "commands-desktop-large",
                    listOf(
                        NitroPayAdSize(
                            728,
                            90
                        ),
                        NitroPayAdSize(
                            970,
                            90
                        ),
                        NitroPayAdSize(
                            970,
                            250
                        )
                    ),
                    mediaQuery = NitroPayAdGenerator.DESKTOP_LARGE_AD_MEDIA_QUERY
                )

                generateNitroPayAd(
                    "commands-desktop",
                    listOf(
                        NitroPayAdSize(
                            728,
                            90
                        )
                    ),
                    mediaQuery = NitroPayAdGenerator.RIGHT_SIDEBAR_DESKTOP_MEDIA_QUERY
                )

                // We don't do tablet here because there isn't any sizes that would fit a tablet comfortably
                generateNitroPayAd(
                    "commands-phone",
                    listOf(
                        NitroPayAdSize(
                            300,
                            250
                        ),
                        NitroPayAdSize(
                            320,
                            50
                        )
                    ),
                    mediaQuery = NitroPayAdGenerator.RIGHT_SIDEBAR_PHONE_MEDIA_QUERY
                )
            }
        }

        hr {}

        // First we are going to sort by category count
        // We change the first compare by to negative because we want it in a descending order (most commands in category -> less commands)
        for (command in publicCommands.sortedWith(compareBy({
            -(commands.groupBy { it.category }[it.category]?.size ?: 0)
        }, net.perfectdreams.loritta.cinnamon.showtime.backend.PublicApplicationCommands.InteractionCommand::category))) { // TODO: Fix sorting commands alphabetically
            val commandDescriptionKey = command.description
            // TODO: Fix this
            // val commandExamplesKey = command.examples
            val commandLabel = command.label

            // Additional command info (like images)
            val additionalInfo = showtimeBackend.publicApplicationCommands.additionalCommandsInfo[command.executor]

            val color = getCategoryColor(command.category)

            commandEntry {
                attributes["data-command-name"] = command.executor ?: "UnknownCommand"
                attributes["data-command-category"] = command.category.name

                style = if (filterByCategory == null || filterByCategory == command.category)
                    "display: block;"
                else
                    "display: none;"

                details(classes = "fancy-details") {
                    style = "line-height: 1.2; position: relative;"

                    summary {
                        commandCategoryTag {
                            style = "background-color: rgb(${color.red}, ${color.green}, ${color.blue});"
                            +(i18nContext.get(command.category.localizedName))
                        }

                        div {
                            style = "display: flex;align-items: center;"

                            div {
                                style = "flex-grow: 1; align-items: center;"
                                div {
                                    style = "display: flex;"

                                    commandLabel {
                                        style =
                                            "font-size: 1.5em; font-weight: bold; box-shadow: inset 0 0px 0 white, inset 0 -1px 0 rgb(${color.red}, ${color.green}, ${color.blue});"
                                        +"/"
                                        +(command.label.joinToString(" ") { i18nContext.get(it) })
                                    }
                                }

                                commandDescription {
                                    style = "word-wrap: anywhere;"
                                    +i18nContext.get(commandDescriptionKey)
                                }
                            }

                            div(classes = "chevron-icon") {
                                style = "font-size: 1.5em"
                                iconManager.chevronDown.apply(this)
                            }
                        }
                    }

                    div(classes = "details-content") {
                        style = "line-height: 1.4;"

                        // TODO: Fix this
                        if (additionalInfo != null) {
                            // Add additional images, if present
                            if (additionalInfo.imageUrls != null && additionalInfo.imageUrls.isNotEmpty()) {
                                for (imageUrl in additionalInfo.imageUrls) {
                                    val imageInfo = ImageUtils.getImageAttributes(imageUrl)

                                    img(src = imageInfo.path.removePrefix("static"), classes = "thumbnail") {
                                        // So, this is hard...
                                        // Because we are "floating" the image, content jumping is inevitable... (because the height is set to 0)
                                        // So we need to set a fixed width AND height, oof!
                                        // So we calculate it during build time and use it here, yay!
                                        // But anyway, this sucks...
                                        width = "250"

                                        // Lazy load the images, because *for some reason* it loads all images even tho the details tag is closed.
                                        attributes["loading"] = "lazy"

                                        // The aspect-ratio is used to avoid content reflow
                                        style = "aspect-ratio: ${imageInfo.width} / ${imageInfo.height};"
                                    }
                                }
                            }

                            // Add additional videos, if present
                            if (additionalInfo.videoUrls != null && additionalInfo.videoUrls.isNotEmpty()) {
                                for (videoUrl in additionalInfo.videoUrls) {
                                    video(classes = "thumbnail") {
                                        // For videos, we need to use the "preload" attribute to force the video to *not* preload
                                        // The video will only start loading after the user clicks the video
                                        attributes["preload"] = "none"

                                        // The aspect-ratio is used to avoid content reflow
                                        style = "aspect-ratio: 16 / 9;"

                                        width = "250"
                                        controls = true

                                        source {
                                            src = videoUrl
                                            type = "video/mp4"
                                        }
                                    }
                                }
                            }
                        }

                        // TODO: Fix this
                        /* if (command.aliases.isNotEmpty()) {
                            div {
                                b {
                                    style = "color: rgb(${color.red}, ${color.green}, ${color.blue});"
                                    +"Sinônimos: "
                                }

                                for ((index, a) in command.aliases.withIndex()) {
                                    if (index != 0) {
                                        +", "
                                    }

                                    code {
                                        +a
                                    }
                                }
                            }

                            hr {}
                        }

                        if (commandExamplesKey != null) {
                            div {
                                b {
                                    style = "color: rgb(${color.red}, ${color.green}, ${color.blue});"
                                    +"Exemplos: "
                                }

                                val examples = locale.getList(commandExamplesKey)

                                for (example in examples) {
                                    val split = example.split("|-|")
                                        .map { it.trim() }

                                    div {
                                        style = "padding-bottom: 8px;"
                                        if (split.size == 2) {
                                            // If the command has a extended description
                                            // "12 |-| Gira um dado de 12 lados"
                                            // A extended description can also contain "nothing", but contains a extended description
                                            // "|-| Gira um dado de 6 lados"
                                            val (commandExample, explanation) = split

                                            div {
                                                span {
                                                    style = "color: rgb(${color.red}, ${color.green}, ${color.blue});"

                                                    iconManager.smallDiamond.apply(this)
                                                }

                                                +" "

                                                b {
                                                    +explanation
                                                }
                                            }

                                            div {
                                                code {
                                                    +commandLabel

                                                    +" "

                                                    +commandExample
                                                }
                                            }
                                            // examples.add("\uD83D\uDD39 **$explanation**")
                                            // examples.add("`" + commandLabelWithPrefix + "`" + (if (commandExample.isEmpty()) "" else "**` $commandExample`**"))
                                        } else {
                                            val commandExample = split[0]

                                            div {
                                                +commandLabel

                                                +" "

                                                +commandExample
                                            }
                                        }
                                    }
                                }
                            }
                        } */
                    }
                }
            }
        }
    }

    fun getCategoryColor(category: CommandCategory) = when (category) {
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

    class COMMANDCATEGORY(consumer: TagConsumer<*>) :
        HTMLTag(
            "lori-command-category", consumer, emptyMap(),
            inlineTag = false,
            emptyTag = false
        ), HtmlBlockTag

    fun FlowOrInteractiveContent.commandCategory(block: COMMANDCATEGORY.() -> Unit = {}) {
        COMMANDCATEGORY(consumer).visit(block)
    }

    class COMMANDENTRY(consumer: TagConsumer<*>) :
        HTMLTag(
            "lori-command-entry", consumer, emptyMap(),
            inlineTag = false,
            emptyTag = false
        ), HtmlBlockTag

    fun FlowOrInteractiveContent.commandEntry(block: COMMANDENTRY.() -> Unit = {}) {
        COMMANDENTRY(consumer).visit(block)
    }

    fun FlowOrInteractiveContent.commandCategoryTag(block: HtmlBlockTag.() -> Unit = {}) =
        customHtmlTag("lori-command-category-tag", block)

    fun FlowOrInteractiveContent.commandLabel(block: HtmlBlockTag.() -> Unit = {}) =
        customHtmlTag("lori-command-label", block)

    fun FlowOrInteractiveContent.commandDescription(block: HtmlBlockTag.() -> Unit = {}) =
        customHtmlTag("lori-command-description", block)
}