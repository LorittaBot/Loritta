package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import com.microsoft.playwright.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.sticker.Sticker
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.discordchatmarkdownparser.*
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.MessageCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.messageCommand
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.extensions.getEffectiveAvatarUrl
import java.util.*

class SaveMessageCommand(val m: LorittaBot) : MessageCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Savemessage
    }

    override fun command() = messageCommand(I18N_PREFIX.Label, CommandCategory.DISCORD, SaveMessageExecutor(m)) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
    }

    class PlaywrightManager {
        val playwright = Playwright.create()
        val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(false))
        val browserContext = browser.newContext(Browser.NewContextOptions().setDeviceScaleFactor(4.0).setJavaScriptEnabled(false))
        val page: Page = browserContext.newPage()
        val mutex = Mutex()

        suspend inline fun <T> withPage(action: (Page) -> (T)): T {
            return mutex.withLock {
                action.invoke(page)
            }
        }
    }

    class SaveMessageExecutor(val m: LorittaBot) : LorittaMessageCommandExecutor() {
        val DiscordEmote = Regex("<(a)?:([a-zA-Z0-9_]+):([0-9]+)>")
        val DiscordCommand = Regex("</([-_\\p{L}\\p{N}\\p{sc=Deva}\\p{sc=Thai} ]+):([0-9]+)>")
        val DiscordChannel = Regex("<#([0-9]+)>")
        val DiscordUserMention = Regex("<@!?([0-9]+)>")
        val DiscordRole = Regex("<@&([0-9]+)>")
        val DiscordEveryone = Regex("@everyone")
        val DiscordHere = Regex("@here")

        val playwrightManager = PlaywrightManager()

        override suspend fun execute(context: ApplicationCommandContext, message: Message) {
            context.deferChannelMessage(false)

            val markdownParser = DiscordChatMarkdownParser()

            // TODO: it would be better to create a single playwright instance and reuse it (TODO: BUT DON'T ACCESS IT IN MULTIPLE THREADS TO AVOID ISSUES!)

            val screenshot = playwrightManager.withPage { page ->
                page.setContent(
                    createHTML()
                        .html {
                            head {
                                link(href = "https://fonts.googleapis.com/css2?family=Lato:ital,wght@0,100;0,300;0,400;0,700;0,900;1,100;1,300;1,400;1,700;1,900&display=swap&family=Pacifico&display=swap&family=JetBrains+Mono:ital,wght@0,100..800;1,100..800&display=swap", type = "stylesheet")
                                style {
                                    unsafe {
                                        raw(LorittaBot::class.java.getResourceAsStream("/message-renderer-assets/style.css").readAllBytes().toString(Charsets.UTF_8))
                                    }
                                }
                            }

                            body {
                                div(classes = "loritta-fancy-preview") {
                                    id = "wrapper"

                                    div(classes = "discord-message") {
                                        div(classes = "discord-message-sidebar") {
                                            img(
                                                src = message.author.getEffectiveAvatarUrl(ImageFormat.PNG),
                                                classes = "discord-message-avatar"
                                            )
                                        }

                                        div(classes = "discord-message-content") {
                                            div(classes = "discord-message-header") {
                                                val memberRoleColor = message.member?.color
                                                // JDA does not have a getIcon yet... so let's do what .color does but with icons
                                                val memberRoleIconUrl = message.member?.roles?.firstNotNullOfOrNull {
                                                    it.icon?.icon?.getUrl(64)
                                                }

                                                span(classes = "discord-message-username") {
                                                    if (memberRoleColor != null) {
                                                        style =
                                                            "color: rgb(${memberRoleColor.red}, ${memberRoleColor.green}, ${memberRoleColor.blue});"
                                                    }

                                                    val displayName =
                                                        message.member?.nickname ?: message.author.globalName
                                                        ?: message.author.name
                                                    text(displayName)

                                                    // text("MrPowerGamerBR\uD83D\uDE18|\uD83D\uDC81Criador da Lori")
                                                }

                                                if (memberRoleIconUrl != null) {
                                                    img(src = memberRoleIconUrl) {
                                                        width = "20"
                                                        height = "20"
                                                        style = "object-fit: contain;"
                                                    }
                                                }

                                                /* span(classes = "discord-message-bot-tag") {
                                                    text("APP")
                                                } */

                                                span(classes = "discord-message-timestamp") {
                                                    val timeCreated =
                                                        message.timeCreated.atZoneSameInstant(Constants.LORITTA_TIMEZONE)

                                                    text(
                                                        buildString {
                                                            append(
                                                                timeCreated.dayOfMonth.toString().padStart(2, '0')
                                                            )
                                                            append("/")
                                                            append(
                                                                timeCreated.monthValue.toString().padStart(2, '0')
                                                            )
                                                            append("/")
                                                            append(timeCreated.year)

                                                            append(" ")

                                                            append(timeCreated.hour.toString().padStart(2, '0'))
                                                            append(":")
                                                            append(timeCreated.minute.toString().padStart(2, '0'))
                                                            append(":")
                                                            append(timeCreated.second.toString().padStart(2, '0'))
                                                        }
                                                    )
                                                    // text("Today at 09:07")
                                                }
                                            }

                                            fun parseContentToHTML(input: String): ChatRootNode {
                                                println("Input: \"$input\"")

                                                val modifiedInput =
                                                // We can't escape HTML entities here because that will also escape emojis and stuff
                                                // This isn't a huge problem tho, but maybe we should check if HTML codeblocks won't break
                                                    // Maybe we should bring back the private unicode use hack to map Discord entities -> private unicode stuff -> convert them to HTML tags
                                                    input
                                                // .replace("&", "&amp;")
                                                // .replace("<", "&lt;")
                                                // .replace(">", "&gt;")
                                                // .replace("\"", "&quot;")
                                                // .replace("'", "&#39;")
                                                /* .replace(DiscordEmote) {
                                                    val animated = it.groupValues[1] == "a"
                                                    val emoteName = it.groupValues[2]
                                                    val emoteId = it.groupValues[3]

                                                    createHTML().span {
                                                        attributes["magic-type"] = "discord-emoji"

                                                        attributes["animated"] = animated.toString()
                                                        attributes["name"] = emoteName
                                                        attributes["id"] = emoteId
                                                    }
                                                }
                                                .replace(DiscordCommand) {
                                                    createHTML().span {
                                                        attributes["magic-type"] = "discord-command"

                                                        attributes["label"] = it.groupValues[1]
                                                    }
                                                }
                                                .replace(DiscordUserMention) {
                                                    createHTML().span {
                                                        attributes["magic-type"] = "discord-user-mention"
                                                        val userId = it.groupValues[1]
                                                        attributes["user-id"] = userId

                                                        val mentionedUser = message.mentions.users.firstOrNull { it.id == userId }
                                                        val mentionedUserEffectiveName = mentionedUser?.effectiveName
                                                        if (mentionedUserEffectiveName != null)
                                                            attributes["user-display-name"] = mentionedUserEffectiveName
                                                    }
                                                } */

                                                // parse the content as markdown
                                                val node = markdownParser.parse(
                                                    modifiedInput
                                                )

                                                // println("Content as nodes: $nodes")

                                                // and then parse the content as HTML!
                                                return node as ChatRootNode
                                            }

                                            fun traverseNodesAndRender(element: MarkdownNode) {
                                                when (element) {
                                                    is CompositeMarkdownNode -> {
                                                        when (element) {
                                                            is BoldNode -> {
                                                                b {
                                                                    for (children in element.children) {
                                                                        traverseNodesAndRender(children)
                                                                    }
                                                                }
                                                            }

                                                            is ItalicsNode -> {
                                                                i {
                                                                    for (children in element.children) {
                                                                        traverseNodesAndRender(children)
                                                                    }
                                                                }
                                                            }

                                                            is CodeBlockNode -> {
                                                                pre {
                                                                    for (children in element.children) {
                                                                        traverseNodesAndRender(children)
                                                                    }
                                                                }
                                                            }

                                                            is HeaderNode -> {
                                                                when (element.level) {
                                                                    1 -> {
                                                                        h1 {
                                                                            for (children in element.children) {
                                                                                traverseNodesAndRender(children)
                                                                            }
                                                                        }
                                                                    }

                                                                    2 -> {
                                                                        h2 {
                                                                            for (children in element.children) {
                                                                                traverseNodesAndRender(children)
                                                                            }
                                                                        }
                                                                    }

                                                                    3 -> {
                                                                        h3 {
                                                                            for (children in element.children) {
                                                                                traverseNodesAndRender(children)
                                                                            }
                                                                        }
                                                                    }

                                                                    else -> error("Unsupported header level ${element.level}")
                                                                }
                                                            }

                                                            is InlineCodeNode -> {
                                                                code {
                                                                    for (children in element.children) {
                                                                        traverseNodesAndRender(children)
                                                                    }
                                                                }
                                                            }

                                                            is StrikethroughNode -> {
                                                                span {
                                                                    style = "text-decoration: line-through;"

                                                                    for (children in element.children) {
                                                                        traverseNodesAndRender(children)
                                                                    }
                                                                }
                                                            }

                                                            is BlockQuoteNode -> {
                                                                div {
                                                                    style = "display: flex; gap: 0.5em;"

                                                                    div {
                                                                        style =
                                                                            "width: 4px; border-radius: 4px; background-color: #4e5058;"
                                                                    }

                                                                    div {
                                                                        for (children in element.children) {
                                                                            traverseNodesAndRender(children)
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            is SubTextNode -> {
                                                                div {
                                                                    for (children in element.children) {
                                                                        traverseNodesAndRender(children)
                                                                    }
                                                                }
                                                            }

                                                            is MaskedLinkNode -> {
                                                                // No need to point it to the real URL
                                                                a(href = "#") {
                                                                    for (children in element.children) {
                                                                        traverseNodesAndRender(children)
                                                                    }
                                                                }
                                                            }

                                                            else -> {
                                                                // Unknown/Unparsed node, just loop thru the child nodes
                                                                for (children in element.children) {
                                                                    traverseNodesAndRender(children)
                                                                }
                                                            }
                                                        }
                                                    }

                                                    is LeafMarkdownNode -> {
                                                        when (element) {
                                                            is TextNode -> {
                                                                println(
                                                                    "Adding text \"${
                                                                        element.text.replace(
                                                                            "\n",
                                                                            "*new line*"
                                                                        )
                                                                    }\""
                                                                )

                                                                val texts = element.text.split("\n")

                                                                var index = 0

                                                                for (character in element.text) {
                                                                    if (character == '\n') {
                                                                        br {}
                                                                    } else {
                                                                        text(character.toString())
                                                                    }
                                                                }
                                                            }

                                                            is LinkNode -> {
                                                                a(href = "#") { // No need to point it to the real URL
                                                                    text(element.url)
                                                                }
                                                            }

                                                            is CodeTextNode -> {
                                                                text(element.text)
                                                            }

                                                            is DiscordEmojiEntityNode -> {
                                                                val animated = element.animated
                                                                val emoteName = element.name
                                                                val emoteId = element.id

                                                                val extension =
                                                                    "png" // Always png because... you know, if it is a screenshot it doesn't matter lol

                                                                img(
                                                                    src = "https://cdn.discordapp.com/emojis/$emoteId.$extension?v=1",
                                                                    classes = "discord-inline-emoji"
                                                                )
                                                            }

                                                            is DiscordCommandEntityNode -> {
                                                                val id = element.id
                                                                val path = element.path

                                                                span(classes = "discord-mention") {
                                                                    text("/")
                                                                    text(path)
                                                                }
                                                            }

                                                            is DiscordUserMentionEntityNode -> {
                                                                val userId = element.id

                                                                span(classes = "discord-mention") {
                                                                    val mentionedUser =
                                                                        message.mentions.users.firstOrNull { it.idLong == userId }
                                                                    val mentionedUserEffectiveName =
                                                                        mentionedUser?.effectiveName

                                                                    val userDisplayName =
                                                                        mentionedUserEffectiveName ?: "???"

                                                                    text("@$userDisplayName ($userId)")
                                                                }
                                                            }

                                                            is DiscordEveryoneMentionEntityNode -> {
                                                                span(classes = "discord-mention") {
                                                                    text("@everyone")
                                                                }
                                                            }

                                                            is DiscordHereMentionEntityNode -> {
                                                                span(classes = "discord-mention") {
                                                                    text("@here")
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                return
                                                /* if (element is TextNode) {

                                                } else {
                                                    if (element is Element) {
                                                        val tagName = element.tagName()
                                                        println("Element is $tagName")
                                                        when (tagName) {
                                                            "strong" -> {
                                                                strong {
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            "h1" -> {
                                                                h1 {
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            "h2" -> {
                                                                h2 {
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            "h3" -> {
                                                                h3 {
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            "em" -> {
                                                                em {
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            "del" -> {
                                                                del {
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            // Line break (brazil lmao)
                                                            "br" -> {
                                                                br {}
                                                            }
                                                            "p" -> {
                                                                p {
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            "a" -> {
                                                                a(href = "#") { // No need to point somewhere
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            "blockquote" -> {
                                                                div {
                                                                    style = "display: flex; gap: 0.5em;"

                                                                    div {
                                                                        style = "width: 4px; border-radius: 4px; background-color: #4e5058;"
                                                                    }

                                                                    div {
                                                                        for (node in element.childNodes()) {
                                                                            traverseNodesAndRender(node)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            "ul" -> {
                                                                ul {
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            "ol" -> {
                                                                ol {
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            "li" -> {
                                                                // A bit of an hack because kotlinx.html does not let you create li tags whenever
                                                                LI(attributesMapOf("class", null), consumer).visit {
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            "code" -> {
                                                                code {
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            "pre" -> {
                                                                pre {
                                                                    for (node in element.childNodes()) {
                                                                        traverseNodesAndRender(node)
                                                                    }
                                                                }
                                                            }
                                                            "span" -> {
                                                                val magicTypeAttribute = element.attr("magic-type")

                                                                when (magicTypeAttribute) {
                                                                    "discord-emoji" -> {
                                                                        val animated = element.attr("animated").toBoolean()
                                                                        val emoteName = element.attr("name")
                                                                        val emoteId = element.attr("id")

                                                                        val extension = "png" // Always png because... you know, if it is a screenshot it doesn't matter lol

                                                                        img(src = "https://cdn.discordapp.com/emojis/$emoteId.$extension?v=1", classes = "discord-inline-emoji")
                                                                    }
                                                                    "discord-command" -> {
                                                                        val label = element.attr("label")

                                                                        span(classes = "discord-mention") {
                                                                            text("/")
                                                                            text(label)
                                                                        }
                                                                    }
                                                                    "discord-user-mention" -> {
                                                                        val userId = element.attr("user-id")

                                                                        span(classes = "discord-mention") {
                                                                            val userDisplayName = if (element.hasAttr("user-display-name")) {
                                                                                element.attr("user-display-name")
                                                                            } else "???"

                                                                            text("@$userDisplayName ($userId)")
                                                                        }
                                                                    }
                                                                    else -> {
                                                                        // Unknown magic type name, just loop thru the child nodes
                                                                        for (node in element.childNodes()) {
                                                                            traverseNodesAndRender(node)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            else -> {
                                                                // Unknown tag name, just loop thru the child nodes
                                                                for (node in element.childNodes()) {
                                                                    traverseNodesAndRender(node)
                                                                }
                                                            }
                                                        }
                                                    }
                                                } */
                                            }

                                            div(classes = "discord-message-text") {
                                                if (message.contentRaw.isNotEmpty()) {
                                                    val contentAsDocument = parseContentToHTML(message.contentRaw)
                                                    traverseNodesAndRender(contentAsDocument)
                                                    /* for (line in message.contentRaw.lines()) {
                                                    div {
                                                        if (line.isNotBlank()) {
                                                            text(line)
                                                        } else {
                                                            unsafe {
                                                                raw("&nbsp;")
                                                            }
                                                        }
                                                    }
                                                } */
                                                } else {
                                                    if (message.isEdited) {
                                                        div {
                                                            text("(editado)")
                                                        }
                                                    }
                                                }

                                                div(classes = "discord-message-accessories") {
                                                    for (embed in message.embeds) {
                                                        println("Embed Type is ${embed.type}")

                                                        // There are multiple embeds type on Discord, so we need to handle it differently
                                                        when (embed.type) {
                                                            EmbedType.RICH, EmbedType.UNKNOWN, EmbedType.LINK -> {
                                                                // RICH is the good old embeds sent by bots
                                                                // LINK is links like https://google.com/ - anything that has a THUMBNAIL
                                                                // UNKNOWN is also used for links like this one: https://mrpowergamerbr.com/en/blog/2024-06-21-downgrading-paper-1-21-to-1-20-6
                                                                // The only visible difference between UNKNOWN/LINK and RICH, is that thumbnails are actually images
                                                                article(classes = "discord-embed") {
                                                                    val embedColor = embed.color
                                                                    if (embedColor != null) {
                                                                        style =
                                                                            "border-color: rgb(${embedColor.red}, ${embedColor.green}, ${embedColor.blue});"
                                                                    }

                                                                    div(classes = "discord-embed-content") {
                                                                        // ===[ EMBED AUTHOR ]===
                                                                        val embedAuthor = embed.author
                                                                        if (embedAuthor != null) {
                                                                            val authorIconUrl = embedAuthor.proxyIconUrl
                                                                            val authorUrl = embedAuthor.url
                                                                            val authorName = embedAuthor.name

                                                                            div(classes = "discord-embed-author") {
                                                                                if (authorIconUrl != null) {
                                                                                    img(
                                                                                        classes = "discord-embed-icon",
                                                                                        src = authorIconUrl
                                                                                    )
                                                                                }

                                                                                if (authorName != null) {
                                                                                    val content =
                                                                                        parseContentToHTML(authorName)

                                                                                    if (authorUrl != null) {
                                                                                        a(
                                                                                            href = "#",
                                                                                            classes = "discord-embed-text"
                                                                                        ) {
                                                                                            traverseNodesAndRender(
                                                                                                content
                                                                                            )
                                                                                        }
                                                                                    } else {
                                                                                        span(classes = "discord-embed-text") {
                                                                                            traverseNodesAndRender(
                                                                                                content
                                                                                            )
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }

                                                                        // ===[ EMBED TITLE ]===
                                                                        val embedTitle = embed.title
                                                                        val embedTitleUrl = embed.url
                                                                        if (embedTitle != null) {
                                                                            if (embedTitleUrl != null) {
                                                                                a(
                                                                                    href = "#",
                                                                                    classes = "discord-embed-title"
                                                                                ) {
                                                                                    traverseNodesAndRender(
                                                                                        parseContentToHTML(embedTitle)
                                                                                    )
                                                                                }
                                                                            } else {
                                                                                div(classes = "discord-embed-title") {
                                                                                    traverseNodesAndRender(
                                                                                        parseContentToHTML(embedTitle)
                                                                                    )
                                                                                }
                                                                            }
                                                                        }

                                                                        // ===[ EMBED DESCRIPTION ]===
                                                                        val embedDescription = embed.description
                                                                        if (embedDescription != null) {
                                                                            div(classes = "discord-embed-description") {
                                                                                traverseNodesAndRender(
                                                                                    parseContentToHTML(
                                                                                        embedDescription
                                                                                    )
                                                                                )
                                                                            }
                                                                        }

                                                                        // ===[ EMBED FIELDS ]===
                                                                        if (embed.fields.isNotEmpty()) {
                                                                            div(classes = "discord-embed-fields") {
                                                                                // Rendering *inline* fields is hard as fucc
                                                                                // We know that there can be at *maximum* three inline fields in a row in a embed
                                                                                // So, if we want to place everything nicely, we need to keep track of the previous and next
                                                                                // inline fields.
                                                                                // After all...
                                                                                // [inline field]
                                                                                // [inline field]
                                                                                // [field]
                                                                                // [inline field]
                                                                                // [inline field]
                                                                                // [inline field]
                                                                                // should be displayed as
                                                                                // [inline field] [inline field]
                                                                                // [field]
                                                                                // [inline field] [inline field] [inline field]
                                                                                // So, to do that, let's split up everything in different chunks, inlined and non inlined chunks
                                                                                val chunks =
                                                                                    mutableListOf<MutableList<MessageEmbed.Field>>()

                                                                                for (field in embed.fields) {
                                                                                    val currentChunk =
                                                                                        chunks.lastOrNull() ?: run {
                                                                                            val newList =
                                                                                                mutableListOf<MessageEmbed.Field>()
                                                                                            chunks.add(newList)
                                                                                            newList
                                                                                        }

                                                                                    if (currentChunk.firstOrNull()?.isInline != field.isInline) {
                                                                                        // New chunk needs to be created!
                                                                                        val newList =
                                                                                            mutableListOf<MessageEmbed.Field>()
                                                                                        newList.add(field)
                                                                                        chunks.add(newList)
                                                                                    } else {
                                                                                        // Same type, so we are going to append to the current chunk
                                                                                        currentChunk.add(field)
                                                                                    }
                                                                                }

                                                                                var fieldIndex = 0
                                                                                for (fieldChunk in chunks) {
                                                                                    // Because fields are grouped by three, we are going to chunk again
                                                                                    val groupedFields =
                                                                                        fieldChunk.chunked(3)

                                                                                    for (fieldGroup in groupedFields) {
                                                                                        for ((index, field) in fieldGroup.withIndex()) {
                                                                                            div(classes = "discord-embed-field") {
                                                                                                style =
                                                                                                    if (!field.isInline) "grid-column: 1 / 13;" else {
                                                                                                        if (fieldGroup.size == 3) {
                                                                                                            when (index) {
                                                                                                                2 -> "grid-column: 9 / 13;"
                                                                                                                1 -> "grid-column: 5 / 9;"
                                                                                                                else -> "grid-column: 1 / 5;"
                                                                                                            }
                                                                                                        } else {
                                                                                                            when (index) {
                                                                                                                1 -> "grid-column: 7 / 13;"
                                                                                                                else -> "grid-column: 1 / 7;"
                                                                                                            }
                                                                                                        }
                                                                                                    }

                                                                                                div(classes = "discord-embed-field-name") {
                                                                                                    traverseNodesAndRender(
                                                                                                        parseContentToHTML(
                                                                                                            field.name
                                                                                                                ?: ""
                                                                                                        )
                                                                                                    )
                                                                                                }

                                                                                                div(classes = "discord-embed-field-value") {
                                                                                                    traverseNodesAndRender(
                                                                                                        parseContentToHTML(
                                                                                                            field.value
                                                                                                                ?: ""
                                                                                                        )
                                                                                                    )
                                                                                                }
                                                                                            }
                                                                                            fieldIndex++
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }

                                                                        if (embed.type != EmbedType.UNKNOWN) {
                                                                            val embedImage = embed.image?.proxyUrl
                                                                            if (embedImage != null) {
                                                                                div(classes = "discord-embed-image") {
                                                                                    img(src = embedImage) {
                                                                                        style = "width: 100%;"
                                                                                    }
                                                                                }
                                                                            }
                                                                        } else {
                                                                            val embedImage = embed.thumbnail?.proxyUrl
                                                                            if (embedImage != null) {
                                                                                div(classes = "discord-embed-image") {
                                                                                    img(src = embedImage) {
                                                                                        style = "width: 100%;"
                                                                                    }
                                                                                }
                                                                            }
                                                                        }

                                                                        // ===[ EMBED FOOTER ]===
                                                                        val footer = embed.footer
                                                                        if (footer != null) {
                                                                            div(classes = "discord-embed-footer") {
                                                                                val footerIconUrl = footer.proxyIconUrl
                                                                                val footerText = footer.text
                                                                                if (footerIconUrl != null) {
                                                                                    img(
                                                                                        src = footerIconUrl,
                                                                                        classes = "discord-embed-footer-icon"
                                                                                    )
                                                                                }

                                                                                if (footerText != null) {
                                                                                    span(classes = "discord-embed-footer-text") {
                                                                                        traverseNodesAndRender(
                                                                                            parseContentToHTML(
                                                                                                footerText
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                    if (embed.type != EmbedType.UNKNOWN) {
                                                                        val embedThumbnail = embed.thumbnail?.proxy?.url
                                                                        if (embedThumbnail != null) {
                                                                            a(
                                                                                href = "#",
                                                                                classes = "discord-embed-thumbnail"
                                                                            ) {
                                                                                img(src = embedThumbnail)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            EmbedType.IMAGE -> {}
                                                            EmbedType.VIDEO -> {}
                                                            EmbedType.AUTO_MODERATION -> {}
                                                        }
                                                    }

                                                    // These are the image embeds that show up if the message is only a single link
                                                    for (embed in message.embeds.filter { it.type == EmbedType.IMAGE }) {
                                                        val embedThumbnailUrl = embed.thumbnail?.proxyUrl

                                                        if (embedThumbnailUrl != null) {
                                                            div {
                                                                img(src = embedThumbnailUrl) {
                                                                    style = "max-width: 500px; height: auto;"
                                                                }
                                                            }
                                                        }
                                                    }

                                                    for (attachment in message.attachments) {
                                                        div {
                                                            if (attachment.isImage) {
                                                                img(src = attachment.url) {
                                                                    style = "max-width: 500px; height: auto;"
                                                                }
                                                            } else {
                                                                // TODO: Fallback if attachment is not supported
                                                            }
                                                        }
                                                    }

                                                    for (sticker in message.stickers) {
                                                        div {
                                                            if (sticker.formatType == Sticker.StickerFormat.LOTTIE) {
                                                                // TODO: Lottie stickers fallback
                                                                text(sticker.name)
                                                            } else {
                                                                img(src = sticker.iconUrl) {
                                                                    width = "240"
                                                                    height = "240"
                                                                }
                                                            }
                                                        }
                                                    }
                                                    // text("A Loritta é muito fofa e muito legal!")
                                                }
                                            }
                                        }
                                    }

                                    div {
                                        style = "display: grid;grid-template-columns: 200px 1fr; align-items: self-start;"

                                        div {
                                            style = "font-size: 0.8em; display: flex; flex-direction: column; gap: 0.5em;"

                                            div {
                                                div {
                                                    style = "font-weight: bold;"
                                                    text("ID do usuário")
                                                }

                                                div {
                                                    text("${message.author.idLong}")
                                                }
                                            }

                                            if (message.guildIdLong != 0L) {
                                                div {
                                                    div {
                                                        style = "font-weight: bold;"
                                                        text("ID do servidor")
                                                    }

                                                    div {
                                                        text("${message.guildIdLong}")
                                                    }
                                                }
                                            }

                                            println("userIntegration: ${context.event.interaction.integrationOwners.userIntegration}")
                                            println("guildIntegration: ${context.event.interaction.integrationOwners.guildIntegration}")


                                            div {
                                                div {
                                                    style = "font-weight: bold;"
                                                    text("ID do canal")
                                                }

                                                div {
                                                    text("${message.channelIdLong}")
                                                }
                                            }

                                            div {
                                                div {
                                                    style = "font-weight: bold;"
                                                    text("ID da mensagem")
                                                }

                                                div {
                                                    text("${message.idLong}")
                                                }
                                            }
                                        }

                                        div(classes = "message-saved-by-loritta") {
                                            div(classes = "message-saved-by-loritta-info") {
                                                div {
                                                    style = "font-size: 1.25em;"
                                                    text("Feito com a ")
                                                    span {
                                                        style = "font-family: Pacifico; color: #29a6fe;"
                                                        text("Loritta")
                                                    }
                                                }

                                                div {
                                                    style = "font-size: 0.75em;"
                                                    text("Desconfiado? Verifique se esta mensagem realmente existiu com ")

                                                    span(classes = "discord-mention") {
                                                        style = "display: inline-block;"
                                                        text("/verificarmensagem LinkDestaImagem")
                                                    }
                                                }
                                            }
                                            div {
                                                img(src = "https://cdn.discordapp.com/emojis/1167125529024012389.png?size=160&quality=lossless") {
                                                    style = "height: 3em;"
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                )
                // val screenshot = page.screenshot(Page.ScreenshotOptions().setFullPage(true))
                page.querySelector("#wrapper").screenshot(ElementHandle.ScreenshotOptions())
            }
            val b64Encoded = Base64.getEncoder().encodeToString(Json.encodeToString(MagicMessage(message.author.idLong, message.contentRaw)).toByteArray(Charsets.UTF_8))
            val finalImage = addChunkToPng(screenshot, createChunk("tEXt", "LORIMESSAGEDATA:${b64Encoded}".toByteArray(Charsets.US_ASCII)))

            // File("data.png").writeBytes(screenshot)

            // playwright.close()

            context.reply(false) {
                content = "Mensagem salva!"

                files += FileUpload.fromData(finalImage, "message.png")
            }
        }
    }
}