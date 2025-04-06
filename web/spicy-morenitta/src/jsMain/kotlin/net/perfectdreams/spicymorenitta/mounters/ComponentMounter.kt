package net.perfectdreams.spicymorenitta.mounters

import web.html.HTMLElement

abstract class ComponentMounter {
    abstract fun mount(element: HTMLElement)
}