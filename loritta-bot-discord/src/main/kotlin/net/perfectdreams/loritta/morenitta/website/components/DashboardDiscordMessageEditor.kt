package net.perfectdreams.loritta.morenitta.website.views.dashboard

import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.common.utils.placeholders.SectionPlaceholders
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.FancyDetails.fancyDetails
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.DiscordUser
import net.perfectdreams.loritta.serializable.messageeditor.LorittaDiscordMessageEditorSetupConfig
import net.perfectdreams.loritta.serializable.messageeditor.LorittaMessageTemplate
import net.perfectdreams.loritta.serializable.messageeditor.MessageEditorMessagePlaceholder
import net.perfectdreams.loritta.serializable.messageeditor.TestMessageTargetChannelQuery

object DashboardDiscordMessageEditor {
    fun DIV.lorittaDiscordMessageEditor(
        i18nContext: I18nContext,
        templates: List<LorittaMessageTemplate>,
        placeholderSectionType: PlaceholderSectionType,
        placeholders: List<MessageEditorMessagePlaceholder>,
        serializableGuild: DiscordGuild,
        serializableLorittaSelfUser: DiscordUser,
        testMessageTargetChannelQuery: TestMessageTargetChannelQuery,
        rawMessage: String
    ) {
        val endpointUrl = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${serializableGuild.id}/configure/test-message"
        val trackSettings = LorittaDiscordMessageEditorSetupConfig(
            templates,
            placeholderSectionType,
            placeholders,
            serializableGuild,
            serializableLorittaSelfUser,
            testMessageTargetChannelQuery,
            endpointUrl
        )
        val lorittaPlaceholders = SectionPlaceholders
            .sections.first {
                it.type == placeholderSectionType
            }.placeholders

        div {
            textArea {
                attributes["loritta-discord-message-editor"] = "true"
                attributes["loritta-discord-message-editor-config"] = Json.encodeToString(trackSettings)
                name = "message"
                text(rawMessage)
            }
        }

        fancyDetails(
            "Quais são as variáveis/placeholders que eu posso usar?"
        ) {
            table {
                thead {
                    tr {
                        th {
                            text("Placeholder")
                        }

                        th {
                            text("Significado")
                        }
                    }
                }

                tbody {
                    for (placeholder in lorittaPlaceholders) {
                        tr {
                            td {
                                // TODO: Put it inside of a code block
                                var isFirst = true
                                for (name in placeholder.names) {
                                    if (!isFirst)
                                        text(", ")
                                    isFirst = false
                                    code {
                                        text(name.placeholder.asKey)
                                    }
                                }
                            }

                            td {
                                val i18nDescription = placeholder.description
                                if (i18nDescription != null) {
                                    text(i18nContext.get(i18nDescription))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}