package com.mrpowergamerbr.loritta.commands.vanilla.undertale

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.enableFontAntiAliasing
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class UndertaleBattleCommand : AbstractCommand("utbattle", listOf("undertalebattle"), CommandCategory.UNDERTALE) {
    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale["UTBATTLE_DESCRIPTION"]
    }

    override fun getExamples(): List<String> {
        return Arrays.asList("Asriel Chara, are you there?")
    }

    override fun getUsage(): String {
        return "monstro mensagem"
    }

    override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
        if (context.args.size >= 2) {
            // Argumento 1: Monstro
            // Argumento 2...: Mensagem
            var monster = context.args.get(0).toLowerCase() // Monstro

            var list = context.args.asList()
            list = list.takeLast(list.size - 1)
            var text = list.joinToString(" ")
            // Será que é um monstro válido?
            val dir = File(Loritta.ASSETS + "utmonsters")
            val directoryListing = dir.listFiles()
            var valid = false
            val validMonsterList = ArrayList<String>()
            var file: File? = null

            if (directoryListing != null) {
                for (child in directoryListing) {
                    // Do something with child
                    if (child.nameWithoutExtension.toLowerCase().equals(monster)) {
                        valid = true
                        file = child
                        break
                    }
                    validMonsterList.add(child.nameWithoutExtension)
                }
            }

            if (valid) {
                if (!LorittaUtils.canUploadFiles(context)) { return }
                // Sim, é válido!
                var undertaleMonster = ImageIO.read(file) // Monstro
                var undertaleSpeechBox = ImageIO.read(File(Loritta.ASSETS, "speech_box.png")) // Speech Box

                val blackWhite = BufferedImage(undertaleMonster.width + undertaleSpeechBox.width + 2, undertaleMonster.height, BufferedImage.TYPE_INT_ARGB) // Criar nosso template
                val graphics = blackWhite.graphics.enableFontAntiAliasing()
                graphics.paint = (Color(0, 0, 0)) // Encher de preto
                graphics.fillRect(0, 0, blackWhite.width, blackWhite.height)
                graphics.paint = (Color(0, 0, 0)) // Encher de preto
                graphics.drawImage(undertaleMonster, 0, 0, null) // Colocar a imagem do monstro

                var startX = undertaleMonster.width + 2
                var startY = 59 - (undertaleSpeechBox.height / 2)
                graphics.drawImage(undertaleSpeechBox, startX, startY, null) // E agora o Speech Box

                graphics.paint = (Color(0, 0, 0)) // Encher de preto
                // TODO: Fonte do Undertale

                graphics.font = Constants.DOTUMCHE.deriveFont(12F)
                ImageUtils.drawTextWrapUndertale(text, startX + 18, startY + 15, startX + 90, 9999, graphics.fontMetrics, graphics)

                context.sendFile(blackWhite, "undertale_battle.png", context.getAsMention(true)) // E agora envie o arquivo
            } else {
                // Não, não é válido!
                context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["UTBATTLE_INVALID", monster, validMonsterList.joinToString(", ")])
            }
        } else {
            this.explain(context)
        }
    }
}