package net.perfectdreams.spicymorenitta.utils

import kotlinx.html.*
import kotlinx.html.js.onChangeFunction
import kotlinx.serialization.Serializable
import org.w3c.dom.HTMLInputElement
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass

object WebsiteUtils : Logging {
    fun getUrlWithLocale(): String {
        val href = window.location.href
        val split = href.split("/")
        if (4 >= split.size)
            return href

        return split.subList(0, 4).joinToString("/")
    }

    fun getPathWithoutLocale(): String {
        var href = window.location.pathname
        if (!href.endsWith("/"))
            href += "/"

        val split = href.split("/")
        if (3 > split.size)
            return href

        var result = "/" + split.drop(2).joinToString("/")
        if (!result.endsWith("/"))
            result += "/"

        return result
    }

    fun getWebsiteLocaleIdViaPath(): String {
        val href = window.location.pathname
        val split = href.split("/")
        // /br/...
        // Então nós removemos o primeiro (que será vazio) e pegamos o primeiro que aparecer
        return split.drop(1).first()
    }

    fun canManageGuild(g: TemmieDiscordGuild) = getUserPermissionLevel(g).canAddBots

    fun getUserPermissionLevel(g: TemmieDiscordGuild): UserPermissionLevel {
        val isAdministrator = g.permissions shr 3 and 1 == 1
        val isManager = g.permissions shr 5 and 1 == 1
        return when {
            g.owner -> WebsiteUtils.UserPermissionLevel.OWNER
            isAdministrator -> WebsiteUtils.UserPermissionLevel.ADMINISTRATOR
            isManager -> WebsiteUtils.UserPermissionLevel.MANAGER
            else -> WebsiteUtils.UserPermissionLevel.MEMBER
        }
    }

    @Serializable
    enum class UserPermissionLevel(val canAddBots: Boolean) {
        OWNER(true),
        ADMINISTRATOR(true),
        MANAGER(true),
        MEMBER(false)
    }

    var currentRadioButtonIdx = 0
}

fun DIV.createRadioButton(queryName: String, title: String, subTitle: String, stringValue: String, checked: Boolean) {
    label {
        div(classes = "discord-radio-button ${if (checked) "active" else ""}") {
            input(InputType.radio) {
                id = "radio-button-${WebsiteUtils.currentRadioButtonIdx}"
                hidden = true
                name = queryName
                this.value = stringValue
                this.checked = checked

                onChangeFunction = { event ->
                    document.selectAll<HTMLInputElement>("input[name='$queryName']").forEach { // Deselect
                        if (it != event.target)
                            it.parentElement?.removeClass("active")
                    }

                    (event.target as HTMLInputElement).parentElement?.addClass("active")
                }
            }

            id = "prettified-radio-button-${WebsiteUtils.currentRadioButtonIdx}"

            div(classes = "checkbox") {
                i(classes = "fas fa-check") {}
            }

            div(classes = "info") {
                div(classes = "title") {
                    + title
                }

                div(classes = "subtitle") {
                    + subTitle
                }
            }
        }
    }

    WebsiteUtils.currentRadioButtonIdx++
}