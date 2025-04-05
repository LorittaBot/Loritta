package net.perfectdreams.spicymorenitta.components

import react.FC
import react.dom.html.HTMLAttributes
import react.dom.html.ReactHTML.div
import web.cssom.ClassName
import web.html.HTMLDivElement

val VerticalListReact = FC<HTMLAttributes<HTMLDivElement>>("VerticalListReact") { props ->
    div {
        this.className = ClassName("qm vertical-list")

        + props.children
    }
}