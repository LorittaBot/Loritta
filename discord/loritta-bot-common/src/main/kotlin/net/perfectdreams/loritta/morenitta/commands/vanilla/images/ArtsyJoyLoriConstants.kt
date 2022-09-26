package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import java.awt.Font
import java.io.File
import java.io.FileInputStream

object ArtsyJoyLoriConstants {
    val BEBAS_NEUE by lazy {
        FileInputStream(File(LorittaBot.ASSETS + "BebasNeue.otf")).use {
            Font.createFont(Font.TRUETYPE_FONT, it)
        }
    }

    val KOMIKA by lazy {
        FileInputStream(File(LorittaBot.ASSETS + "komika.ttf")).use {
            Font.createFont(Font.TRUETYPE_FONT, it)
        }
    }
}