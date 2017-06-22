package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext

class DiscriminatorCommand : CommandBase() {
    override fun getLabel(): String {
        return "discriminator"
    }

    override fun getAliases(): List<String> {
        return listOf("discrim", "discriminador");
    }

    override fun getDescription(): String {
        return "Veja usuários que possuem o mesmo discriminador que você ou de outro usuário!"
    }

	override fun getExample(): List<String> {
		return listOf("", "@Loritta");
	}
	override fun getCategory(): CommandCategory {
		return CommandCategory.SOCIAL;
	}

    override fun run(context: CommandContext) {
		var user = context.userHandle;
		if (context.message.mentionedUsers.isNotEmpty()) {
			user = context.message.mentionedUsers[0];
		}

		var users = LorittaLauncher.loritta.lorittaShards.getUsers().filter { it.discriminator.equals(user.discriminator) }

		var text = "";
		var idx = 0;
		for (found in users) {
			text += "$idx : ${found.name}#${found.discriminator}\n";
			idx++;
		}

		context.sendMessage(context.asMention + "```ada\n$text```")
    }
}