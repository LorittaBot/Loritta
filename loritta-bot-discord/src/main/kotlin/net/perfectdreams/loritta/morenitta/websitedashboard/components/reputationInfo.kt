package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmPurchaseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import org.jetbrains.exposed.sql.ResultRow

fun FlowContent.reputationInfo(
    i18nContext: I18nContext,
    user: CachedUserInfo?,
    reputation: ResultRow,
    isReceivedReputation: Boolean,
    page: Int,
    userSonhos: Long
) {
    val path = if (isReceivedReputation)
        "received"
    else "given"

    val content = reputation[Reputations.content]?.ifEmpty { null }

    div {
        style = "display: flex; flex-direction: column; gap: 16px;"

        simpleImageWithTextHeader(
            user?.globalName ?: user?.name ?: "???",
            user?.effectiveAvatarUrl ?: "",
            true
        )

        div {
            if (content != null) {
                text(content)
            } else {
                i {
                    text("Uma reputação sem descrição")
                }
            }
        }

        div {
            discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                val deleteModal = createEmbeddedConfirmDeletionModal(i18nContext) {
                    attributes["bliss-delete"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/reputations/${reputation[Reputations.id].value}?page=$page"
                    attributes["bliss-push-url:200"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/reputations/$path?page=$page"
                    attributes["bliss-swap:200"] = "body (innerHTML) -> #right-sidebar-contents (innerHTML)"

                }

                if (isReceivedReputation) {
                    openModalOnClick(deleteModal)
                } else {
                    openModalOnClick(
                        createEmbeddedConfirmPurchaseModal(
                            i18nContext,
                            30_000,
                            userSonhos,
                            {
                                openModalOnClick(deleteModal)
                            }
                        )
                    )
                }

                text("Excluir")
            }
        }
    }
}