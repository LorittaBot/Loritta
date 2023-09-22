package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

// Maybe, with what little power you have... You can SAVE something else.
@Composable
fun SaveBar(
    m: LorittaDashboardFrontend,
    i18nContext: I18nContext,
    shouldBeVisible: Boolean,
    isSaving: Boolean,
    onReset: () -> (Unit),
    onSave: () -> (Unit)
) {
    var isInitialState by remember { mutableStateOf(true) }
    m.globalState.activeSaveBar = shouldBeVisible

    Div(attrs = {
        classes("save-bar-fill-screen-height")
    })

    Div(attrs = {
        classes("save-bar")

        ref {
            onDispose {
                println("Disposing save bar...")
                m.globalState.activeSaveBar = false
            }
        }

        if (isInitialState) {
            // This is a HACK to avoid the save bar showing up when it is inserted into the DOM
            classes("no-changes")
            classes("initial-state")

            onAnimationEnd {
                isInitialState = false
            }
        } else {
            if (shouldBeVisible)
                classes("has-changes")
            else
                classes("no-changes")
        }
    }) {
        Div(attrs = {
            classes("save-bar-small-text")
        }) {
            Text("Deseja salvar?")
        }

        Div(attrs = {
            classes("save-bar-large-text")
        }) {
            Text("Cuidado! Você tem alterações que não foram salvas")
        }

        Div(attrs = {
            classes("save-bar-buttons")
        }) {
            DiscordButton(
                DiscordButtonType.NO_BACKGROUND_LIGHT_TEXT,
                {
                    onClick {
                        onReset.invoke()
                    }
                }
            ) {
                Text("Redefinir")
            }

            DiscordButton(
                DiscordButtonType.SUCCESS,
                {
                    onClick {
                        onSave.invoke()
                    }

                    if (isSaving)
                        disabled()
                }
            ) {
                Text("Salvar")
            }
        }
    }
}