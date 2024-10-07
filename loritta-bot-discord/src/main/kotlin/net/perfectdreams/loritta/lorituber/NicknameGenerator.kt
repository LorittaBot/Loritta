package net.perfectdreams.loritta.lorituber

fun main() {
    val names = listOf("Power", "Yoru", "Skye", "Loritta", "Lori")

    for (name in names) {
        repeat(100) {
            println(
                buildString {
                    append(name)
                    append("Fan")
                    append("$it")
                }
            )
        }
    }
}