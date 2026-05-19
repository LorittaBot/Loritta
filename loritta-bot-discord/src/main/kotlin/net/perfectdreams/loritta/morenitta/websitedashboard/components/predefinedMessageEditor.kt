package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.numberInput
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.textArea
import kotlinx.html.textInput
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData

fun FlowContent.predefinedMessageEditor(
    i18nContext: I18nContext,
    guild: Guild,
    short: String?,
    message: String?,
    duration: String?,
    deleteDays: Int?
) {
    fieldWrappers {
        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text(i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Fields.Short.Title))
                }
                fieldDescription {
                    text(i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Fields.Short.Description))
                }
            }

            textInput {
                value = short ?:  i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Fields.Short.Placeholder)
                name = "short"
                attributes["loritta-config"] = "short"
                attributes["bliss-transform-text"] = "trim, no-spaces, lowercase"
                placeholder = i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Fields.Short.Placeholder)
            }
        }

        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text(i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Fields.Message.Title))
                }
                fieldDescription {
                    div {
                        handleI18nString(
                            i18nContext,
                            I18nKeys.Website.Dashboard.PredefinedMessages.Fields.Message.Description,
                            appendAsFormattedText(i18nContext, mapOf())
                        ) {
                            when (it) {
                                "banCommand" -> {
                                    TextReplaceControls.ComposableFunctionResult {
                                        span(classes = "discord-mention") {
                                            text("/" + i18nContext.get(I18nKeysData.Commands.Command.Ban.Label))
                                        }
                                    }
                                }
                                "kickCommand" -> {
                                    TextReplaceControls.ComposableFunctionResult {
                                        span(classes = "discord-mention") {
                                            text("/" + i18nContext.get(I18nKeysData.Commands.Command.Kick.Label))
                                        }
                                    }
                                }
                                "warnCommand" -> {
                                    TextReplaceControls.ComposableFunctionResult {
                                        span(classes = "discord-mention") {
                                            text("/" + i18nContext.get(I18nKeysData.Commands.Command.Warn.Label))
                                        }
                                    }
                                }
                                "muteCommand" -> {
                                    TextReplaceControls.ComposableFunctionResult {
                                        span(classes = "discord-mention") {
                                            text("/" + i18nContext.get(I18nKeysData.Commands.Command.Mute.Label))
                                        }
                                    }
                                }
                                else -> TextReplaceControls.AppendControlAsIsResult
                            }
                        }
                    }
                }
            }

            textArea {
                name = "message"
                attributes["loritta-config"] = "message"

                placeholder = i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Fields.Message.Placeholder)
                text(message ?: i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Fields.Message.Placeholder))
            }
        }

        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text(i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Fields.Duration.Title))
                }
                fieldDescription {
                    div {
                        handleI18nString(
                            i18nContext,
                            I18nKeys.Website.Dashboard.PredefinedMessages.Fields.Duration.Description,
                            appendAsFormattedText(i18nContext, mapOf())
                        ) {
                            when (it) {
                                "warnCommand" -> {
                                    TextReplaceControls.ComposableFunctionResult {
                                        span(classes = "discord-mention") {
                                            text("/" + i18nContext.get(I18nKeysData.Commands.Command.Warn.Label))
                                        }
                                    }
                                }
                                "muteCommand" -> {
                                    TextReplaceControls.ComposableFunctionResult {
                                        span(classes = "discord-mention") {
                                            text("/" + i18nContext.get(I18nKeysData.Commands.Command.Mute.Label))
                                        }
                                    }
                                }
                                else -> TextReplaceControls.AppendControlAsIsResult
                            }
                        }
                    }
                }
            }

            textInput {
                value = duration ?: ""
                name = "duration"
                attributes["loritta-config"] = "duration"
                attributes["bliss-coerce-to-null-if-blank"] = "true"
                placeholder = i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Fields.Duration.Placeholder)
            }
        }

        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text(i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Fields.DeleteDays.Title))
                }
                fieldDescription {
                    div {
                        handleI18nString(
                            i18nContext,
                            I18nKeys.Website.Dashboard.PredefinedMessages.Fields.DeleteDays.Description,
                            appendAsFormattedText(i18nContext, mapOf())
                        ) {
                            when (it) {
                                "banCommand" -> {
                                    TextReplaceControls.ComposableFunctionResult {
                                        span(classes = "discord-mention") {
                                            text("/" + i18nContext.get(I18nKeysData.Commands.Command.Ban.Label))
                                        }
                                    }
                                }
                                "kickCommand" -> {
                                    TextReplaceControls.ComposableFunctionResult {
                                        span(classes = "discord-mention") {
                                            text("/" + i18nContext.get(I18nKeysData.Commands.Command.Kick.Label))
                                        }
                                    }
                                }
                                else -> TextReplaceControls.AppendControlAsIsResult
                            }
                        }
                    }
                }
            }

            numberInput {
                value = (deleteDays ?: 0).toString()
                name = "deleteDays"
                attributes["loritta-config"] = "deleteDays"
                min = "0"
                max = "7"
            }
        }
    }
}
