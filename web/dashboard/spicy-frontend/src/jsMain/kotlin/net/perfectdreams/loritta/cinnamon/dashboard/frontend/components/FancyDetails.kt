package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.ListI18nData
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Details
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Summary
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.css.lineHeight
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

@Composable
fun FAQWrapper(
    i18nContext: I18nContext,
    content: ContentBuilder<HTMLElement>? = null,
) = Div {
    LocalizedH2(i18nContext, I18nKeysData.Website.Dashboard.FrequentlyAskedQuestions)

    FancyDetailsWrapper(content)
}

@Composable
fun FancyDetailsWrapper(
    content: ContentBuilder<HTMLElement>? = null,
) = Div(
    {
        classes("fancy-details-wrapper")
    },
    content
)

@Composable
fun FancyDetails(
    attrs: AttrBuilderContext<HTMLElement>? = null,
    summary: ContentBuilder<HTMLElement>? = null,
    details: ContentBuilder<HTMLElement>? = null
) = Details(
    {
        classes("fancy-details")

        attrs?.invoke(this)
    },
    {
        Summary {
            summary?.invoke(this)

            Div(attrs = {
                classes("chevron-icon")
            }) {
                UIIcon(SVGIconManager.chevronDown)
            }
        }

        Div(
            attrs = {
                classes("details-content")

                style {
                    lineHeight("1.4")
                }
            }
        ) {
            details?.invoke(this)
        }
    }
)

@Composable
fun FancyDetails(
    i18nContext: I18nContext,
    attrs: AttrBuilderContext<HTMLElement>? = null,
    title: StringI18nData,
    description: ListI18nData
) = FancyDetails(
    attrs,
    {
        LocalizedText(i18nContext, title)
    },
    {
        for (text in i18nContext.get(description)) {
            P {
                Text(text)
            }
        }
    }
)