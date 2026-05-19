package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.b
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.style
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationPredefinedPunishmentMessages
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.predefinedmessages.PredefinedMessagesUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import org.jetbrains.exposed.sql.ResultRow

fun FlowContent.predefinedMessagesSection(
    i18nContext: I18nContext,
    guild: Guild,
    predefinedMessages: List<ResultRow>
) {
    cardsWithHeader {
        cardHeader {
            cardHeaderInfo {
                cardHeaderTitle {
                    text(i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Title))
                }

                cardHeaderDescription {
                    text(i18nContext.get(DashboardI18nKeysData.PredefinedMessages.MessagesCount(predefinedMessages.size)))
                }
            }

            discordButtonLink(
                ButtonStyle.SUCCESS,
                href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/predefined-messages/create"
            ) {
                if (predefinedMessages.size >= PredefinedMessagesUtils.MAX_PREDEFINED_MESSAGES)
                    attributes["aria-disabled"] = "true"

                swapRightSidebarContentsAttributes()

                text(i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Actions.Create))
            }
        }

        if (predefinedMessages.isNotEmpty()) {
            div(classes = "cards") {
                for (predefined in predefinedMessages.sortedBy { it[ModerationPredefinedPunishmentMessages.short] }) {
                    val entryId = predefined[ModerationPredefinedPunishmentMessages.id].value
                    val short = predefined[ModerationPredefinedPunishmentMessages.short]
                    val message = predefined[ModerationPredefinedPunishmentMessages.message]
                    val duration = predefined[ModerationPredefinedPunishmentMessages.duration]
                    val deleteDays = predefined[ModerationPredefinedPunishmentMessages.deleteDays]

                    div(classes = "card") {
                        style = "flex-direction: row; align-items: center; gap: 0.5em;"

                        div {
                            style = "flex-grow: 1;"

                            div(classes = "icon-with-text") {
                                svgIcon(SVGIcons.Barcode) { classNames(setOf("icon")) }
                                b {
                                    text(short)
                                }
                            }

                            div(classes = "icon-with-text") {
                                svgIcon(SVGIcons.Scroll) { classNames(setOf("icon")) }
                                text(message)
                            }

                            if (!duration.isNullOrBlank()) {
                                div(classes = "icon-with-text") {
                                    svgIcon(SVGIcons.Clock) { classNames(setOf("icon")) }
                                    text(duration)
                                }
                            }

                            if (deleteDays != null) {
                                div(classes = "icon-with-text") {
                                    svgIcon(SVGIcons.Knife) { classNames(setOf("icon")) }
                                    text(i18nContext.get(DashboardI18nKeysData.PredefinedMessages.RowDeleteDaysSuffix(deleteDays = deleteDays)))
                                }
                            }
                        }

                        div {
                            style = "display: grid; grid-template-columns: 1fr 1fr; grid-column-gap: 0.5em;"

                            discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                                openModalOnClick(
                                    createEmbeddedConfirmDeletionModal(i18nContext) {
                                        attributes["bliss-delete"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/predefined-messages/$entryId"
                                        attributes["bliss-swap:200"] = "body (innerHTML) -> #section-config (innerHTML)"
                                    }
                                )

                                text(i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Actions.Delete))
                            }

                            discordButtonLink(
                                ButtonStyle.PRIMARY,
                                href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/predefined-messages/$entryId"
                            ) {
                                swapRightSidebarContentsAttributes()

                                text(i18nContext.get(DashboardI18nKeysData.PredefinedMessages.Actions.Edit))
                            }
                        }
                    }
                }
            }
        } else {
            emptySection(i18nContext)
        }
    }
}
