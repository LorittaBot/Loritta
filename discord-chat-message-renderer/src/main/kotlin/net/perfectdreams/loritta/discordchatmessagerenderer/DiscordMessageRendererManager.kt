package net.perfectdreams.loritta.discordchatmessagerenderer

import com.microsoft.playwright.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.sticker.Sticker
import net.perfectdreams.loritta.discordchatmarkdownparser.*
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.*
import java.awt.Color
import java.io.Closeable
import java.time.ZoneId
import java.util.*
import kotlin.time.measureTimedValue

class DiscordMessageRendererManager(
    /**
     * The [ZoneId] used when rendering dates
     */
    private val zoneId: ZoneId,
    /**
     * The MIME type of attachments that are considered images
     */
    private val imageContentTypes: Set<String>,
) : Closeable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val playwright = Playwright.create()
    // Firefox has an issue in headless more where there is a white space at the bottom of the screenshot...
    // Chromium has an issue where screenshots >16384 are "corrupted"
    private val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
    private val deviceScale = 2.0
    private val maxDimensionsOfImages = (16_384 / deviceScale).toInt()
    private val browserContext = browser.newContext(Browser.NewContextOptions().setDeviceScaleFactor(deviceScale).setJavaScriptEnabled(false))
    private val mutex = Mutex()
    private val markdownParser = DiscordChatMarkdownParser()

    private fun createPage(): Page {
        val newPage = browserContext.newPage()
        newPage.onCrash {
            // The reason we don't attempt to withPage lock it, is because this seems to create a deadlock because the onCrash handler is triggered within the rendering call
            // Failsafe if a page crashes
            logger.error { "Page $it crashed! Is locked? ${mutex.isLocked} - Closing page..." }
            // We won't attempt to close the page, because this should be handled by our try finally in the "withPage" call
        }
        return newPage
    }

    private suspend inline fun <T> withPage(action: (Page) -> (T)): T {
        return mutex.withLock {
            // We create a new page every time because we are experiencing random "Page has crashed" issues, and it seems that it is correlated to reusing pages?
            // (maybe the page memory is never freed and Chromium crashes?)
            val page = createPage()
            page.use {
                action.invoke(it)
            }
        }
    }

    suspend fun renderMessage(
        savedMessage: SavedMessage,
        qrCodeImageAsByteArray: ByteArray?
    ): ByteArray {
        logger.info { "Rendering message ${savedMessage.id}... Is locked? ${mutex.isLocked}" }

        val screenshot = withPage { page ->
            // We somewhat piggy back the withPage lock here
            // The page count should NEVER be != 1!
            logger.info { "Starting to render message ${savedMessage.id}! - Open pages: ${browserContext.pages().size}" }
            val placeContext = savedMessage.placeContext

            val timedValue = measureTimedValue {
                page.setContent(
                    createHTML()
                        .html {
                            head {
                                link(
                                    href = "https://fonts.googleapis.com/css2?family=Lato:ital,wght@0,100;0,300;0,400;0,700;0,900;1,100;1,300;1,400;1,700;1,900&display=swap&family=Pacifico&display=swap&family=JetBrains+Mono:ital,wght@0,100..800;1,100..800&display=swap",
                                    rel = "stylesheet"
                                )
                                style {
                                    unsafe {
                                        raw(
                                            DiscordMessageRendererManager::class.java.getResourceAsStream("/message-renderer-assets/style.css")
                                                .readAllBytes().toString(Charsets.UTF_8)
                                        )
                                    }
                                }
                            }

                            body {
                                div(classes = "loritta-fancy-preview") {
                                    id = "wrapper"

                                    div(classes = "discord-message-sent-at-location") {
                                        // We need to detect which type of thing we are dealing with

                                        if (placeContext is SavedGroupChannel) {
                                            val iconUrl = placeContext.getIconUrl(64)
                                            if (iconUrl != null) {
                                                img(src = iconUrl) {
                                                    height = "20"
                                                    width = "20"
                                                    style = "border-radius: 99999px; background-color: #1e1f22;"
                                                }
                                            } else {
                                                div {
                                                    style = "border-radius: 99999px; width: 20px; height: 20px; background-color: #1e1f22;"
                                                }
                                            }

                                            div {
                                                text("Mensagem enviada no grupo ")
                                                b {
                                                    text(placeContext.name)
                                                }
                                            }
                                        } else if (placeContext is SavedPrivateChannel) {
                                            // While there is a "channel.name" in the PrivateChannel, it seems that it always returns an empty string, even when you are using in another user's DMs
                                            div {
                                                style = "border-radius: 99999px; width: 20px; height: 20px; background-color: #1e1f22;"
                                            }

                                            div {
                                                text("Mensagem enviada no privado")
                                            }
                                        } else if (placeContext is SavedGuild) {
                                            when (placeContext) {
                                                is SavedDetachedGuild -> {
                                                    div {
                                                        style = "border-radius: 99999px; width: 20px; height: 20px; background-color: #1e1f22;"
                                                    }

                                                    div {
                                                        style = """display: flex; white-space: pre-wrap; align-items: center;"""

                                                        span {
                                                            text("Mensagem enviada no servidor de ID ")
                                                            b {
                                                                text(placeContext.id)
                                                            }
                                                            text(" no canal ")
                                                        }
                                                        span(classes = "discord-mention discord-channel") {
                                                            unsafe {
                                                                raw(getSVGForChannelType(placeContext.channelType))
                                                            }
                                                            text(placeContext.channelName)
                                                        }
                                                    }
                                                }
                                                is SavedAttachedGuild -> {
                                                    val iconUrl = placeContext.getIconUrl(64, ImageFormat.PNG)
                                                    if (iconUrl != null) {
                                                        img(src = iconUrl) {
                                                            height = "20"
                                                            width = "20"
                                                            style = "border-radius: 99999px; background-color: #1e1f22;"
                                                        }
                                                    } else {
                                                        div {
                                                            style = "border-radius: 99999px; width: 20px; height: 20px; background-color: #1e1f22;"
                                                        }
                                                    }

                                                    div {
                                                        style = """display: flex; white-space: pre-wrap; align-items: center;"""

                                                        span {
                                                            text("Mensagem enviada no servidor ")
                                                            b {
                                                                text(placeContext.name)
                                                            }
                                                            text(" no canal ")
                                                        }
                                                        span(classes = "discord-mention discord-channel") {
                                                            unsafe {
                                                                raw(getSVGForChannelType(placeContext.channelType))
                                                            }
                                                            text(placeContext.channelName)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    div(classes = "discord-message") {
                                        div(classes = "discord-message-sidebar") {
                                            img(
                                                src = savedMessage.author.getEffectiveAvatarUrl(ImageFormat.PNG, 128),
                                                classes = "discord-message-avatar"
                                            )
                                        }

                                        div(classes = "discord-message-content") {
                                            div(classes = "discord-message-header") {
                                                val memberRoleColor = savedMessage.member?.color
                                                val memberRoleIconUrl = savedMessage.member?.iconUrl

                                                span(classes = "discord-message-username") {
                                                    if (memberRoleColor != null) {
                                                        style = "color: rgb(${memberRoleColor.red}, ${memberRoleColor.green}, ${memberRoleColor.blue});"
                                                    }

                                                    val displayName = savedMessage.member?.nickname ?: savedMessage.author.globalName ?: savedMessage.author.name
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

                                                if (savedMessage.author.isSystem) {
                                                    span(classes = "discord-message-bot-tag") {
                                                        unsafe {
                                                            raw("""<svg aria-label="App Verificado" aria-hidden="false" role="img" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><path fill="currentColor" fill-rule="evenodd" d="M19.06 6.94a1.5 1.5 0 0 1 0 2.12l-8 8a1.5 1.5 0 0 1-2.12 0l-4-4a1.5 1.5 0 0 1 2.12-2.12L10 13.88l6.94-6.94a1.5 1.5 0 0 1 2.12 0Z" clip-rule="evenodd" class=""></path></svg>""")
                                                        }
                                                        text(" ")
                                                        text("OFICIAL")
                                                    }
                                                } else if (savedMessage.author.isBot) {
                                                    span(classes = "discord-message-bot-tag") {
                                                        if (savedMessage.author.flags.contains(User.UserFlag.VERIFIED_BOT)) {
                                                            unsafe {
                                                                raw("""<svg aria-label="App Verificado" aria-hidden="false" role="img" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><path fill="currentColor" fill-rule="evenodd" d="M19.06 6.94a1.5 1.5 0 0 1 0 2.12l-8 8a1.5 1.5 0 0 1-2.12 0l-4-4a1.5 1.5 0 0 1 2.12-2.12L10 13.88l6.94-6.94a1.5 1.5 0 0 1 2.12 0Z" clip-rule="evenodd" class=""></path></svg>""")
                                                            }
                                                            text(" ")
                                                        }
                                                        text("APP")
                                                    }
                                                }

                                                span(classes = "discord-message-timestamp") {
                                                    val timeCreated = savedMessage.timeCreated.atZoneSameInstant(zoneId)

                                                    text(
                                                        buildString {
                                                            append(timeCreated.dayOfMonth.toString().padStart(2, '0'))
                                                            append("/")
                                                            append(timeCreated.monthValue.toString().padStart(2, '0'))
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

                                            fun parseMarkdownToNodes(input: String): ChatRootNode {
                                                // Parse the content as markdown
                                                val node = markdownParser.parse(input)

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

                                                                val extension = "png" // Always png because... you know, if it is a screenshot it doesn't matter lol

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
                                                                    val mentionedUser = savedMessage.mentions.users.firstOrNull { it.id == userId }
                                                                    val mentionedUserEffectiveName = mentionedUser?.effectiveName

                                                                    val userDisplayName = mentionedUserEffectiveName ?: "???"

                                                                    text("@$userDisplayName ($userId)")
                                                                }
                                                            }

                                                            is DiscordRoleMentionEntityNode -> {
                                                                val roleId = element.id

                                                                span(classes = "discord-mention") {
                                                                    val mentionedRole = savedMessage.mentions.roles.firstOrNull { it.id == roleId }
                                                                    val mentionedRoleName = mentionedRole?.name
                                                                    val mentionedRoleColor = mentionedRole?.colorRaw
                                                                    if (mentionedRoleColor != null) {
                                                                        val color = Color(mentionedRoleColor)
                                                                        style = "color: rgb(${color.red}, ${color.green}, ${color.blue});"
                                                                    }

                                                                    text("@$mentionedRoleName ($roleId)")
                                                                }
                                                            }

                                                            is DiscordChannelMentionEntityNode -> {
                                                                // We don't store all the guild's channels, nor do we store a list of the mentioned channels
                                                                // (because technically Discord does NOT provide that info to us)
                                                                val channelId = element.id

                                                                span(classes = "discord-mention discord-channel") {
                                                                    unsafe {
                                                                        raw(getSVGForChannelType(ChannelType.UNKNOWN))
                                                                    }
                                                                    text("??? ($channelId)")
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
                                            }

                                            div(classes = "discord-message-text") {
                                                div(classes = "discord-message-text-content") {
                                                    if (savedMessage.content.isNotEmpty()) {
                                                        val contentAsDocument =
                                                            parseMarkdownToNodes(savedMessage.content)
                                                        traverseNodesAndRender(contentAsDocument)

                                                        if (savedMessage.isEdited) {
                                                            text(" ")
                                                            span {
                                                                style = "color: #90969f; font-size: 0.5em;"
                                                                text("(editado)")
                                                            }
                                                        }
                                                    } else {
                                                        text(" ")
                                                        if (savedMessage.isEdited) {
                                                            span {
                                                                style = "color: #90969f; font-size: 0.5em;"
                                                                text("(editado)")
                                                            }
                                                        }
                                                    }
                                                }

                                                div(classes = "discord-message-accessories") {
                                                    for (embed in savedMessage.embeds) {
                                                        // There are multiple embeds type on Discord, so we need to handle it differently
                                                        when (embed.type) {
                                                            EmbedType.RICH, EmbedType.UNKNOWN, EmbedType.LINK -> {
                                                                // RICH is the good old embeds sent by bots
                                                                // LINK is links like https://google.com/ - anything that has a THUMBNAIL
                                                                // UNKNOWN is also used for links like this one: https://mrpowergamerbr.com/en/blog/2024-06-21-downgrading-paper-1-21-to-1-20-6
                                                                // The only visible difference between UNKNOWN/LINK and RICH, is that thumbnails are actually images
                                                                article(classes = "discord-embed") {
                                                                    val embedColorRaw = embed.color
                                                                    if (embedColorRaw != null) {
                                                                        val embedColor = Color(embedColorRaw)
                                                                        style = "border-color: rgb(${embedColor.red}, ${embedColor.green}, ${embedColor.blue});"
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
                                                                                        parseMarkdownToNodes(
                                                                                            authorName
                                                                                        )

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
                                                                                        parseMarkdownToNodes(
                                                                                            embedTitle
                                                                                        )
                                                                                    )
                                                                                }
                                                                            } else {
                                                                                div(classes = "discord-embed-title") {
                                                                                    traverseNodesAndRender(
                                                                                        parseMarkdownToNodes(
                                                                                            embedTitle
                                                                                        )
                                                                                    )
                                                                                }
                                                                            }
                                                                        }

                                                                        // ===[ EMBED DESCRIPTION ]===
                                                                        val embedDescription = embed.description
                                                                        if (embedDescription != null) {
                                                                            div(classes = "discord-embed-description") {
                                                                                traverseNodesAndRender(
                                                                                    parseMarkdownToNodes(
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
                                                                                    mutableListOf<MutableList<SavedEmbed.SavedField>>()

                                                                                for (field in embed.fields) {
                                                                                    val currentChunk =
                                                                                        chunks.lastOrNull() ?: run {
                                                                                            val newList =
                                                                                                mutableListOf<SavedEmbed.SavedField>()
                                                                                            chunks.add(newList)
                                                                                            newList
                                                                                        }

                                                                                    if (currentChunk.firstOrNull()?.isInline != field.isInline) {
                                                                                        // New chunk needs to be created!
                                                                                        val newList = mutableListOf<SavedEmbed.SavedField>()
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
                                                                                                        parseMarkdownToNodes(
                                                                                                            field.name
                                                                                                                ?: ""
                                                                                                        )
                                                                                                    )
                                                                                                }

                                                                                                div(classes = "discord-embed-field-value") {
                                                                                                    traverseNodesAndRender(
                                                                                                        parseMarkdownToNodes(
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
                                                                            val embedImage =
                                                                                embed.thumbnail?.proxyUrl
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
                                                                                val footerIconUrl =
                                                                                    footer.proxyIconUrl
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
                                                                                            parseMarkdownToNodes(
                                                                                                footerText
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                    if (embed.type != EmbedType.UNKNOWN) {
                                                                        val embedThumbnail = embed.thumbnail?.proxyUrl
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

                                                    div(classes = "discord-message-attachments") {
                                                        // These are the image embeds that show up if the message is only a single link
                                                        for (embed in savedMessage.embeds.filter { it.type == EmbedType.IMAGE }) {
                                                            val embedThumbnailUrl = embed.thumbnail?.proxyUrl

                                                            if (embedThumbnailUrl != null) {
                                                                img(src = embedThumbnailUrl, classes = "discord-message-attachment-preview") {}
                                                            }
                                                        }

                                                        for (attachment in savedMessage.attachments) {
                                                            if (attachment.contentType in imageContentTypes) {
                                                                img(src = attachment.proxyUrl, classes = "discord-message-attachment-preview") {}
                                                            } else {
                                                                div(classes = "discord-message-attachment") {
                                                                    div {
                                                                        unsafe {
                                                                            raw("""<svg fill="none" height="96" viewBox="0 0 72 96" width="72" xmlns="http://www.w3.org/2000/svg"><path d="m72 29.3v60.3c0 2.24 0 3.36-.44 4.22-.38.74-1 1.36-1.74 1.74-.86.44-1.98.44-4.22.44h-59.2c-2.24 0-3.36 0-4.22-.44-.74-.38-1.36-1-1.74-1.74-.44-.86-.44-1.98-.44-4.22v-83.2c0-2.24 0-3.36.44-4.22.38-.74 1-1.36 1.74-1.74.86-.44 1.98-.44 4.22-.44h36.3c1.96 0 2.94 0 3.86.22.5.12.98.28 1.44.5v16.88c0 2.24 0 3.36.44 4.22.38.74 1 1.36 1.74 1.74.86.44 1.98.44 4.22.44h16.88c.22.46.38.94.5 1.44.22.92.22 1.9.22 3.86z" fill="#d3d6fd"/><path d="m68.26 20.26c1.38 1.38 2.06 2.06 2.56 2.88.18.28.32.56.46.86h-16.88c-2.24 0-3.36 0-4.22-.44-.74-.38-1.36-1-1.74-1.74-.44-.86-.44-1.98-.44-4.22v-16.880029c.3.14.58.28.86.459999.82.5 1.5 1.18 2.88 2.56z" fill="#939bf9"/><path clip-rule="evenodd" d="m24 24c4.42 0 8-3.58 8-8 0-.72-.1-1.42-.28-2.08l-3.72-13.92h-8l-3.72 13.92c-.18.66-.28 1.36-.28 2.08 0 4.42 3.58 8 8 8zm0-4c2.2091 0 4-1.7909 4-4s-1.7909-4-4-4-4 1.7909-4 4 1.7909 4 4 4zm0 20v-8h-8v8zm0 8h8v-8h-8zm0 8v-8h-8v8zm0 8h8v-8h-8zm0 8v-8h-8v8zm0 8h8v-8h-8zm0 8h-8v-8h8zm0 0h8v8h-8z" fill="#5865f2" fill-rule="evenodd"/></svg>""")
                                                                        }
                                                                    }

                                                                    div(classes = "discord-message-attachment-info") {
                                                                        a(href = "#") {
                                                                            text(attachment.fileName)
                                                                        }
                                                                        div(classes = "discord-message-attachment-size") {
                                                                            text("${attachment.size} bytes")
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    for (sticker in savedMessage.stickers) {
                                                        div {
                                                            if (sticker.formatType == Sticker.StickerFormat.LOTTIE) {
                                                                // TODO: Lottie stickers fallback
                                                                text(sticker.name)
                                                            } else {
                                                                img(src = sticker.iconUrl) {
                                                                    style = "object-fit: contain;"
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
                                        style = "display: grid;grid-template-columns: 200px 1fr;"

                                        div {
                                            style =
                                                "font-size: 0.8em; display: flex; flex-direction: column; gap: 0.5em;"

                                            div {
                                                div {
                                                    style = "font-weight: bold;"
                                                    text("ID do usuário")
                                                }

                                                div {
                                                    // Makes the IDs look better when rendered in Linux
                                                    style = "letter-spacing: -0.5px;"

                                                    text("${savedMessage.author.id}")
                                                }
                                            }

                                            if (placeContext is SavedGuild) {
                                                div {
                                                    div {
                                                        style = "font-weight: bold;"
                                                        text("ID do servidor")
                                                    }

                                                    div {
                                                        // Makes the IDs look better when rendered in Linux
                                                        style = "letter-spacing: -0.5px;"

                                                        text("${placeContext.id}")
                                                    }
                                                }
                                            }

                                            div {
                                                div {
                                                    style = "font-weight: bold;"
                                                    text("ID do canal")
                                                }

                                                div {
                                                    // Makes the IDs look better when rendered in Linux
                                                    style = "letter-spacing: -0.5px;"

                                                    when (placeContext) {
                                                        // Guild messages are sent in a channelId
                                                        is SavedAttachedGuild -> {
                                                            text("${placeContext.channelId}")
                                                        }

                                                        is SavedDetachedGuild -> {
                                                            text("${placeContext.channelId}")
                                                        }
                                                        // While everything else is sent on the place context ID itself
                                                        is SavedGroupChannel -> {
                                                            text("${placeContext.id}")
                                                        }

                                                        is SavedPrivateChannel -> {
                                                            text("${placeContext.id}")
                                                        }
                                                    }
                                                }
                                            }

                                            div {
                                                div {
                                                    style = "font-weight: bold;"
                                                    text("ID da mensagem")
                                                }

                                                div {
                                                    // Makes the IDs look better when rendered in Linux
                                                    style = "letter-spacing: -0.5px;"
                                                    text("${savedMessage.id}")
                                                }
                                            }
                                        }

                                        div {
                                            style = "display: flex;\n" +
                                                    "    flex-direction: column;\n" +
                                                    "    align-items: flex-end;\n" +
                                                    "    height: 100%;\n" +
                                                    "    justify-content: flex-end;"

                                            if (qrCodeImageAsByteArray != null) {
                                                div {
                                                    img(
                                                        src = "data:image/png;base64,${Base64.getEncoder().encodeToString(qrCodeImageAsByteArray)}"
                                                    ) {
                                                        width = "64"
                                                        height = "64"
                                                        style = "border-radius: 2px;"
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
                                                            text("/verificarmensagem url LinkDestaImagem")
                                                        }
                                                    }
                                                }
                                                div {
                                                    style = "display: flex; flex-direction: column; align-items: center;"
                                                    div {
                                                        img(src = "https://cdn.discordapp.com/emojis/1167125529024012389.png?size=160&quality=lossless") {
                                                            style = "height: 3em;"
                                                        }
                                                    }
                                                    div {
                                                        style = "font-size: 0.5em;"
                                                        text("loritta.website")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                )
                // val screenshot = page.screenshot(Page.ScreenshotOptions().setFullPage(true))

                // Not really needed but... Wait all fonts to be ready
                // This still works even with JS disabled
                page.waitForFunction("document.fonts.ready")

                // Wait all images to be loaded
                page.waitForFunction("""
                        const images = Array.from(document.querySelectorAll('img'));
                        images.every(img => img.complete);
                """.trimIndent())

                // Parse unicode emojis to Twemoji, this is a bit hacky but it does work
                val script = DiscordMessageRendererManager::class.java.getResourceAsStream("/message-renderer-assets/twemoji.min.js").readAllBytes().toString(Charsets.UTF_8)
                page.evaluate(script)
                page.evaluate("twemoji.parse(document.body, {className: 'unicode-inline-emoji'});")

                page.querySelector("#wrapper").screenshot(ElementHandle.ScreenshotOptions())
            }

            logger.info { "Took ${timedValue.duration} to generate a message screenshot for ${savedMessage.id}!" }
            timedValue.value
        }

        return screenshot
    }

    private fun getSVGForChannelType(type: ChannelType): String {
        return when (type) {
            // Can't be mentioned, but doles have a special icon in chat
            ChannelType.VOICE -> """<svg aria-hidden="false" role="img" xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24"><path fill="currentColor" d="M12 3a1 1 0 0 0-1-1h-.06a1 1 0 0 0-.74.32L5.92 7H3a1 1 0 0 0-1 1v8a1 1 0 0 0 1 1h2.92l4.28 4.68a1 1 0 0 0 .74.32H11a1 1 0 0 0 1-1V3ZM15.1 20.75c-.58.14-1.1-.33-1.1-.92v-.03c0-.5.37-.92.85-1.05a7 7 0 0 0 0-13.5A1.11 1.11 0 0 1 14 4.2v-.03c0-.6.52-1.06 1.1-.92a9 9 0 0 1 0 17.5Z" class=""></path><path fill="currentColor" d="M15.16 16.51c-.57.28-1.16-.2-1.16-.83v-.14c0-.43.28-.8.63-1.02a3 3 0 0 0 0-5.04c-.35-.23-.63-.6-.63-1.02v-.14c0-.63.59-1.1 1.16-.83a5 5 0 0 1 0 9.02Z" class=""></path></svg>"""
            // Icon does not show up when mentioning in chat
            ChannelType.NEWS -> """<svg role="img" xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24"><path fill="currentColor" fill-rule="evenodd" d="M19.56 2a3 3 0 0 0-2.46 1.28 3.85 3.85 0 0 1-1.86 1.42l-8.9 3.18a.5.5 0 0 0-.34.47v10.09a3 3 0 0 0 2.27 2.9l.62.16c1.57.4 3.15-.56 3.55-2.12a.92.92 0 0 1 1.23-.63l2.36.94c.42.27.79.62 1.07 1.03A3 3 0 0 0 19.56 22h.94c.83 0 1.5-.67 1.5-1.5v-17c0-.83-.67-1.5-1.5-1.5h-.94Zm-8.53 15.8L8 16.7v1.73a1 1 0 0 0 .76.97l.62.15c.5.13 1-.17 1.12-.67.1-.41.29-.78.53-1.1Z" clip-rule="evenodd" class=""></path><path fill="currentColor" d="M2 10c0-1.1.9-2 2-2h.5c.28 0 .5.22.5.5v7a.5.5 0 0 1-.5.5H4a2 2 0 0 1-2-2v-4Z" class=""></path></svg>"""
            // Can't be mentioned, but does have a special icon in chat
            ChannelType.STAGE -> """<svg aria-hidden="false" role="img" xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="none" viewBox="0 0 24 24"><path fill="currentColor" d="M19.61 18.25a1.08 1.08 0 0 1-.07-1.33 9 9 0 1 0-15.07 0c.26.42.25.97-.08 1.33l-.02.02c-.41.44-1.12.43-1.46-.07a11 11 0 1 1 18.17 0c-.33.5-1.04.51-1.45.07l-.02-.02Z" class=""></path><path fill="currentColor" d="M16.83 15.23c.43.47 1.18.42 1.45-.14a7 7 0 1 0-12.57 0c.28.56 1.03.6 1.46.14l.05-.06c.3-.33.35-.81.17-1.23A4.98 4.98 0 0 1 12 7a5 5 0 0 1 4.6 6.94c-.17.42-.13.9.18 1.23l.05.06Z" class=""></path><path fill="currentColor" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0ZM6.33 20.03c-.25.72.12 1.5.8 1.84a10.96 10.96 0 0 0 9.73 0 1.52 1.52 0 0 0 .8-1.84 6 6 0 0 0-11.33 0Z" class=""></path></svg>"""
            ChannelType.GUILD_NEWS_THREAD, ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_PRIVATE_THREAD -> """<svg aria-hidden="false" role="img" xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24"><path d="M12 2.81a1 1 0 0 1 0-1.41l.36-.36a1 1 0 0 1 1.41 0l9.2 9.2a1 1 0 0 1 0 1.4l-.7.7a1 1 0 0 1-1.3.13l-9.54-6.72a1 1 0 0 1-.08-1.58l1-1L12 2.8ZM12 21.2a1 1 0 0 1 0 1.41l-.35.35a1 1 0 0 1-1.41 0l-9.2-9.19a1 1 0 0 1 0-1.41l.7-.7a1 1 0 0 1 1.3-.12l9.54 6.72a1 1 0 0 1 .07 1.58l-1 1 .35.36ZM15.66 16.8a1 1 0 0 1-1.38.28l-8.49-5.66A1 1 0 1 1 6.9 9.76l8.49 5.65a1 1 0 0 1 .27 1.39ZM17.1 14.25a1 1 0 1 0 1.11-1.66L9.73 6.93a1 1 0 0 0-1.11 1.66l8.49 5.66Z" fill="currentColor" class=""></path></svg>"""
            // The fallback icon is the text channel hashtag # icon
            else -> """<svg aria-hidden="false" role="img" xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24"><path fill="currentColor" fill-rule="evenodd" d="M10.99 3.16A1 1 0 1 0 9 2.84L8.15 8H4a1 1 0 0 0 0 2h3.82l-.67 4H3a1 1 0 1 0 0 2h3.82l-.8 4.84a1 1 0 0 0 1.97.32L8.85 16h4.97l-.8 4.84a1 1 0 0 0 1.97.32l.86-5.16H20a1 1 0 1 0 0-2h-3.82l.67-4H21a1 1 0 1 0 0-2h-3.82l.8-4.84a1 1 0 1 0-1.97-.32L15.15 8h-4.97l.8-4.84ZM14.15 14l.67-4H9.85l-.67 4h4.97Z" clip-rule="evenodd" class=""></path></svg>"""
        }
    }

    override fun close() {
        playwright.close()
    }
}