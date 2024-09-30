package net.perfectdreams.loritta.lorituber.server

import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory

fun main() {
    val contentList = LoriTuberVideoContentCategory.entries
    val combinations = contentList.combinations(3)

    combinations.forEach { println(it) }
}

fun <T> List<T>.combinations(length: Int): List<List<T>> {
    if (length == 0) return listOf(emptyList())
    if (this.isEmpty()) return emptyList()

    val head = first()
    val tail = drop(1)

    val combWithHead = tail.combinations(length - 1).map { listOf(head) + it }
    val combWithoutHead = tail.combinations(length)

    return combWithHead + combWithoutHead
}