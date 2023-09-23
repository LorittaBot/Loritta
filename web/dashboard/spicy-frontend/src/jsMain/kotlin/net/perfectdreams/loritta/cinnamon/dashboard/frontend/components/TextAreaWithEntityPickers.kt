package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrappers
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.DiscordUtils
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.DiscordCdn
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.Image
import net.perfectdreams.loritta.common.utils.Color
import net.perfectdreams.loritta.serializable.DiscordChannel
import net.perfectdreams.loritta.serializable.DiscordEmoji
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.DiscordRole
import org.jetbrains.compose.web.attributes.autoFocus
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.jetbrains.compose.web.dom.TextInput
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TextAreaWithEntityPickers(guild: DiscordGuild, content: String, onChange: (content: String) -> (Unit)) {
    var textAreaWrapper by remember { mutableStateOf<TextAreaWithEntityPickers?>(null) }

    SideEffect {
        // Update content when recomposed, this is required when the user clicks the "Reset" config option
        textAreaWrapper?.textArea?.value = content
    }

    TextArea {
        defaultValue(content)

        ref {
            textAreaWrapper = TextAreaWithEntityPickers(it, guild, onChange)
            onDispose {
                textAreaWrapper?.unmount()
                textAreaWrapper = null
            }
        }
    }

    val hackyTextAreaX = textAreaWrapper
    if (hackyTextAreaX != null) {
        val cursorXY = hackyTextAreaX.cursorXY
        val typeaheadMatches = textAreaWrapper?.matches
        if (cursorXY != null && typeaheadMatches != null) {
            Div(attrs = {
                attr("style", "position: absolute; left: ${cursorXY.x}px; top: ${cursorXY.y}px;")
                classes("message-config-tooltip", "reset-theme-variables")
            }) {
                when (typeaheadMatches) {
                    is TextAreaWithEntityPickers.TypeaheadMatches.DiscordChannelMatches -> {
                        typeaheadMatches.channels.forEachIndexed { index, channel ->
                            Div(attrs = {
                                classes("message-config-tooltip-entry")
                                if (index == typeaheadMatches.index.value)
                                    classes("selected")

                                onClick {
                                    hackyTextAreaX.selectChannel(typeaheadMatches, channel)
                                }
                            }) {
                                TextWithIconWrapper(DiscordUtils.getIconForChannel(channel), svgAttrs = {
                                    attr("style", "height: 1em; width: 1em;")
                                }) {
                                    Text(channel.name)
                                }
                            }
                        }
                    }
                    is TextAreaWithEntityPickers.TypeaheadMatches.DiscordRoleMatches -> {
                        typeaheadMatches.roles.forEachIndexed { index, role ->
                            Div(attrs = {
                                classes("message-config-tooltip-entry")
                                if (index == typeaheadMatches.index.value)
                                    classes("selected")

                                onClick {
                                    hackyTextAreaX.selectRole(typeaheadMatches, role)
                                }
                            }) {
                                TextWithIconWrapper(SVGIconManager.roleShield, svgAttrs = {
                                    val color = if (role.color != 0x1FFFFFFF) Color(role.color) else null

                                    style {
                                        width(1.em)
                                        height(1.em)
                                        if (color != null)
                                            color(rgb(color.red, color.green, color.blue))
                                    }
                                }) {
                                    Text(role.name)
                                }
                            }
                        }
                    }
                    is TextAreaWithEntityPickers.TypeaheadMatches.DiscordEmojiMatches -> {
                        typeaheadMatches.emojis.forEachIndexed { index, emoji ->
                            Div(attrs = {
                                classes("message-config-tooltip-entry", "emoji-entry")
                                if (index == typeaheadMatches.index.value)
                                    classes("selected")

                                onClick {
                                    hackyTextAreaX.selectEmoji(typeaheadMatches, emoji)
                                }
                            }) {
                                Img(DiscordCdn.emoji(emoji.id.toULong()).toUrl {
                                    format = if (emoji.animated) Image.Format.GIF else Image.Format.PNG
                                }) {
                                    attr("width", "24")
                                    attr("height", "24")
                                }

                                Text(":")
                                Text(emoji.name)
                                Text(":")
                            }
                        }
                    }
                }
            }
        }
    }

    // Debug coordinates
    /* Div {
        val cursorXY = textAreaWrapper?.cursorXY

        if (cursorXY != null) {
            Text("${cursorXY.x}, ${cursorXY.y}")
        } else {
            Text("null")
        }
    } */

    Div(attrs = {
        attr("style", "display: flex; justify-content: flex-end; gap: 0.5em;")
    }) {
        Div(attrs = {
            attr("style", "position: relative; display: flex;")
        }) {
            var isMenuOpen by remember { mutableStateOf(false) }
            var channelFilter by remember { mutableStateOf("") }

            DiscordButton(
                DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
                attrs = {
                    onClick {
                        isMenuOpen = !isMenuOpen
                    }
                }
            ) {
                TextWithIconWrapper(SVGIconManager.discordTextChannel, {
                    attr("style", "height: 1em;")
                }) {
                    Text("Canais")
                }
            }

            if (isMenuOpen) {
                var onClickCallback by remember {
                    mutableStateOf<EventListener?>(null)
                }

                Div(attrs = {
                    classes("message-config-popover", "reset-theme-variables")

                    ref {
                        onClickCallback = object: EventListener {
                            override fun handleEvent(event: Event) {
                                if (it.contains(event.target as Node))
                                    return

                                isMenuOpen = false
                            }
                        }
                        window.addEventListener("click", onClickCallback)

                        onDispose {
                            window.removeEventListener("click", onClickCallback)
                            onClickCallback = null
                        }
                    }
                }) {
                    FieldWrappers(attrs = {
                        classes("message-config-popover-content")
                    }) {
                        FieldLabel("Adicionar menção de canal")

                        Div(attrs = {
                            classes("message-config-channel-list")
                        }) {
                            for (channel in guild.channels.filter { it.name.contains(channelFilter.removePrefix("#"), true) }) {
                                Div(attrs = {
                                    classes("message-config-channel-list-entry")
                                    onClick {
                                        val textArea = textAreaWrapper?.textArea ?: return@onClick

                                        val selectionStart = textArea.selectionStart ?: 0
                                        val selectionEnd = textArea.selectionEnd ?: 0

                                        textAreaWrapper!!.replaceText(selectionStart, selectionEnd, "<#${channel.id}>")
                                        onChange.invoke(textArea.value)

                                        // Only close the menu if the shift key is not being held
                                        if (!it.shiftKey)
                                            isMenuOpen = false
                                    }
                                }) {
                                    UIIcon(DiscordUtils.getIconForChannel(channel)) {
                                        attr("style", "height: 1em;")
                                    }

                                    Text(channel.name)
                                }
                            }
                        }
                    }

                    Div(attrs = {
                        classes("message-config-popover-filter-input-wrapper")
                    }) {
                        TextInput(channelFilter) {
                            autoFocus()
                            placeholder("Filtrar canais")

                            onInput {
                                channelFilter = it.value
                            }
                        }
                    }
                }
            }
        }

        Div(attrs = {
            attr("style", "position: relative; display: flex;")
        }) {
            var isMenuOpen by remember { mutableStateOf(false) }
            var roleFilter by remember { mutableStateOf("") }

            DiscordButton(
                DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
                attrs = {
                    onClick {
                        isMenuOpen = !isMenuOpen
                    }
                }
            ) {
                TextWithIconWrapper(SVGIconManager.roleShield, {
                    attr("style", "height: 1em;")
                }) {
                    Text("Cargos")
                }
            }

            if (isMenuOpen) {
                var onClickCallback by remember {
                    mutableStateOf<EventListener?>(null)
                }

                Div(attrs = {
                    classes("message-config-popover", "reset-theme-variables")

                    ref {
                        onClickCallback = object: EventListener {
                            override fun handleEvent(event: Event) {
                                if (it.contains(event.target as Node))
                                    return

                                isMenuOpen = false
                            }
                        }
                        window.addEventListener("click", onClickCallback)

                        onDispose {
                            window.removeEventListener("click", onClickCallback)
                            onClickCallback = null
                        }
                    }
                }) {
                    FieldWrappers(attrs = {
                        classes("message-config-popover-content")
                    }) {
                        FieldLabel("Adicionar menção de cargo")

                        Div(attrs = {
                            classes("message-config-role-list")
                        }) {
                            for (role in guild.roles.filter { it.name.contains(roleFilter.removePrefix("@"), true) }) {
                                Div(attrs = {
                                    classes("message-config-role-list-entry")
                                    onClick {
                                        val textArea = textAreaWrapper?.textArea ?: return@onClick

                                        val selectionStart = textArea.selectionStart ?: 0
                                        val selectionEnd = textArea.selectionEnd ?: 0

                                        textAreaWrapper!!.replaceText(selectionStart, selectionEnd, "<@&${role.id}>")
                                        onChange.invoke(textArea.value)

                                        // Only close the menu if the shift key is not being held
                                        if (!it.shiftKey)
                                            isMenuOpen = false
                                    }
                                }) {
                                    UIIcon(SVGIconManager.roleShield) {
                                        val color = if (role.color != 0x1FFFFFFF) Color(role.color) else null

                                        style {
                                            width(1.em)
                                            height(1.em)
                                            if (color != null)
                                                color(rgb(color.red, color.green, color.blue))
                                        }
                                    }

                                    Text(role.name)
                                }
                            }
                        }
                    }

                    Div(attrs = {
                        classes("message-config-popover-filter-input-wrapper")
                    }) {
                        TextInput(roleFilter) {
                            autoFocus()
                            placeholder("Filtrar cargos")

                            onInput {
                                roleFilter = it.value
                            }
                        }
                    }
                }
            }
        }

        Div(attrs = {
            attr("style", "display: flex; justify-content: flex-end;")
        }) {
            Div(attrs = {
                attr("style", "position: relative")
            }) {
                var isMenuOpen by remember { mutableStateOf(false) }
                var emojiFilter by remember { mutableStateOf("") }

                DiscordButton(
                    DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
                    attrs = {
                        onClick {
                            isMenuOpen = !isMenuOpen
                        }
                    }
                ) {
                    TextWithIconWrapper(SVGIconManager.faceSmile, {
                        attr("style", "height: 1em;")
                    }) {
                        Text("Emojis")
                    }
                }

                if (isMenuOpen) {
                    var onClickCallback by remember {
                        mutableStateOf<EventListener?>(null)
                    }

                    Div(attrs = {
                        classes("message-config-popover", "reset-theme-variables")

                        ref {
                            onClickCallback = object: EventListener {
                                override fun handleEvent(event: Event) {
                                    if (it.contains(event.target as Node))
                                        return

                                    isMenuOpen = false
                                }
                            }
                            window.addEventListener("click", onClickCallback)

                            onDispose {
                                window.removeEventListener("click", onClickCallback)
                                onClickCallback = null
                            }
                        }
                    }) {
                        FieldWrappers(attrs = {
                            classes("message-config-popover-content")
                        }) {
                            FieldLabel("Adicionar emoji")

                            Div(attrs = {
                                classes("message-config-emoji-grid")
                            }) {
                                for (emoji in guild.emojis.filter {
                                    it.name.contains(
                                        emojiFilter.removePrefix(":").removeSuffix(":"),
                                        true
                                    )
                                }.sortedBy { it.name }) {
                                    Div(attrs = {
                                        attr("style", "cursor: pointer;")
                                        onClick {
                                            val textArea = textAreaWrapper?.textArea ?: return@onClick

                                            val selectionStart = textArea.selectionStart ?: 0
                                            val selectionEnd = textArea.selectionEnd ?: 0

                                            textAreaWrapper!!.replaceText(selectionStart, selectionEnd, buildString {
                                                append("<")
                                                if (emoji.animated)
                                                    append("a")
                                                append(":")
                                                append(emoji.name)
                                                append(":")
                                                append(emoji.id)
                                                append(">")
                                            })
                                            onChange.invoke(textArea.value)

                                            // Only close the menu if the shift key is not being held
                                            if (!it.shiftKey)
                                                isMenuOpen = false
                                        }
                                    }) {
                                        Img(DiscordCdn.emoji(emoji.id.toULong()).toUrl {
                                            format = if (emoji.animated) Image.Format.GIF else Image.Format.PNG
                                        })
                                    }
                                }
                            }
                        }

                        Div(attrs = {
                            classes("message-config-popover-filter-input-wrapper")
                        }) {
                            TextInput(emojiFilter) {
                                autoFocus()
                                placeholder("Filtrar emojis")

                                onInput {
                                    emojiFilter = it.value
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A text area with Discord entity pickers
 */
private class TextAreaWithEntityPickers(val textArea: HTMLTextAreaElement, val guild: DiscordGuild, val onChange: (String) -> (Unit)) {
    var cursorXY by mutableStateOf<CursorXY?>(null)
    private val updateCursorListener = object: EventListener {
        override fun handleEvent(event: Event) {
            updateCursor()
        }
    }
    var matches by mutableStateOf<TypeaheadMatches?>(null)
    private val selectEntryListener = object: EventListener {
        override fun handleEvent(event: Event) {
            event as KeyboardEvent

            val match = matches

            if (match != null) {
                // ENTER or TAB
                // TODO: Handling space at the end would be pretty pog
                if (event.keyCode == 13 || event.keyCode == 9) {
                    when (match) {
                        is TypeaheadMatches.DiscordChannelMatches -> {
                            val channel = match.channels[match.index.value]

                            selectChannel(match, channel)
                        }
                        is TypeaheadMatches.DiscordEmojiMatches -> {
                            val channel = match.emojis[match.index.value]

                            selectEmoji(match, channel)
                        }

                        is TypeaheadMatches.DiscordRoleMatches -> {
                            val role = match.roles[match.index.value]

                            selectRole(match, role)
                        }
                    }

                    // Cancel the event
                    event.preventDefault()
                }

                // Arrow Up
                if (event.keyCode == 38) {
                    when (match) {
                        is TypeaheadMatches.DiscordChannelMatches -> match.index.value = (match.index.value - 1).coerceAtLeast(0)
                        is TypeaheadMatches.DiscordEmojiMatches -> match.index.value = (match.index.value - 1).coerceAtLeast(0)
                        is TypeaheadMatches.DiscordRoleMatches -> match.index.value = (match.index.value - 1).coerceAtLeast(0)
                    }

                    // Cancel the event
                    event.preventDefault()
                }

                // Arrow Down
                if (event.keyCode == 40) {
                    when (match) {
                        is TypeaheadMatches.DiscordChannelMatches ->  match.index.value = (match.index.value + 1).coerceAtMost(match.channels.size - 1)
                        is TypeaheadMatches.DiscordEmojiMatches ->  match.index.value = (match.index.value + 1).coerceAtMost(match.emojis.size - 1)
                        is TypeaheadMatches.DiscordRoleMatches -> match.index.value = (match.index.value + 1).coerceAtMost(match.roles.size - 1)
                    }

                    // Cancel the event
                    event.preventDefault()
                }
            }
        }
    }
    private val getSelectionsListener = object: EventListener {
        override fun handleEvent(event: Event) {
            // Reset current match
            matches = null

            val selStart = textArea.selectionStart
            val selEnd = textArea.selectionEnd

            // println("selStart: $selStart")
            // println("selEnd: $selEnd")

            // They MUST be equal, if not the user is selecting a range and we don't want to handle that
            if (selStart != selEnd || selEnd == null) {
                return
            }

            // Get text BEFORE the selection end
            val everythingBeforeSelEnd = textArea.value.take(selEnd)

            // Now we try to check if we are querying something!
            var controlIndexFromEnd: Int? = null
            var type: TypeaheadType? = null

            for ((index, char) in everythingBeforeSelEnd.reversed().withIndex()) {
                if (char == '#') {
                    // Discord Channel
                    controlIndexFromEnd = index
                    type = TypeaheadType.CHANNEL
                    break
                }

                if (char == ':') {
                    // Discord Emoji
                    controlIndexFromEnd = index
                    type = TypeaheadType.EMOJI
                    break
                }

                if (char == '@') {
                    // Discord Emoji
                    controlIndexFromEnd = index
                    type = TypeaheadType.ROLE
                    break
                }

                // Can't be a valid selection
                if (char == ' ' && (type != null || type == TypeaheadType.CHANNEL || type == TypeaheadType.EMOJI))
                    break
            }

            if (controlIndexFromEnd != null) {
                val controlIndexFromBeginning = everythingBeforeSelEnd.length - controlIndexFromEnd
                val query = everythingBeforeSelEnd.drop(controlIndexFromBeginning)

                if (type == TypeaheadType.CHANNEL) {
                    val channels = guild.channels.filter { it.name.startsWith(query, true) }
                        .take(10)

                    if (channels.isEmpty()) {
                        matches = null
                        return
                    }

                    // The -1 is because we ignore the #
                    matches = TypeaheadMatches.DiscordChannelMatches(
                        controlIndexFromBeginning - 1,
                        controlIndexFromBeginning + query.length,
                        mutableStateOf(0),
                        channels
                    )
                } else if (type == TypeaheadType.EMOJI) {
                    val emojis = guild.emojis.filter { it.name.startsWith(query, true) }
                        .take(10)

                    if (emojis.isEmpty()) {
                        matches = null
                        return
                    }

                    // The -1 is because we ignore the #
                    matches = TypeaheadMatches.DiscordEmojiMatches(
                        controlIndexFromBeginning - 1,
                        controlIndexFromBeginning + query.length,
                        mutableStateOf(0),
                        emojis
                    )
                } else if (type == TypeaheadType.ROLE) {
                    val roles = guild.roles.filter { it.name.startsWith(query, true) }
                        .take(10)

                    if (roles.isEmpty()) {
                        matches = null
                        return
                    }

                    // The -1 is because we ignore the #
                    matches = TypeaheadMatches.DiscordRoleMatches(
                        controlIndexFromBeginning - 1,
                        controlIndexFromBeginning + query.length,
                        mutableStateOf(0),
                        roles
                    )
                }
            }
        }
    }
    private val inputListener = object: EventListener {
        override fun handleEvent(event: Event) {
            onChange.invoke(textArea.value)
        }
    }
    private val blurListener = object: EventListener {
        override fun handleEvent(event: Event) {
            GlobalScope.launch {
                // This is a HACK! We only remove the matches after 100ms because clicking the tooltip would remove the matches
                // So we wait a bit before actually removing them
                delay(100.milliseconds)
                matches = null
            }
        }
    }

    init {
        textArea.addEventListener("click", updateCursorListener)
        textArea.addEventListener("selectionchange", updateCursorListener)
        textArea.addEventListener("input", updateCursorListener)

        textArea.addEventListener("input", getSelectionsListener)
        textArea.addEventListener("selectionchange", getSelectionsListener)
        textArea.addEventListener("focus", getSelectionsListener)

        textArea.addEventListener("input", inputListener)

        textArea.addEventListener("keydown", selectEntryListener)

        textArea.addEventListener("blur", blurListener)
    }

    fun unmount() {
        textArea.removeEventListener("click", updateCursorListener)
        textArea.removeEventListener("selectionchange", updateCursorListener)
        textArea.removeEventListener("input", updateCursorListener)

        textArea.removeEventListener("input", getSelectionsListener)
        textArea.removeEventListener("selectionchange", getSelectionsListener)
        textArea.removeEventListener("focus", getSelectionsListener)

        textArea.removeEventListener("input", inputListener)

        textArea.removeEventListener("keydown", selectEntryListener)

        textArea.removeEventListener("blur", blurListener)
    }

    fun updateCursor() {
        cursorXY = getCursorXY(textArea, textArea.selectionEnd ?: 0)
    }

    fun selectChannel(match: TypeaheadMatches.DiscordChannelMatches, channel: DiscordChannel) {
        // Append to the end of the selection
        // And append a space at the end too, like how Discord does
        replaceText(match.matchStart, match.matchEnd, "<#${channel.id}> ")

        // Set current selection to null
        this@TextAreaWithEntityPickers.matches = null

        // Update the cursor position
        updateCursor()

        // Invoke the callback
        onChange.invoke(textArea.value)
    }

    fun selectEmoji(match: TypeaheadMatches.DiscordEmojiMatches, emoji: DiscordEmoji) {
        // Append to the end of the selection
        // And append a space at the end too, like how Discord does
        replaceText(
            match.matchStart,
            match.matchEnd,
            buildString {
                append("<")
                if (emoji.animated)
                    append("a")
                append(":")
                append(emoji.name)
                append(":")
                append(emoji.id)
                append(">")
                append(" ")
            }
        )

        // Set current selection to null
        this@TextAreaWithEntityPickers.matches = null

        // Update the cursor position
        updateCursor()

        // Invoke the callback
        onChange.invoke(textArea.value)
    }

    fun selectRole(match: TypeaheadMatches.DiscordRoleMatches, channel: DiscordRole) {
        // Append to the end of the selection
        // And append a space at the end too, like how Discord does
        replaceText(
            match.matchStart,
            match.matchEnd,
            buildString {
                append("<@&")
                append(channel.id)
                append(">")
                append(" ")
            }
        )

        // Set current selection to null
        this@TextAreaWithEntityPickers.matches = null

        // Update the cursor position
        updateCursor()

        // Invoke the callback
        onChange.invoke(textArea.value)
    }

    fun replaceText(start: Int, end: Int, newText: String) {
        val newDataBeforeTheAfterContent = textArea.value.take(start) + newText
        textArea.value = newDataBeforeTheAfterContent + textArea.value.drop(end)

        // Update the selection end, required if we have post-match data
        textArea.selectionEnd = newDataBeforeTheAfterContent.length
    }

    sealed class TypeaheadMatches {
        data class DiscordChannelMatches(
            val matchStart: Int,
            val matchEnd: Int,
            val index: MutableState<Int>,
            val channels: List<DiscordChannel>
        ) : TypeaheadMatches()

        data class DiscordEmojiMatches(
            val matchStart: Int,
            val matchEnd: Int,
            val index: MutableState<Int>,
            val emojis: List<DiscordEmoji>
        ) : TypeaheadMatches()

        data class DiscordRoleMatches(
            val matchStart: Int,
            val matchEnd: Int,
            val index: MutableState<Int>,
            val roles: List<DiscordRole>
        ) : TypeaheadMatches()
    }

    enum class TypeaheadType {
        CHANNEL,
        EMOJI,
        ROLE
    }
}

/**
 * returns x, y coordinates for absolute positioning of a span within a given text input
 * at a given selection point
 * @param {object} input - the input element to obtain coordinates for
 * @param {number} selectionPoint - the selection point for the input
 */
// https://jh3y.medium.com/how-to-where-s-the-caret-getting-the-xy-position-of-the-caret-a24ba372990a
fun getCursorXY(input: HTMLElement, selectionPoint: Int): CursorXY {
    val inputX = input.offsetLeft
    val inputY = input.offsetTop

    // create a dummy element that will be a clone of our input
    document.querySelector("#hacky")?.remove()
    val div = document.createElement("div") as HTMLElement
    div.id = "hacky"

    // get the computed style of the input and clone it onto the dummy element
    val copyStyle = window.getComputedStyle(input)

    for (prop in copyStyle.asList()) {
        div.style.setProperty(prop, copyStyle.getPropertyValue(prop))
    }

    div.style.setProperty("position", "absolute")
    div.style.setProperty("z-index", "10000")

    // we need a character that will replace whitespace when filling our dummy element if it's a single line <input/>
    val swap = "."
    val inputValue = when (input) {
        is HTMLInputElement -> {
            input.value.replace(" ", swap)
        }

        is HTMLTextAreaElement -> {
            input.value
        }

        else -> error("Unsupported element $input")
    }

    // set the div content to that of the textarea up until selection
    val textContent = inputValue.substring(0, selectionPoint)
    // set the text content of the dummy element div
    div.textContent = textContent

    if (input.tagName == "TEXTAREA") {
        div.style.height = "auto"
    }

    // if a single line input then the div needs to be single line and not break out like a text area
    if (input.tagName == "INPUT") {
        div.style.width = "auto"
    }

    // create a marker element to obtain caret position
    val span = document.createElement("span") as HTMLElement
    // give the span the textContent of remaining content so that the recreated dummy element is as close as possible
    span.textContent = inputValue.substring(selectionPoint).ifEmpty { "." }
    // append the span marker to the div
    div.appendChild(span)
    // append the dummy element to the body
    document.body?.appendChild(div)
    // get the marker position, this is the caret position top and left relative to the input
    val spanX = span.offsetLeft
    val spanY = span.offsetTop
    // lastly, remove that dummy element
    // NOTE:: can comment this out for debugging purposes if you want to see where that span is rendered
    // document.body?.removeChild(div)
    // return an object with the x and y of the caret. account for input positioning so that you don't need to wrap the input

    // Power Changes: Subtract the scroll offset to the cursor position, fixes issues when the textarea scrolls
    return CursorXY(inputX + spanX - input.scrollLeft.toInt(), inputY + spanY - input.scrollTop.toInt())
}

data class CursorXY(val x: Int, val y: Int)