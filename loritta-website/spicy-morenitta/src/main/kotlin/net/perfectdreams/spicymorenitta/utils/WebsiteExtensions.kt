package net.perfectdreams.spicymorenitta.utils

import kotlinx.html.*

fun DIV.generateAd(adSlot: String, adName: String? = null) {
    // O "adName" não é utilizado para nada, só está aí para que fique mais fácil de analisar aonde está cada ad (caso seja necessário)
    ins(classes = "adsbygoogle") {
        style = "display: block;"

        attributes["data-ad-client"] = "ca-pub-9989170954243288"
        attributes["data-ad-slot"] = adSlot
        attributes["data-ad-format"] = "auto"
        attributes["data-full-width-responsive"] = "true"
    }

    script(type = ScriptType.textJavaScript) {
        unsafe {
            raw("(adsbygoogle = window.adsbygoogle || []).push({});")
        }
    }
}