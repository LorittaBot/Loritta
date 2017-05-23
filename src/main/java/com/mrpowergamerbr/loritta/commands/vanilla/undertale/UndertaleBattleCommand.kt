package com.mrpowergamerbr.loritta.commands.vanilla.undertale

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import net.dv8tion.jda.core.MessageBuilder
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.imageio.ImageIO

class UndertaleBattleCommand : CommandBase() {
    override fun getLabel(): String {
        return "undertalebattle";
    }

    override fun getDescription(): String {
        return "Cria um balão de fala igual ao do Undertale"
    }

    override fun getCategory(): CommandCategory {
        return CommandCategory.UNDERTALE;
    }

    override fun getExample(): List<String> {
        return Arrays.asList("Asriel Chara, are you there?")
    }

    override fun getUsage(): String {
        return "monstro mensagem"
    }

    override fun run(context: CommandContext?) {
        if (context!!.args.size >= 2) {
            // Argumento 1: Monstro
            // Argumento 2...: Mensagem
            var monster = context.args.get(0).toLowerCase(); // Monstro

            var list = context.args.asList()
            list = list.takeLast(list.size - 1);
            var text = list.joinToString(" ");
            // Será que é um monstro válido?
            val dir = File(Loritta.FOLDER + "utmonsters")
            val directoryListing = dir.listFiles()
            var valid = false;
            val validMonsterList = ArrayList<String>();
            var file: File? = null;

            if (directoryListing != null) {
                for (child in directoryListing!!) {
                    // Do something with child
                    if (child.nameWithoutExtension.toLowerCase().equals(monster)) {
                        valid = true;
                        file = child;
                        break;
                    }
                    validMonsterList.add(child.nameWithoutExtension);
                }
            }

            if (valid) {
                // Sim, é válido!
                var undertaleMonster = ImageIO.read(file); // Monstro
                var undertaleSpeechBox = ImageIO.read(File(Loritta.FOLDER, "speech_box.png")); // Speech Box

                val blackWhite = BufferedImage(undertaleMonster.width + undertaleSpeechBox.width + 2, undertaleMonster.height, BufferedImage.TYPE_INT_ARGB) // Criar nosso template
                val graphics = blackWhite.graphics as Graphics2D
                graphics.setPaint((Color(0, 0, 0))); // Encher de preto
                graphics.fillRect(0, 0, blackWhite.width, blackWhite.height);
                graphics.setPaint((Color(0, 0, 0))); // Encher de preto
                graphics.drawImage(undertaleMonster, 0, 0, null); // Colocar a imagem do monstro

                var startX = undertaleMonster.width + 2;
                var startY = 59 - (undertaleSpeechBox.height / 2)
                graphics.drawImage(undertaleSpeechBox, startX, startY, null); // E agora o Speech Box

                graphics.setPaint((Color(0, 0, 0))); // Encher de preto
                // TODO: Fonte do Undertale
                ImageUtils.drawTextWrap(text, startX + 18, startY + 15, startX + 90, 9999, graphics.fontMetrics, graphics);
                val os = ByteArrayOutputStream()
                ImageIO.write(blackWhite, "png", os)
                val inputStream = ByteArrayInputStream(os.toByteArray())

                context.sendFile(inputStream, "undertale_battle.png", " "); // E agora envie o arquivo
            } else {
                // Não, não é válido!
                context.sendMessage(context.getAsMention(true) + " Monstro \"" + monster + "\" não é válido! **Lista de monstros válidos:** " + validMonsterList.joinToString(", "))
            }
        } else {
            this.explain(context);
        }
    }
}