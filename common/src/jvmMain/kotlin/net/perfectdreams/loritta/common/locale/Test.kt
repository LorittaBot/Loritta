package net.perfectdreams.loritta.common.locale

import com.ibm.icu.text.MessageFormat

fun main() {
    val a = MessageFormat("{sonhosCount, plural, zero {sonhos} one {sonho} other {sonhos}}")

    println(
        a.format(
            mapOf(
                "sonhosCount" to 2
            )
        )
    )
}