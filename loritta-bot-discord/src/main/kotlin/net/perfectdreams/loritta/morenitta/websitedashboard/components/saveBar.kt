package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.BUTTON
import kotlinx.html.FlowContent
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import kotlin.collections.plus

fun FlowContent.saveBarReservedSpace() = div(classes = "save-bar-reserved-space")

fun FlowContent.saveBar(
    i18nContext: I18nContext,
    alwaysDirty: Boolean,
    resetButtonAttributes: BUTTON.() -> (Unit),
    saveButtonAttributes: BUTTON.() -> (Unit)
) {
    // Maybe, with what little power you have... You can SAVE something else.
    div(classes = "save-bar") {
        id = "save-bar"
        attributes["bliss-component"] = "save-bar"
        attributes["save-bar-track-section"] = "#section-config"

        if (alwaysDirty) {
            attributes["save-bar-always-dirty"] = "true"
            classes += "has-changes"
        } else {
            classes += "initial-state"
            classes += "no-changes"
        }

        div(classes = "save-bar-small-text") {
            text(i18nContext.get(DashboardI18nKeysData.SaveBar.TextShort))
        }

        div(classes = "save-bar-large-text") {
            text(i18nContext.get(DashboardI18nKeysData.SaveBar.TextLong))
        }

        div(classes = "save-bar-buttons") {
            id = "save-bar-buttons"
            button(classes = "discord-button no-background-light-text") {
                resetButtonAttributes.invoke(this)
                id = "save-bar-reset-button"

                div(classes = "bliss-discord-like-loading-button") {
                    div {
                        text(i18nContext.get(DashboardI18nKeysData.SaveBar.Reset))
                    }

                    div(classes = "loading-text-wrapper") {
                        img(src = LoadingSectionComponents.list.random())

                        text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                    }
                }
            }

            button(classes = "discord-button success") {
                attributes["bliss-indicator"] = "this"
                saveButtonAttributes.invoke(this)
                id = "save-bar-save-button"

                div {
                    text(i18nContext.get(DashboardI18nKeysData.SaveBar.Save))
                }

                div(classes = "loading-text-wrapper") {
                    loadingSpinnerImage()

                    text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                }
            }
        }
    }
}

/**
 * A generic save bar that PUTs everything that is marked with the `loritta-config` attribute
 */
fun FlowContent.genericUserSaveBar(
    i18nContext: I18nContext,
    alwaysDirty: Boolean,
    resourcePathRelativeToRoot: String
) {
    saveBar(
        i18nContext,
        alwaysDirty,
        {
            attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}$resourcePathRelativeToRoot"
            attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
            attributes["bliss-headers"] = buildJsonObject {
                put("Loritta-Configuration-Reset", "true")
            }.toString()
        }
    ) {
        attributes["bliss-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}$resourcePathRelativeToRoot"
        attributes["bliss-include-json"] = "[loritta-config]"
    }
}

/**
 * A generic save bar that PUTs everything that is marked with the `loritta-config` attribute
 */
fun FlowContent.genericSaveBar(
    i18nContext: I18nContext,
    alwaysDirty: Boolean,
    guild: Guild,
    resourcePathRelativeToGuild: String
) {
    saveBar(
        i18nContext,
        alwaysDirty,
        {
            attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}$resourcePathRelativeToGuild"
            attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
            attributes["bliss-headers"] = buildJsonObject {
                put("Loritta-Configuration-Reset", "true")
            }.toString()
        }
    ) {
        attributes["bliss-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}$resourcePathRelativeToGuild"
        attributes["bliss-include-json"] = "[loritta-config]"
    }
}

fun FlowContent.trackedProfileEditorSaveBar(
    i18nContext: I18nContext,
    guild: Guild,
    socialPathPart: String,
    entryId: Long,
) {
    saveBar(
        i18nContext,
        false,
        {
            attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/$socialPathPart/$entryId"
            attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
            attributes["bliss-headers"] = buildJsonObject {
                put("Loritta-Configuration-Reset", "true")
            }.toString()
        }
    ) {
        attributes["bliss-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/$socialPathPart/$entryId"
        attributes["bliss-include-json"] = "#section-config"
    }
}

fun FlowContent.trackedNewProfileEditorSaveBar(
    i18nContext: I18nContext,
    guild: Guild,
    socialPathPart: String,
    valsQuery: JsonObjectBuilder.() -> (Unit),
    valsJson: JsonObjectBuilder.() -> (Unit)
) {
    saveBar(
        i18nContext,
        true,
        {
            attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/$socialPathPart/add"
            attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
            attributes["bliss-headers"] = buildJsonObject {
                put("Loritta-Configuration-Reset", "true")
            }.toString()
            attributes["bliss-vals-query"] = buildJsonObject {
                valsQuery()
            }.toString()
        }
    ) {
        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/$socialPathPart"
        attributes["bliss-swap:200"] = "#save-bar (innerHTML) -> #save-bar (innerHTML)"
        attributes["bliss-include-json"] = "#section-config"
        attributes["bliss-vals-json"] = buildJsonObject {
            valsJson()
        }.toString()
    }
}