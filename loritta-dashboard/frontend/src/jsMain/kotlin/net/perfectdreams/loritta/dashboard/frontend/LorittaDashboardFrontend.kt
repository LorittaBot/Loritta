package net.perfectdreams.loritta.dashboard.frontend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import net.perfectdreams.loritta.dashboard.frontend.components.ButtonType
import net.perfectdreams.loritta.dashboard.frontend.components.ChooseYourServer
import net.perfectdreams.loritta.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.dashboard.frontend.components.guilds.GuildGeneralSettings
import net.perfectdreams.loritta.dashboard.frontend.screens.ChooseYourServerScreen
import net.perfectdreams.loritta.dashboard.frontend.screens.GuildGeneralSettingsScreen
import net.perfectdreams.loritta.dashboard.frontend.screens.Screen
import org.jetbrains.compose.web.dom.Article
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Nav
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import web.dom.ElementId

class LorittaDashboardFrontend {
    val http = HttpClient {
        expectSuccess = false
    }

    var screen by mutableStateOf<Screen>(ChooseYourServerScreen())

    fun start() {
        println("Howdy from Kotlin ${KotlinVersion.CURRENT}! :3")

        renderComposable(rootElementId = ElementId("root")) {
            var counter by remember { mutableStateOf(0) }

            Div(attrs = {
                id("app-wrapper")
            }) {
                Div(attrs = {
                    id("wrapper")
                }) {
                    Nav(attrs = {
                        id("left-sidebar")
                    }) {
                        Div(attrs = {
                            classes("entries")
                        }) {
                            Div(attrs = {
                                classes("entry", "loritta-logo")
                            }) {
                                Text("Loritta")
                            }

                            Hr(attrs = { classes("divider") })
                        }
                    }

                    Section(attrs = {
                        id("right-sidebar")
                    }) {
                        Div(attrs = {
                            id("right-sidebar-wrapper")
                        }) {
                            Article(attrs = {
                                id("right-sidebar-contents")
                            }) {
                                when (val screen = screen) {
                                    is ChooseYourServerScreen -> {
                                        ChooseYourServer(this@LorittaDashboardFrontend)

                                    }
                                    is GuildGeneralSettingsScreen -> {
                                        GuildGeneralSettings(this@LorittaDashboardFrontend, screen)
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