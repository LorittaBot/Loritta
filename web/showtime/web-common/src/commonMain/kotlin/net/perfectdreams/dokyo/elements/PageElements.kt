package net.perfectdreams.dokyo.elements

abstract class PageElements {
    private val registeredElements = mutableSetOf<String>()

    fun register(id: String) {
        if (id in registeredElements)
            throw RuntimeException("A element with ID $id is already registered!")

        registeredElements.add(id)
    }

    fun registerDiv(id: String): PageElement {
        register(id)
        return PageElement(id)
    }
}