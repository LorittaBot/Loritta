package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

external object Intl {
    class RelativeTimeFormat(
        locale: String,
        opts: dynamic = definedExternally
    ) {
        fun format(value: Double, unit: String): String
    }
}

