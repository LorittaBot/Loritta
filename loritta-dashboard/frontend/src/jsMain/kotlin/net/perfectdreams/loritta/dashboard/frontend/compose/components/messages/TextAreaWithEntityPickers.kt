package net.perfectdreams.loritta.dashboard.frontend.compose.components.messages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import js.reflect.unsafeCast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.dashboard.discord.DiscordChannel
import net.perfectdreams.loritta.dashboard.discord.DiscordEmoji
import net.perfectdreams.loritta.dashboard.discord.DiscordGuild
import net.perfectdreams.loritta.dashboard.discord.DiscordRole
import net.perfectdreams.loritta.dashboard.frontend.compose.components.DiscordButton
import net.perfectdreams.loritta.dashboard.frontend.compose.components.DiscordButtonType
import net.perfectdreams.loritta.dashboard.frontend.compose.components.FieldLabel
import net.perfectdreams.loritta.dashboard.frontend.compose.components.FieldWrappers
import net.perfectdreams.loritta.dashboard.frontend.compose.components.TextWithIconWrapper
import org.jetbrains.compose.web.attributes.autoFocus
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.jetbrains.compose.web.dom.TextInput
import web.dom.Node
import web.events.CLICK
import web.events.Event
import web.events.EventHandler
import web.events.EventTarget
import web.events.SELECTION_CHANGE
import web.events.addEventHandler
import web.events.addEventListener
import web.events.removeEventListener
import web.focus.BLUR
import web.focus.FOCUS
import web.focus.FocusEvent
import web.html.HTMLElement
import web.html.HTMLTextAreaElement
import web.input.INPUT
import web.input.InputEvent
import web.keyboard.KEY_DOWN
import web.keyboard.KeyboardEvent
import web.mouse.MouseEvent
import web.pointer.CLICK
import web.pointer.PointerEvent
import web.window.window
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TextAreaWithEntityPickers(guild: DiscordGuild, content: String, onChange: (content: String) -> (Unit)) {
    // This needs to be remembered across compositions!
    val textAreaWrapper by remember {
        mutableStateOf(TextAreaWithEntityPickers(guild, onChange))
    }

    // Update the area in our uncontrolled input
    SideEffect {
        textAreaWrapper.updateData(guild, onChange)
    }

    TextArea(content) {
        ref {
            textAreaWrapper.mountIn(it)
            onDispose {
                textAreaWrapper.unmount()
            }
        }
    }

    val hackyTextAreaX = textAreaWrapper
    val cursorXY = hackyTextAreaX.cursorXY
    val typeaheadMatches = textAreaWrapper.matches
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
                            // TODO - htmx-mix: Fix this!
                            TextWithIconWrapper(/* DiscordUtils.getIconForChannel(channel), svgAttrs = {
                                attr("style", "height: 1em; width: 1em;")
                            }*/) {
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
                            // TODO - htmx-mix: Fix this!
                            TextWithIconWrapper(/* )SVGIconManager.roleShield, svgAttrs = {
                                val color = if (role.color != 0x1FFFFFFF) Color(role.color) else null

                                style {
                                    width(1.em)
                                    height(1.em)
                                    if (color != null)
                                        color(rgb(color.red, color.green, color.blue))
                                }
                            }*/) {
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
                            // TODO - htmx-mix: Refactor this!
                            var url = "https://cdn.discordapp.com/emojis/${emoji.id}"
                            url += if (emoji.animated) {
                                ".gif"
                            } else
                                ".png"
                            Img(url) {
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
        classes("message-popover-buttons")
    }) {
        DiscordChannelEntityPickerButton(textAreaWrapper, guild.channels, onChange)
        DiscordRoleEntityPickerButton(textAreaWrapper, guild.roles, onChange)
        DiscordEmojiEntityPickerButton(textAreaWrapper, guild.emojis, onChange)
    }
}

@Composable
private fun DiscordChannelEntityPickerButton(textAreaWrapper: TextAreaWithEntityPickers, channels: List<DiscordChannel>, onChange: (content: String) -> Unit) {
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
            // TODO - htmx-fix: Fix this!
            TextWithIconWrapper(/* SVGIconManager.discordTextChannel, {
                attr("style", "height: 1em;")
            } */) {
                Text("Canais")
            }
        }

        if (isMenuOpen) {
            Div(attrs = {
                classes("message-config-popover", "reset-theme-variables")

                ref {
                    val removeClickCallback = window.addEventHandler(Event.CLICK) { event ->
                        if (it.contains(event.target as Node))
                            return@addEventHandler

                        isMenuOpen = false
                    }

                    onDispose {
                        removeClickCallback()
                    }
                }
            }) {
                FieldWrappers(attrs = {
                    classes("message-config-popover-content")
                }) {
                    FieldLabel("Adicionar menção de canal")

                    Div(attrs = {
                        classes("message-config-list")
                    }) {
                        for (channel in channels.filter { it.name.contains(channelFilter.removePrefix("#"), true) }) {
                            Div(attrs = {
                                classes("message-config-list-entry")
                                onClick {
                                    val textArea = textAreaWrapper.textArea

                                    val selectionStart = textArea.selectionStart ?: 0
                                    val selectionEnd = textArea.selectionEnd ?: 0

                                    textAreaWrapper.replaceText(selectionStart, selectionEnd, "<#${channel.id}>")
                                    onChange.invoke(textArea.value)

                                    // Only close the menu if the shift key is not being held
                                    if (!it.shiftKey)
                                        isMenuOpen = false
                                }
                            }) {
                                // TODO - htmx-fix: Fix this!
                                /* UIIcon(DiscordUtils.getIconForChannel(channel)) {
                                    attr("style", "height: 1em;")
                                } */

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
}

@Composable
private fun DiscordRoleEntityPickerButton(textAreaWrapper: TextAreaWithEntityPickers, roles: List<DiscordRole>, onChange: (content: String) -> Unit) {
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
            // TODO - htmx-fix: Fix this!
            TextWithIconWrapper(/* SVGIconManager.roleShield, {
                attr("style", "height: 1em;")
            } */) {
                Text("Cargos")
            }
        }

        if (isMenuOpen) {
            Div(attrs = {
                classes("message-config-popover", "reset-theme-variables")

                ref {
                    val removeClickCallback = window.addEventHandler(Event.CLICK) { event ->
                        if (it.contains(event.target as Node))
                            return@addEventHandler

                        isMenuOpen = false
                    }

                    onDispose {
                        removeClickCallback()
                    }
                }
            }) {
                FieldWrappers(attrs = {
                    classes("message-config-popover-content")
                }) {
                    FieldLabel("Adicionar menção de cargo")

                    Div(attrs = {
                        classes("message-config-list")
                    }) {
                        for (role in roles.filter { it.name != "@everyone" && it.name.contains(roleFilter.removePrefix("@"), true) }) {
                            Div(attrs = {
                                classes("message-config-list-entry")
                                onClick {
                                    val textArea = textAreaWrapper.textArea

                                    val selectionStart = textArea.selectionStart ?: 0
                                    val selectionEnd = textArea.selectionEnd ?: 0

                                    textAreaWrapper.replaceText(selectionStart, selectionEnd, "<@&${role.id}>")
                                    onChange.invoke(textArea.value)

                                    // Only close the menu if the shift key is not being held
                                    if (!it.shiftKey)
                                        isMenuOpen = false
                                }
                            }) {
                                // TODO - htmx-fix: Fix this!
                                /* UIIcon(SVGIconManager.roleShield) {
                                    val color = if (role.color != 0x1FFFFFFF) Color(role.color) else null

                                    style {
                                        width(1.em)
                                        height(1.em)
                                        if (color != null)
                                            color(rgb(color.red, color.green, color.blue))
                                    }
                                } */

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
}

@Composable
private fun DiscordEmojiEntityPickerButton(textAreaWrapper: TextAreaWithEntityPickers, emojis: List<DiscordEmoji>, onChange: (content: String) -> Unit) {
    Div(attrs = {
        attr("style", "position: relative; display: flex;")
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
            // TODO - htmx-fix: Fix this!
            TextWithIconWrapper(/* SVGIconManager.faceSmile, {
                attr("style", "height: 1em;")
            } */) {
                Text("Emojis")
            }
        }

        if (isMenuOpen) {
            Div(attrs = {
                classes("message-config-popover", "reset-theme-variables")

                ref {
                    val removeClickCallback = window.addEventHandler(Event.CLICK) { event ->
                        if (it.contains(event.target as Node))
                            return@addEventHandler

                        isMenuOpen = false
                    }

                    onDispose {
                        removeClickCallback()
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
                        for (emoji in emojis.filter {
                            it.name.contains(
                                emojiFilter.removePrefix(":").removeSuffix(":"),
                                true
                            )
                        }.sortedBy { it.name }) {
                            Div(attrs = {
                                attr("style", "cursor: pointer;")
                                onClick {
                                    val textArea = textAreaWrapper.textArea

                                    val selectionStart = textArea.selectionStart ?: 0
                                    val selectionEnd = textArea.selectionEnd ?: 0

                                    textAreaWrapper.replaceText(selectionStart, selectionEnd, buildString {
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
                                // TODO - htmx-mix: Refactor this!
                                var url = "https://cdn.discordapp.com/emojis/${emoji.id}"
                                url += if (emoji.animated) {
                                    ".gif"
                                } else
                                    ".png"
                                Img(src = url)
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

/**
 * A text area with Discord entity pickers
 */
private class TextAreaWithEntityPickers(private var guild: DiscordGuild, private var onChange: (String) -> (Unit)) {
    lateinit var textArea: HTMLTextAreaElement
    var cursorXY by mutableStateOf<CursorXY?>(null)
    private val updateCursorListener = EventHandler {
        updateCursor()
    }
    var matches by mutableStateOf<TypeaheadMatches?>(null)
    private val selectEntryListener = EventHandlerTest<KeyboardEvent> { event ->
        val keyCode = event.asDynamic().keyCode

        val match = matches

        if (match != null) {
            // ENTER or TAB
            // TODO: Handling space at the end would be pretty pog
            if (keyCode == 13 || keyCode == 9) {
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
            if (keyCode == 38) {
                when (match) {
                    is TypeaheadMatches.DiscordChannelMatches -> match.index.value = (match.index.value - 1).coerceAtLeast(0)
                    is TypeaheadMatches.DiscordEmojiMatches -> match.index.value = (match.index.value - 1).coerceAtLeast(0)
                    is TypeaheadMatches.DiscordRoleMatches -> match.index.value = (match.index.value - 1).coerceAtLeast(0)
                }

                // Cancel the event
                event.preventDefault()
            }

            // Arrow Down
            if (keyCode == 40) {
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
    private val getSelectionsListener = EventHandler {
        // Reset current match
        matches = null

        val selStart = textArea.selectionStart
        val selEnd = textArea.selectionEnd

        // println("selStart: $selStart")
        // println("selEnd: $selEnd")

        // They MUST be equal, if not the user is selecting a range and we don't want to handle that
        if (selStart != selEnd || selEnd == null) {
            return@EventHandler
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
                // Discord Role Mention
                controlIndexFromEnd = index
                type = TypeaheadType.ROLE
                break
            }

            // Can't be a valid selection (unless if it is a role)
            if (char == ' ' && (type != null || type == TypeaheadType.CHANNEL || type == TypeaheadType.EMOJI))
                break
        }

        if (controlIndexFromEnd != null && type != null) {
            val controlIndexFromBeginning = everythingBeforeSelEnd.length - controlIndexFromEnd
            val query = everythingBeforeSelEnd.drop(controlIndexFromBeginning)

            when (type) {
                TypeaheadType.CHANNEL -> {
                    val channels = guild.channels.filter { it.name.contains(query, true) }
                        .take(10)

                    if (channels.isEmpty()) {
                        matches = null
                        return@EventHandler
                    }

                    // The -1 is because we ignore the #
                    matches = TypeaheadMatches.DiscordChannelMatches(
                        controlIndexFromBeginning - 1,
                        controlIndexFromBeginning + query.length,
                        mutableStateOf(0),
                        channels
                    )
                }
                TypeaheadType.EMOJI -> {
                    val emojis = guild.emojis.filter { it.name.contains(query, true) }
                        .take(10)

                    if (emojis.isEmpty()) {
                        matches = null
                        return@EventHandler
                    }

                    // The -1 is because we ignore the #
                    matches = TypeaheadMatches.DiscordEmojiMatches(
                        controlIndexFromBeginning - 1,
                        controlIndexFromBeginning + query.length,
                        mutableStateOf(0),
                        emojis
                    )
                }
                TypeaheadType.ROLE -> {
                    val roles = guild.roles.filter { it.name != "@everyone" && it.name.contains(query, true) }
                        .take(10)

                    if (roles.isEmpty()) {
                        matches = null
                        return@EventHandler
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
    private val inputListener = EventHandler {
        onChange.invoke(textArea.value)
    }
    private val blurListener = EventHandler {
        GlobalScope.launch {
            // This is a HACK! We only remove the matches after 100ms because clicking the tooltip would remove the matches
            // So we wait a bit before actually removing them
            delay(100.milliseconds)
            matches = null
        }
    }

    init {
        println("Initialized TextAreaWithEntityPickers")
    }

    fun mountIn(textArea: HTMLTextAreaElement) {
        println("Mounted TextAreaWithEntityPickers")
        this.textArea = textArea
        textArea.addEventListener(Event.CLICK, updateCursorListener)
        textArea.addEventListener(Event.SELECTION_CHANGE, updateCursorListener)
        textArea.addEventListener(InputEvent.INPUT, updateCursorListener)

        textArea.addEventListener(InputEvent.INPUT, getSelectionsListener)
        textArea.addEventListener(Event.SELECTION_CHANGE, getSelectionsListener)
        textArea.addEventListener(FocusEvent.FOCUS, getSelectionsListener)

        textArea.addEventListener(InputEvent.INPUT, inputListener)

        textArea.addEventListener(KeyboardEvent.KEY_DOWN, selectEntryListener)

        textArea.addEventListener(FocusEvent.BLUR, blurListener)
    }

    fun unmount() {
        println("Unmounted TextAreaWithEntityPickers")
        textArea.removeEventListener(Event.CLICK, updateCursorListener)
        textArea.removeEventListener(Event.SELECTION_CHANGE, updateCursorListener)
        textArea.removeEventListener(InputEvent.INPUT, updateCursorListener)

        textArea.removeEventListener(InputEvent.INPUT, getSelectionsListener)
        textArea.removeEventListener(Event.SELECTION_CHANGE, getSelectionsListener)
        textArea.removeEventListener(FocusEvent.FOCUS, getSelectionsListener)

        textArea.removeEventListener(InputEvent.INPUT, inputListener)

        textArea.removeEventListener(KeyboardEvent.KEY_DOWN, selectEntryListener)

        textArea.removeEventListener(FocusEvent.BLUR, blurListener)
    }

    private fun updateCursor() {
        cursorXY = getCursorXY(textArea, textArea.selectionEnd ?: 0)
    }

    fun updateData(guild: DiscordGuild, onChange: (content: String) -> Unit) {
        this.guild = guild
        this.onChange = onChange
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

inline fun <E : Event> EventHandlerTest(
    noinline handler: (E) -> Unit,
): EventHandler<E, Nothing, Nothing> {
    return unsafeCast(handler)
}