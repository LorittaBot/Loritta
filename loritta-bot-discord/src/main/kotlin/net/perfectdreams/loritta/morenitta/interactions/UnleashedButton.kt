package net.perfectdreams.loritta.morenitta.interactions

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton.DO_NOT_USE_THIS_COMPONENT_ID
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton.DO_NOT_USE_THIS_LINK_URL
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import java.util.*

/**
 * A button type that creates a fake JDA button
 *
 * Buttons that aren't [ButtonStyle.LINK] type created by this class can be used in JDA, however their ID will be set to [DO_NOT_USE_THIS_COMPONENT_ID] + ":" + randomUUID
 *
 * Buttons that are [ButtonStyle.LINK] will have [DO_NOT_USE_THIS_LINK_URL] set as the URL
 */
object UnleashedButton {
    // Just a dummy component ID, this SHOULD HOPEFULLY be replaced by a proper ID down the road when used with InteractivityManager
    private const val DO_NOT_USE_THIS_COMPONENT_ID = "DO_NOT_USE_THIS"
    // Just a dummy component url, this SHOULD HOPEFULLY be replaced by a proper URL down the road
    private const val DO_NOT_USE_THIS_LINK_URL = "https://loritta.website/?you-forgot-to-use-withUrl-on-the-unleashed-button"

    fun of(
        style: ButtonStyle,
        label: String? = null,
        emoji: Emote
    ) = of(style, label, emoji.toJDA())

    fun of(
        style: ButtonStyle,
        label: String? = null,
        emoji: Emoji? = null
    ): Button {
        // In recent JDA updates, JDA trips a check if the label && emoji are empty
        // This is bad for us, because we use this as a builder and, in some things, we set the emoji after the button is created, which
        // completely borks out any buttons that have an empty label + button
        //
        // To work around this, we set a " " label to bypass the check
        // This MUST be refactored later, because if JDA changes the check again, this WILL break!
        if (style == ButtonStyle.LINK)
            return Button.of(style, DO_NOT_USE_THIS_LINK_URL, label.let { if (it.isNullOrEmpty()) " " else it }, emoji)
        return Button.of(style, DO_NOT_USE_THIS_COMPONENT_ID + ":" + UUID.randomUUID().toString(), label.let { if (it.isNullOrEmpty()) " " else it }, emoji)
    }
}