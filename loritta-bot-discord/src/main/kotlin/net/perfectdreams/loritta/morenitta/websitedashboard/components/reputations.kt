package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.reputations.ReputationsUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import org.jetbrains.exposed.sql.ResultRow
import kotlin.collections.plus
import kotlin.math.ceil

fun FlowContent.reputations(
    i18nContext: I18nContext,
    totalReputations: Long,
    reputations: List<ResultRow>,
    isReceivedReputation: Boolean,
    page: Int,
    usersInformation: Map<Long, CachedUserInfo?>
) {
    val path = if (isReceivedReputation)
        "received"
    else "given"

    val totalPages = ceil(totalReputations / ReputationsUtils.MAX_REPUTATIONS_PER_PAGE.toDouble())

    cardsWithHeader {
        cardHeader {
            cardHeaderInfo {
                cardHeaderTitle {
                    text(if (isReceivedReputation) "Reputações recebidas" else "Reputações enviadas")
                }

                cardHeaderDescription {
                    text("$totalReputations reputações")
                }
            }
        }

        if (reputations.isNotEmpty()) {
            div(classes = "cards") {
                for (reputation in reputations) {
                    val givenBy = if (isReceivedReputation) usersInformation[reputation[Reputations.givenById]] else usersInformation[reputation[Reputations.receivedById]]

                    reputationCard(
                        i18nContext,
                        givenBy,
                        reputation,
                        isReceivedReputation,
                        page
                    )
                }

                div {
                    style = "display: flex; gap: 16px; justify-content: space-between;"

                    discordButtonLink(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, href = if (page == 1) null else "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/reputations/$path?page=${page - 1}") {
                        classes += "text-with-icon"
                        if (page == 1) {
                            attributes["aria-disabled"] = "true"
                        } else {
                            swapRightSidebarContentsAttributes()
                        }

                        svgIcon(SVGIcons.CaretLeft)

                        text("Voltar")
                    }

                    discordButtonLink(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, href = if (page >= totalPages) null else "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/reputations/$path?page=${page + 1}") {
                        classes += "text-with-icon"
                        if (page >= totalPages) {
                            attributes["aria-disabled"] = "true"
                        } else {
                            swapRightSidebarContentsAttributes()
                        }
                        svgIcon(SVGIcons.CaretRight)

                        text("Próximo")
                    }
                }
            }
        } else {
            emptySection(i18nContext)
        }
    }
}