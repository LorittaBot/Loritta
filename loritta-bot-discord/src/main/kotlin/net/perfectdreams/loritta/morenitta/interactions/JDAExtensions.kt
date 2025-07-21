package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.interactions.components.ButtonDefaults
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.UnicodeEmote


/**
 * Create a button with keyword arguments.
 *
 * This will use the defaults from [ButtonDefaults] unless specified as parameters.
 *
 * @param [id] The component id to use.
 * @param [style] The button style.
 * @param [label] The button label
 * @param [emoji] The button emoji
 *
 * @return [Button] The resulting button instance.
 */
fun linkButton(
    url: String,
    label: String? = null,
    emoji: Emote? = null,
    disabled: Boolean = false,
) = Button.of(
    ButtonStyle.LINK,
    url,
    label,
    when (emoji) {
        is DiscordEmote -> Emoji.fromCustom(emoji.name, emoji.id, emoji.animated)
        is UnicodeEmote -> Emoji.fromUnicode(emoji.asMention)
        null -> null
    }
).withDisabled(disabled)