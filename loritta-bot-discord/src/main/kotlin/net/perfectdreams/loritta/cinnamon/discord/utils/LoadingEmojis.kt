package net.perfectdreams.loritta.cinnamon.discord.utils

import net.perfectdreams.loritta.cinnamon.emotes.Emotes

object LoadingEmojis {
    val list = listOf(
        Emotes.LoriLick,
        Emotes.PantufaLick,
        Emotes.GabrielaLick,
        Emotes.PowerLick
    )

    fun random() = list.random()
}