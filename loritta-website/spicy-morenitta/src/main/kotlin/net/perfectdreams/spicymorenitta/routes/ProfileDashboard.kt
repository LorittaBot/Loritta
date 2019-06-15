package net.perfectdreams.spicymorenitta.routes

import kotlinx.html.DIV
import kotlinx.html.div

fun DIV.leftSidebarForProfileDashboard() {
    div(classes = "entry") {
        + "Servidores"
    }
    div(classes = "entry") {
        + "Coisa #2"
    }
    div(classes = "entry") {
        + "Coisa #3"
    }
}