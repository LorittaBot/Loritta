package net.perfectdreams.loritta.morenitta.commands.vanilla.undertale

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*

class UndertaleBattleCommand(loritta: LorittaBot) : AbstractCommand(loritta, "utbattle", listOf("undertalebattle"), net.perfectdreams.loritta.common.commands.CommandCategory.UNDERTALE) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.utbattle.description")

    override fun getExamples(): List<String> {
        return Arrays.asList("Asriel Chara, are you there?")
    }

    // TODO: Fix Usage

    override suspend fun run(context: CommandContext,locale: BaseLocale) {
        if (context.args.size >= 2) {
            // Argumento 1: Monstro
            // Argumento 2...: Mensagem
            var monster = context.args.get(0).lowercase() // Monstro

            var list = context.args.asList()
            list = list.takeLast(list.size - 1)
            var text = list.joinToString(" ")
            // Será que é um monstro válido?
            val dir = File(LorittaBot.ASSETS + "utmonsters")
            val directoryListing = dir.listFiles()
            var valid = false
            val validMonsterList = ArrayList<String>()
            var file: File? = null

            if (directoryListing != null) {
                for (child in directoryListing) {
                    // Do something with child
                    if (child.nameWithoutExtension.lowercase().equals(monster)) {
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
                var undertaleMonster = readImage(file!!) // Monstro
                var undertaleSpeechBox = readImage(File(LorittaBot.ASSETS, "speech_box.png")) // Speech Box

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
                ImageUtils.drawTextWrap(loritta, text, startX + 18, startY + 15, startX + 90, 9999, graphics.fontMetrics, graphics)

                context.sendFile(blackWhite, "undertale_battle.png", context.getAsMention(true)) // E agora envie o arquivo
            } else {
                // Não, não é válido!
                context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["commands.command.utbattle.invalid", monster, validMonsterList.joinToString(", ")])
            }
        } else {
            this.explain(context)
        }
    }
}