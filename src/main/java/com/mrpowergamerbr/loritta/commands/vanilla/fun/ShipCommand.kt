package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class ShipCommand : CommandBase() {
	val valor90 = listOf("😍 %ship% Os dois se amam! 😍",
			"💗 %ship% Casal mais perfeito? Impossível! 💗",
			"☠ %ship% Nem a *dona* morte separa! 😂",
			"😋 %ship% Casal mais perfeito que eu! 😋",
			"😚 %ship% Casal? Casal que nada! Eles já são casados! 😚")

	val valor80 = listOf("😏 %ship% Mas esses dois já se conhecem faz muito tempo... 😏",
			"😊 %ship% Claro que os dois são um lindo casal! 😊",
			"😜 %ship% Casal mais grudento que Despacito! 😜",
			"😄 %ship% Se os dois já não namoram eu estaria surpresa! 😄")

	val valor70 = listOf("🙂 %ship% Prevejo um casal fofo se formando! 🙂",
			"😄 %ship% Só precisa de um pouco mais de conversa para rolar! 😜",
			"😊 %ship% Os dois foram feitos um para o outro! 😊",
			"😄 %ship% Sim! 😄")

	val valor60 = listOf("🙂 %ship% Se o/a %user% parasse de ser um pouco tímido(a)... 😏",
			"😊 %ship% Tem um pouco de interesses compatíveis aí 😊",
			"🙂 %ship% Eu aprovo esse casal! 🙂",
			"😄 %ship% Sim! 😄")

	val valor50 = listOf("😶 %ship% Amigos... Mas talvez né... 😏",
			"😊 %ship% Talvez... Só precisa o/a %user% querer! 😶",
			"😶 %ship% Eu queria ver esse casal funcionar 😶")

	val valor40 = listOf("😶 %ship% É... talvez, eu acho... 🙁",
			"😶 %ship% Nada é impossível, mas... 🙁",
			"😶 %ship% Se dois quererem, talvez dê certo... Mas... 😶")

	val valor30 = listOf("😕 %ship% Acho que não seria um belo casal... 🙁",
			"😶 %ship% Parece que só são conhecidos... 😶")

	val valor20 = listOf("😐 %ship% Se o(a) %user% não tivesse deixado na friendzone... 😐")

	val valor10 = listOf("😐 %ship% Eu queria muito falar que é possível, mas... 😢")

	val valor0 = listOf("😭 %ship% As possibilidades de ter este casal são quase impossíveis! 😭")

    override fun getLabel(): String {
        return "ship"
    }

    override fun getDescription(): String {
        return "Veja se um casal daria certo (ou não!)"
    }

	override fun getExample(): List<String> {
		return listOf("@Loritta @SparklyBot");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun getUsage(): String {
		return "<usuário 1> <usuário 2>";
	}

    override fun run(context: CommandContext) {
		if (context.message.mentionedUsers.size == 2) {
			var texto = context.getAsMention(true) + "\n💖 **Hmmm, será que nós temos um novo casal aqui?** 💖\n";

			for (user in context.message.mentionedUsers) {
				texto += "`${user.name}`\n";
			}

			var name1 = context.message.mentionedUsers[0].name.substring(0..(context.message.mentionedUsers[0].name.length / 2));
			var name2 = context.message.mentionedUsers[1].name.substring(context.message.mentionedUsers[1].name.length / 2..context.message.mentionedUsers[1].name.length - 1);
			var shipName = name1 + name2;

			// Para motivos de cálculos, nós iremos criar um "real ship name"
			// Que é só o nome do ship... mas em ordem alfabética!
			var realShipName = shipName;
			if (1 > context.message.mentionedUsers[1].name.compareTo(context.message.mentionedUsers[0].name)) {
				var reversedMentionedUsers = context.message.mentionedUsers.toMutableList();
				reversedMentionedUsers.reverse();
				name1 = reversedMentionedUsers[0].name.substring(0..(reversedMentionedUsers[0].name.length / 2));
				name2 = reversedMentionedUsers[1].name.substring(reversedMentionedUsers[1].name.length / 2..reversedMentionedUsers[1].name.length - 1);
				realShipName = name1 + name2;
			}

			var random = SplittableRandom(realShipName.hashCode().toLong() + 1);

			var percentage = random.nextInt(0, 101);

			// Loritta presa amanhã por manipulação de resultados
			if (context.message.mentionedUsers[0].id == Loritta.config.clientId || context.message.mentionedUsers[1].id == Loritta.config.clientId) {
				if (context.message.mentionedUsers[0].id != Loritta.config.ownerId && context.message.mentionedUsers[1].id != Loritta.config.ownerId) {
					percentage = random.nextInt(0, 51);
				}
			}

			var friendzone: String;

			friendzone = if (random.nextBoolean()) {
				context.message.mentionedUsers[0].name;
			} else {
				context.message.mentionedUsers[1].name;
			}

			var messages = listOf("Isto nunca deverá aparecer!");
			if (percentage >= 90) {
				messages = valor90;
			} else if (percentage >= 80) {
				messages = valor80;
			} else if (percentage >= 70) {
				messages = valor70;
			} else if (percentage >= 60) {
				messages = valor60;
			} else if (percentage >= 50) {
				messages = valor50;
			} else if (percentage >= 40) {
				messages = valor40;
			} else if (percentage >= 30) {
				messages = valor30;
			} else if (percentage >= 20) {
				messages = valor20;
			} else if (percentage >= 10) {
				messages = valor10;
			} else if (percentage >= 0) {
				messages = valor0;
			}

			var emoji: BufferedImage;
			if (percentage >= 50) {
				emoji = ImageIO.read(File(Loritta.FOLDER + "heart.png"));
			} else if (percentage >= 30) {
				emoji = ImageIO.read(File(Loritta.FOLDER + "shrug.png"));
			} else {
				emoji = ImageIO.read(File(Loritta.FOLDER + "crying.png"));
			}

			var resizedEmoji = emoji.getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH);

			var message = messages[random.nextInt(messages.size)];
			message = message.replace("%user%", friendzone);
			message = message.replace("%ship%", "`$shipName`");
			texto += "$message";

			var avatar1Old = LorittaUtils.downloadImage(context.message.mentionedUsers[0].effectiveAvatarUrl + "?size=128")
			var avatar2Old = LorittaUtils.downloadImage(context.message.mentionedUsers[1].effectiveAvatarUrl + "?size=128")

			var avatar1 = avatar1Old;
			var avatar2 = avatar2Old;


			if (avatar1.height != 128 && avatar1.width != 128) {
				avatar1 = ImageUtils.toBufferedImage(avatar1.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH));
			}

			if (avatar2.height != 128 && avatar2.width != 128) {
				avatar2 = ImageUtils.toBufferedImage(avatar2.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH));
			}

			var image = BufferedImage(384, 128, BufferedImage.TYPE_INT_ARGB);
			var graphics = image.graphics;
			graphics.drawImage(avatar1, 0, 0, null);
			graphics.drawImage(resizedEmoji, 142, 10, null);
			graphics.drawImage(avatar2, 256, 0, null);

			var embed = EmbedBuilder();
			embed.setColor(Color(255, 132, 188));

			var text = "[`";
			for (i in 0..100 step 10) {
				if (percentage > i) {
					text += "█";
				} else {
					text += ".";
				}
			}
			text += "`]"
			embed.setDescription("**$percentage%** $text");
			embed.setImage("attachment://ships.png");
			var msgBuilder = MessageBuilder().append(texto);
			msgBuilder.setEmbed(embed.build());
			context.sendFile(image, "ships.png", msgBuilder.build());
		} else {
			this.explain(context);
		}
    }
}