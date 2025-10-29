package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.textInput
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedBlueskyAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfile
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import org.jetbrains.exposed.sql.ResultRow

fun FlowContent.trackedBlueskyProfilesSection(
    i18nContext: I18nContext,
    guild: Guild,
    trackedProfiles: List<TrackedProfile>,
) {
    trackedProfilesSection(
        i18nContext,
        guild,
        "Contas que você está seguindo",
        i18nContext.get(DashboardI18nKeysData.Bluesky.Accounts(trackedProfiles.size)),
        {
            openModalOnClick(
                createEmbeddedModal(
                    "Adicionar Conta",
                    true,
                    {
                        textInput {
                            name = "handle"
                            placeholder = "@loritta.website"
                        }
                    },
                    listOf(
                        {
                            defaultModalCloseButton(i18nContext)
                        },
                        {
                            discordButton(ButtonStyle.PRIMARY) {
                                id = "add-profile"
                                attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/bluesky/add"
                                attributes["bliss-include-query"] = ".modal [name='handle']"
                                attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
                                attributes["bliss-push-url:200"] = "true"

                                text("Continuar")
                            }
                        }
                    )
                )
            )

            text("Adicionar Conta")
        },
        "bluesky",
        trackedProfiles
    )
}