package net.perfectdreams.loritta.embededitor.editors

import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import net.perfectdreams.loritta.embededitor.EmbedEditor
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.utils.lovelyButton

object EmbedThumbnailEditor : EditorBase {
    val isNotNull: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        currentElement.classes += "clickable"

        currentElement.onClickFunction = {
            imagePopup(discordMessage.embed!!, m)
        }
    }

    val isNull: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        lovelyButton(
                "fas fa-image",
                "Adicionar Thumbnail"
        ) {
            imagePopup(discordMessage.embed!!, m)
        }
    }

    fun imagePopup(embed: DiscordEmbed, m: EmbedEditor) = genericPopupTextInputWithSaveAndDelete(
            m,
            {
                val realLink = it.ifBlank { null }
                if (realLink != null)
                    m.activeMessage!!.copy(
                            embed = m.activeMessage!!.embed!!.copy(
                                    thumbnail = DiscordEmbed.EmbedUrl(
                                            realLink
                                    )
                            )
                    )
                else m.activeMessage!!.copy(
                        embed = m.activeMessage!!.embed!!.copy(thumbnail = null)
                )
            },
            {
                m.activeMessage!!.copy(
                        embed = m.activeMessage!!.embed!!.copy(thumbnail = null)
                )
            },
            embed.thumbnail?.url ?: "",
            6000
    )
}