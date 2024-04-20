package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.ButtonDefaults
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.sticker.GuildSticker
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.UnicodeEmote
import net.perfectdreams.loritta.morenitta.interactions.vanilla.discord.GuildCommand.Companion.I18N_PREFIX
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.SimpleImageInfo
import net.perfectdreams.loritta.morenitta.utils.readAllBytes
import java.io.InputStream


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

suspend fun Guild.newSticker(
    context: UnleashedContext,
    name: String,
    description: String,
    sticker: String,
    tags: List<String>
): GuildSticker? {
    val image = (LorittaUtils.downloadFile(context.loritta, sticker, 5000) ?: context.fail(true) {
        styled(
            context.i18nContext.get(
                I18N_PREFIX.Sticker.Add.InvalidUrl
            ),
            Emotes.Error
        )
    }).readAllBytes(8_388_608)

    val allowedImageTypes = setOf("png", "gif", "json", "jpeg", "jpg")
    var imageInfo = SimpleImageInfo(image)
    var imageType = imageInfo.mimeType!!.split("/")[1]

    val imageData = if (imageType in allowedImageTypes) {
        when (imageType) {
            "jpeg", "jpg" -> LorittaUtils.convertImage(image, "png", true)
            else -> image
        }
    } else {
        null
    }!!

    imageInfo = SimpleImageInfo(imageData)
    imageType = imageInfo.mimeType!!.split("/")[1]

    return context.guild.createSticker(
        name,
        description,
        FileUpload.fromData(imageData, "sticker.$imageType"),
        tags
    ).submit(false).await()
}