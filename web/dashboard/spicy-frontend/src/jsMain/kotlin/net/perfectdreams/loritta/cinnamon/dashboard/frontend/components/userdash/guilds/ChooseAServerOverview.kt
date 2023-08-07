package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.guilds

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ResourceChecker
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ChooseAServerScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalSpicyInfo
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Svg
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildsViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

enum class UserPermissionLevel(val canAddBots: Boolean) {
    OWNER(true),
    ADMINISTRATOR(true),
    MANAGER(true),
    MEMBER(false)
}

@Composable
fun ChooseAServerOverview(
    m: LorittaDashboardFrontend,
    screen: ChooseAServerScreen,
    i18nContext: I18nContext
) {
    val vm = viewModel { GuildsViewModel(m, it) }
    println("Composing Guilds...")

    fun getUserPermissionLevel(g: LorittaDashboardRPCResponse.GetUserGuildsResponse.DiscordGuild): UserPermissionLevel {
        val isAdministrator = g.permissions shr 3 and 1 == 1L
        val isManager = g.permissions shr 5 and 1 == 1L

        return when {
            g.owner -> UserPermissionLevel.OWNER
            isAdministrator -> UserPermissionLevel.ADMINISTRATOR
            isManager -> UserPermissionLevel.MANAGER
            else -> UserPermissionLevel.MEMBER
        }
    }

    ResourceChecker(i18nContext, vm.guildResource) {
        when (it) {
            is LorittaDashboardRPCResponse.GetUserGuildsResponse.Success -> {
                Div(attrs = { classes("choose-your-server") }) {
                    for (guild in it.guilds
                        .filter {
                            getUserPermissionLevel(it).canAddBots
                        }
                        .sortedBy { it.name }
                    ) {
                        Div(attrs = { classes("discord-invite-wrapper") }) {
                            Div(attrs = { classes("discord-server-details") }) {
                                Div(attrs = { classes("discord-server-icon") }) {
                                    val icon = guild.icon

                                    if (icon != null) {
                                        val extension = if (icon.startsWith("a_")) {
                                            "gif"
                                        } else "png"

                                        Img(src = "https://cdn.discordapp.com/icons/${guild.id}/$icon.$extension")
                                    }
                                }

                                Div(attrs = { classes("discord-server-info") }) {
                                    Div(attrs = { classes("discord-server-name") }) {
                                        if (guild.features.contains("VERIFIED")) {
                                            Svg(
                                                attrs = {
                                                    this.ref {
                                                        it.outerHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" aria-label=\"Verificado\" aria-hidden=\"false\" role=\"img\" width=\"16\" height=\"16\" viewBox=\"0 0 16 15.2\" class=\"\" style=\"position: relative;top: 0.1em;color: var(--loritta-blue);\"><path fill=\"currentColor\" fill-rule=\"evenodd\" d=\"m16 7.6c0 .79-1.28 1.38-1.52 2.09s.44 2 0 2.59-1.84.35-2.46.8-.79 1.84-1.54 2.09-1.67-.8-2.47-.8-1.75 1-2.47.8-.92-1.64-1.54-2.09-2-.18-2.46-.8.23-1.84 0-2.59-1.54-1.3-1.54-2.09 1.28-1.38 1.52-2.09-.44-2 0-2.59 1.85-.35 2.48-.8.78-1.84 1.53-2.12 1.67.83 2.47.83 1.75-1 2.47-.8.91 1.64 1.53 2.09 2 .18 2.46.8-.23 1.84 0 2.59 1.54 1.3 1.54 2.09z\"></path><path d=\"M7.4,11.17,4,8.62,5,7.26l2,1.53L10.64,4l1.36,1Z\" fill=\"white\"></path></svg>"

                                                        onDispose {}
                                                    }
                                                }
                                            )
                                            Text(" ")
                                        } else if (guild.features.contains("PARTNERED")) {
                                            Svg(
                                                attrs = {
                                                    this.ref {
                                                        it.outerHTML = "<svg aria-label=\"Parceiro(a) do Discord\" class=\"flowerStar-2tNFCR\" aria-hidden=\"false\" role=\"img\" width=\"16\" height=\"16\" viewBox=\"0 0 16 15.2\" style=\"position: relative;top: 0.1em;color: var(--loritta-blue);\"><path fill=\"currentColor\" fill-rule=\"evenodd\" d=\"m16 7.6c0 .79-1.28 1.38-1.52 2.09s.44 2 0 2.59-1.84.35-2.46.8-.79 1.84-1.54 2.09-1.67-.8-2.47-.8-1.75 1-2.47.8-.92-1.64-1.54-2.09-2-.18-2.46-.8.23-1.84 0-2.59-1.54-1.3-1.54-2.09 1.28-1.38 1.52-2.09-.44-2 0-2.59 1.85-.35 2.48-.8.78-1.84 1.53-2.12 1.67.83 2.47.83 1.75-1 2.47-.8.91 1.64 1.53 2.09 2 .18 2.46.8-.23 1.84 0 2.59 1.54 1.3 1.54 2.09z\"></path><path d=\"M10.5906 6.39993L9.19223 7.29993C8.99246 7.39993 8.89258 7.39993 8.69281 7.29993C8.59293 7.19993 8.39317 7.09993 8.29328 6.99993C7.89375 6.89993 7.5941 6.99993 7.29445 7.19993L6.79504 7.49993L4.29797 9.19993C3.69867 9.49993 2.99949 9.39993 2.69984 8.79993C2.30031 8.29993 2.50008 7.59993 2.99949 7.19993L5.99598 5.19993C6.79504 4.69993 7.79387 4.49993 8.69281 4.69993C9.49188 4.89993 10.0912 5.29993 10.5906 5.89993C10.7904 6.09993 10.6905 6.29993 10.5906 6.39993Z\" fill=\"white\"></path><path d=\"M13.4871 7.79985C13.4871 8.19985 13.2874 8.59985 12.9877 8.79985L9.89135 10.7999C9.29206 11.1999 8.69276 11.3999 7.99358 11.3999C7.69393 11.3999 7.49417 11.3999 7.19452 11.2999C6.39545 11.0999 5.79616 10.6999 5.29674 10.0999C5.19686 9.89985 5.29674 9.69985 5.39663 9.59985L6.79499 8.69985C6.89487 8.59985 7.09463 8.59985 7.19452 8.69985C7.39428 8.79985 7.59405 8.89985 7.69393 8.99985C8.09346 8.99985 8.39311 8.99985 8.69276 8.79985L9.39194 8.39985L11.3896 6.99985L11.6892 6.79985C12.1887 6.49985 12.9877 6.59985 13.2874 7.09985C13.4871 7.39985 13.4871 7.59985 13.4871 7.79985Z\" fill=\"white\"></path></svg>"

                                                        onDispose {}
                                                    }
                                                }
                                            )
                                            Text(" ")
                                        }

                                        Text(guild.name)
                                    }

                                    Div(attrs = { classes("discord-server-description") }) {
                                        val userPermissionLevel = getUserPermissionLevel(guild)

                                        Text(
                                            when (userPermissionLevel) {
                                                UserPermissionLevel.OWNER -> i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.Owner)
                                                UserPermissionLevel.ADMINISTRATOR -> i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.Administrator)
                                                UserPermissionLevel.MANAGER -> i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.Manager)
                                                UserPermissionLevel.MEMBER -> i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.Member)
                                            }
                                        )
                                    }
                                }

                                Div(attrs = {
                                    style {
                                        this.property("margin-left", "auto")
                                    }
                                }) {
                                    val spicyInfo = LocalSpicyInfo.current

                                    A(href = "${spicyInfo.legacyDashboardUrl}/guild/${guild.id}/configure") {
                                        DiscordButton(DiscordButtonType.PRIMARY) {
                                            Text(i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.ManageServer))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}