package net.perfectdreams.loritta.embededitor.editors

import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import net.perfectdreams.loritta.embededitor.EmbedEditor
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.data.DiscordMessage
import net.perfectdreams.loritta.embededitor.generator.EmbedAuthorGenerator
import net.perfectdreams.loritta.embededitor.generator.EmbedDescriptionGenerator
import net.perfectdreams.loritta.embededitor.utils.lovelyButton

object EmbedDescriptionEditor : EditorBase {
    val isNotNull: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        currentElement.classes += "clickable"

        currentElement.onClickFunction = {
            descriptionPopup(discordMessage.embed!!, m)
        }
    }

    val isNull: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        lovelyButton(
                "fas fa-align-justify",
                "Adicionar Descrição"
        ) {
            descriptionPopup(discordMessage.embed!!, m)
        }
    }

    fun descriptionPopup(embed: DiscordEmbed, m: EmbedEditor) = genericPopupTextAreaWithSaveAndDelete(
            m,
            {
                m.activeMessage!!.copy(
                        embed = m.activeMessage!!.embed!!.copy(description = it)
                )
            },
            {
                m.activeMessage!!.copy(
                        embed = m.activeMessage!!.embed!!.copy(description = null)
                )
            },
            embed.description ?: "Loritta é muito fofa!",
            DiscordEmbed.MAX_DESCRIPTION_LENGTH
    )
}