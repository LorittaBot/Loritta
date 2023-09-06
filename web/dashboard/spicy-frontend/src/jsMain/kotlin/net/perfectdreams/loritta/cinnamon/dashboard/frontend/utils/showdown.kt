@file:JsModule("showdown")
@file:JsNonModule
package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

@JsName("Converter")
external class ShowdownConverter {
    fun makeHtml(text: String): String

    fun setOption(option: String, value: Any)

    // fun extension(name: String, exts: Array<ShowdownExtension>)
}