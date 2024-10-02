package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.LoriTuberVideoCommentType

fun main() {
    LoriTuberVideoCommentType::class.nestedClasses.forEach {
        println(it.simpleName)
    }

    val clazz = LoriTuberVideoCommentType::class.nestedClasses.first {
        it.simpleName == "LikedVideoCorrectVibe1AligmentLeft0"
    }

    println(clazz.objectInstance)
    LoriTuberVideoCommentType.LikedVideoCorrectVibe1AligmentLeft0
}