package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import java.util.*

class ShipCommand : CommandBase() {
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

    override fun run(context: CommandContext) {
		if (context.message.mentionedUsers.size == 2) {
			var texto = context.getAsMention(true) + "\n💖 **Hmmm, será que nós temos um novo casal aqui?** 💖\n";
			var size = context.message.mentionedUsers.size;
			// var
			for (user in context.message.mentionedUsers) {
				texto += "`${user.name}`\n";
			}

			var name1 = context.message.mentionedUsers[0].name.substring(0..(context.message.mentionedUsers[0].name.length / 2));
			var name2 = context.message.mentionedUsers[1].name.substring(context.message.mentionedUsers[1].name.length / 2..context.message.mentionedUsers[1].name.length - 1);
			var shipName = name1 + name2;
			var random = SplittableRandom(shipName.hashCode().toLong());

			var rand = random.nextInt(5);
			var friendzone = "";

			friendzone = if (random.nextBoolean()) {
				context.message.mentionedUsers[0].name;
			} else { context.message.mentionedUsers[1].name; }

			if (rand == 0) {
				texto += "😍 `$shipName` **parece que eles são um bom casal!** 😍";
			} else if (rand == 1) {
				texto += "💗 `$shipName` **parece que eles são um casal perfeito!** 💗";
			} else if (rand == 2) {
				texto += "😐 `$shipName` **parece que só são conhecidos...** 😐";
			} else if (rand == 3) {
				texto += "😢 `$shipName` **parece que o `$friendzone` deixou na friendzone...** 😢";
			} else if (rand == 4) {
				texto += "😏 `$shipName` **mas esses dois já se conhecem há muito tempo...** 😏";
			}
			context.sendMessage(texto);
		} else {
			this.explain(context);
		}
    }
}