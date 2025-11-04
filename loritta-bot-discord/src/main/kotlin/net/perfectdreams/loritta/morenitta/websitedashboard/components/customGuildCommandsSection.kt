package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserCreatedProfilePresets
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands.CustomCommandsUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import org.jetbrains.exposed.sql.ResultRow

fun FlowContent.customGuildCommands(i18nContext: I18nContext, guild: Guild, customCommands: List<ResultRow>) {
    cardsWithHeader {
        cardHeader {
            cardHeaderInfo {
                cardHeaderTitle {
                    text("Comandos personalizados do servidor")
                }

                cardHeaderDescription {
                    text(i18nContext.get(DashboardI18nKeysData.CustomCommands.CreatedCommands(customCommands.size)))
                }
            }

            discordButtonLink(ButtonStyle.SUCCESS, href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/custom-commands/create?type=text") {
                if (customCommands.size >= CustomCommandsUtils.MAX_CUSTOM_COMMANDS)
                    attributes["aria-disabled"] = "true"
                
                swapRightSidebarContentsAttributes()

                text("Criar Comando")
            }
        }

        if (customCommands.isNotEmpty()) {
            div(classes = "cards") {
                for (customCommand in customCommands) {
                    div(classes = "card") {
                        style = "flex-direction: row; align-items: center; gap: 0.5em;"

                        div {
                            style = "flex-grow: 1;"

                            text(customCommand[CustomGuildCommands.label])
                        }

                        div {
                            style = "display: grid;grid-template-columns: 1fr 1fr;grid-column-gap: 0.5em;"

                            discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                                openModalOnClick(
                                    createEmbeddedConfirmDeletionModal(i18nContext) {
                                        attributes["bliss-delete"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/custom-commands/${customCommand[CustomGuildCommands.id]}"
                                        attributes["bliss-swap:200"] = "body (innerHTML) -> #section-config (innerHTML)"
                                    }
                                )

                                text("Excluir")
                            }

                            discordButtonLink(ButtonStyle.PRIMARY, href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/custom-commands/${customCommand[CustomGuildCommands.id]}") {
                                swapRightSidebarContentsAttributes()

                                text("Editar")
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