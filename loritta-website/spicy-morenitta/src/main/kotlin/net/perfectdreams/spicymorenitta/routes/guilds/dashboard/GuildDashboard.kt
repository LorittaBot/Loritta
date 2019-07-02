package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import kotlinx.coroutines.delay
import kotlinx.html.*
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.GuildDashboard.isModified
import net.perfectdreams.spicymorenitta.utils.WebsiteUtils
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLBodyElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.hasClass
import kotlin.dom.removeClass

object GuildDashboard {
    var isModified = false
}

fun DIV.createToggle(title: String, subText: String? = null, id: String? = null, isChecked: Boolean = false) {
    div(classes = "toggleable-wrapper") {
        div(classes = "information") {
            div {
                + title
            }
            if (subText != null) {
                div(classes = "sub-text") {
                    + subText
                }
            }
        }

        label("switch") {
            attributes["for"] = id ?: "checkbox"

            input(InputType.checkBox) {
                attributes["id"] = id ?: "checkbox"

                if (isChecked) {
                    attributes["checked"] = "true"
                }

                onChangeFunction = {
                    displayUnsavedAlert()
                    isModified = true
                }
            }
            div(classes = "slider round") {}
        }
    }
}

fun displayUnsavedAlert() {
    if (isModified)
        return

    val unsavedAlert = document.select<HTMLDivElement>("#not-saved-alert")

    unsavedAlert.removeClass("reversed", "invisible")

    resetAnimation(unsavedAlert)
}

fun hideUnsavedAlert() {
    if (!isModified)
        return

    val unsavedAlert = document.select<HTMLDivElement>("#not-saved-alert")

    unsavedAlert.removeClass("invisible")
    unsavedAlert.addClass("reversed")
    resetAnimation(unsavedAlert)
}

fun resetAnimation(element: HTMLElement) {
    element.style.display = "none" // reset animation
    SpicyMorenitta.INSTANCE.launch { // Precisa ter um delay para o reset da animação funcionar
        delay(5)
        element.style.display = "block"
    }
}

fun DIV.leftSidebarForGuildDashboard() {
    val function: (Event) -> Unit = {
        if (isModified) {
            SpicyMorenitta.INSTANCE.launch {
                val content = document.select<HTMLDivElement>("#content")
                val body = document.select<HTMLBodyElement>("body")
                val unsavedAlert = document.select<HTMLDivElement>("#not-saved-alert")

                if (unsavedAlert.hasClass("warning")) // Já está sendo avisado, ignore
                    return@launch

                unsavedAlert.addClass("warning")
                content.addClass("shake")
                body.style.asDynamic().overflow = "hidden"

                delay(750)

                unsavedAlert.removeClass("warning")
                content.removeClass("shake")
                body.style.asDynamic().overflow = ""
            }
        }
    }

    img(src = "https://cdn.discordapp.com/icons/297732013006389252/75327a9cf9ad3a2fc76945d06dc897aa.png") {
        width = "128"
        height = "128"
        style = "border-radius: 100%;"
    }

    hr {}

    div(classes = "entry") {
        i("fas fa-cog") {}

        + " Configurações gerais"

        onClickFunction = function
    }
    div(classes = "entry") {
        i("fas fa-user-shield") {}

        + " Moderação"

        onClickFunction = function
    }
    div(classes = "entry") {
        i("fas fa-terminal") {}

        + " Comandos"

        onClickFunction = function
    }
    div(classes = "entry") {
        i("fas fa-address-card") {}

        + " Permissões"

        onClickFunction = function
    }
    hr {}
    div(classes = "section-title") {
        + "Notificações"
    }
    div(classes = "entry") {
        i("fas fa-sign-in-alt") {}

        + " Mensagens de Entrada/Saída"
    }
    div(classes = "entry") {
        i("fas fa-eye") {}

        + " Event Log"
    }
    hr {}
    div(classes = "section-title") {
        + "Miscelânea"
    }
    div(classes = "entry") {
        i("fas fa-briefcase") {}

        + " Autorole"
    }
    div(classes = "entry") {
        i("fas fa-ban") {}

        + " Bloqueador de Convites"
    }
    div(classes = "entry") {
        i("fas fa-music") {}

        + " DJ Loritta"
    }
    div(classes = "entry") {
        i("fas fa-star") {}

        + " Starboard"
    }
    div(classes = "entry") {
        i("fas fa-shuffle") {}

        + " Miscelânea"
    }
    hr {}
    div(classes = "section-title") {
        + "Premium"
    }
    div(classes = "entry") {
        i("fas fa-gift") {}

        + " Premium Keys"
    }
    div(classes = "entry") {
        i("fas fa-certificate") {}

        + " Emblema Personalizado"
    }
    div(classes = "entry") {
        i("fas fa-times") {}

        + " Multiplicador de Sonhos"
    }
    hr {}
    div(classes = "section-title") {
        + "Precisando de Ajuda?"
    }
    a(href = WebsiteUtils.getUrlWithLocale() + "/support") {
        div(classes = "entry") {
            attributes["data-enable-link-preload"] = "true"

            i("fas fa-question-circle") {}

            +" Suporte"
        }
    }
}