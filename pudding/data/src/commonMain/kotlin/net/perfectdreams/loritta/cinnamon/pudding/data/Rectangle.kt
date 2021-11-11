package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class Rectangle(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)