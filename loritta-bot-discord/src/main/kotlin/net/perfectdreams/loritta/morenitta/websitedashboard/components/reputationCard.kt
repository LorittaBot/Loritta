package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.img
import kotlinx.html.span
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import org.jetbrains.exposed.sql.ResultRow

fun FlowContent.reputationCard(
    i18nContext: I18nContext,
    user: CachedUserInfo?,
    reputation: ResultRow,
    isReceivedReputation: Boolean,
    page: Int
) {
    val now = System.currentTimeMillis()

    div(classes = "card") {
        style = "flex-direction: row; align-items: center; gap: 0.5em;"

        div {
            style = "flex-grow: 1; display: flex;\n" +
                    "  align-items: center;\n" +
                    "  flex-direction: row;\n" +
                    "  gap: 16px;"

            img(src = user?.effectiveAvatarUrl ?: "???") {
                style = "border-radius: 99999px;"
                width = "48"
                height = "48"
            }

            div {
                style = "display: flex; flex-direction: column;"

                div {
                    span {
                        style = "font-weight: bold;"

                        if (isReceivedReputation) {
                            text("Reputação recebida de ")
                            span(classes = "discord-mention") {
                                text("@")
                                text(user?.globalName ?: user?.name ?: "???")
                            }
                        } else {
                            text("Reputação enviada para ")
                            span(classes = "discord-mention") {
                                text("@")
                                text(user?.globalName ?: user?.name ?: "???")
                            }
                        }
                    }
                }

                div {
                    text(DateUtils.formatDateDiff(i18nContext, reputation[Reputations.receivedAt], now, maxParts = 2))
                }

                div {
                    style = "display: -webkit-box; -webkit-line-clamp: 1; overflow: hidden; text-overflow: ellipsis; -webkit-box-orient: vertical;"
                    val content = reputation[Reputations.content]?.ifEmpty { null }

                    if (content != null) {
                        text(content)
                    } else {
                        i {
                            text("Sem descrição")
                        }
                    }
                }
            }
        }

        div {
            style = "display: grid;grid-template-columns: 1fr;grid-column-gap: 0.5em;"

            discordButtonLink(
                ButtonStyle.PRIMARY,

                href = if (isReceivedReputation) {
                    "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/reputations/received/${reputation[Reputations.id].value}?page=$page"
                } else {
                    "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/reputations/given/${reputation[Reputations.id].value}?page=$page"
                }
            ) {
                swapRightSidebarContentsAttributes()

                text("Abrir")
            }
        }
    }
}