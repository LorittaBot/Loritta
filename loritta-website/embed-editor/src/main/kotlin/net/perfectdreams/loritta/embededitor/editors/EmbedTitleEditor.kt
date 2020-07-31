package net.perfectdreams.loritta.embededitor.editors

import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import net.perfectdreams.loritta.embededitor.EmbedEditor
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.utils.lovelyButton

object EmbedTitleEditor : EditorBase {
    val isNotNull: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        currentElement.classes += "clickable"

        currentElement.onClickFunction = {
            titlePopup(discordMessage.embed!!, m)
        }
    }

    val isNull: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        lovelyButton(
                "fas fa-heading",
                "Adicionar Título"
        ) {
            titlePopup(discordMessage.embed!!, m)
        }
    }

    fun titlePopup(embed: DiscordEmbed, m: EmbedEditor) = genericPopupTextAreaWithSaveAndDelete(
            m,
            {
                m.activeMessage!!.copy(
                        embed = m.activeMessage!!.embed!!.copy(title = it)
                )
            },
            {
                m.activeMessage!!.copy(
                        embed = m.activeMessage!!.embed!!.copy(title = null)
                )
            },
            embed.title ?: "Loritta é muito fofa!",
            DiscordEmbed.MAX_DESCRIPTION_LENGTH
    )
}