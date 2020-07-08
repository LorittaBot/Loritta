package net.perfectdreams.loritta.website.utils

interface LorittaHtmlProvider {
    fun render(page: String, arguments: List<Any?>): String
}