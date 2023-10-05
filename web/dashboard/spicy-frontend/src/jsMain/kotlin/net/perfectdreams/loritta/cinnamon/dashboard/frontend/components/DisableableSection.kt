package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement

@Composable
fun DisableableSection(
    m: LorittaDashboardFrontend,
    isAvailable: Boolean,
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
    content: ContentBuilder<HTMLDivElement>? = null
) = Div(
    attrs = {
        classes("disableable-section-wrapper")

        if (!isAvailable) {
            classes("disabled")
            attr("aria-disabled", "true")

            onClick {
                m.soundEffects.error.play(1.0)
            }
        }
    },
    content = {
        Div(
            attrs = {
                attrs?.invoke(this)
                classes("disableable-section")

                if (!isAvailable)
                    classes("disabled")
            }
        ) {
            content?.invoke(this)
        }
    }
)