package net.perfectdreams.loritta.morenitta.website.components

import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.embeds.DiscordMessage
import net.perfectdreams.loritta.common.utils.placeholders.MessagePlaceholder
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
    private val JsonForDiscordMessages = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Converts a [MessagePlaceholder] into multiple [MessageEditorMessagePlaceholder] to be used in [lorittaDiscordMessageEditor]
     */
    fun createMessageEditorPlaceholders(
        placeholder: MessagePlaceholder,
        replaceWithBackend: String,
        replaceWithFrontend: String = replaceWithBackend
    ): List<MessageEditorMessagePlaceholder> {
        return placeholder.names.map {
            MessageEditorMessagePlaceholder(
                it.placeholder.name,
                replaceWithBackend,
                replaceWithFrontend,
                placeholder.renderType
            )
        }
    }

    fun createMessageTemplate(
        title: String,
        content: String
    ) = LorittaMessageTemplate(
        title,
        content
    )

    fun createMessageTemplate(
        title: String,
        content: DiscordMessage
    ) = LorittaMessageTemplate(
        title,
        JsonForDiscordMessages.encodeToString(content)
    )

    fun DIV.lorittaDiscordMessageEditor(
        i18nContext: I18nContext,
        textAreaName: String,
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
                name = textAreaName
                // TODO: This may cause issues if the saved message does not match what we have on the db due to formatting issues
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