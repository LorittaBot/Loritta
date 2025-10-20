package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.textInput
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholder

fun FlowContent.customGuildCommandTextEditor(
    i18nContext: I18nContext,
    guild: Guild,
    label: String,
    message: String
) {
    fieldWrappers {
        fieldWrapper {
            fieldTitle {
                text("Nome do Comando")
            }

            textInput {
                value = label
                name = "label"
                this.attributes["bliss-transform-text"] = "trim, no-spaces, lowercase"
                this.attributes["loritta-config"] = "label"
            }
        }

        fieldWrapper {
            fieldTitle {
                text("Mensagem")
            }

            discordMessageEditor(
                guild,
                MessageEditorBootstrap.TestMessageTarget.Unavailable,
                listOf(),
                message
            ) {
                this.name = "message"
                this.attributes["loritta-config"] = "message"
            }
        }
    }
}