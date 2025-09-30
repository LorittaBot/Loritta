package net.perfectdreams.loritta.dashboard.frontend.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.ContentBuilder
import web.html.HTMLButtonElement

@Composable
fun DiscordButton(
    type: ButtonType,
    attrs: AttrBuilderContext<HTMLButtonElement>? = null,
    content: ContentBuilder<HTMLButtonElement>? = null
) = Button({
    attrs?.invoke(this)

    classes(
        "discord-button",
        type.cssClass
    )
}, content)

enum class ButtonType(val cssClass: String) {
    PRIMARY("primary")
}