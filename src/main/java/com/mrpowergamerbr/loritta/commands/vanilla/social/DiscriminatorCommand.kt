package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils

class DiscriminatorCommand : CommandBase() {
    override fun getLabel(): String {
        return "discriminator"
    }

	override fun getUsage(): String {
		return "<usuário>"
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
		var discriminator = user.discriminator;
		if (context.message.mentionedUsers.isNotEmpty()) {
			user = context.message.mentionedUsers[0];
			discriminator = context.message.mentionedUsers[0].discriminator;
		} else if (context.args.isNotEmpty()) {
			discriminator = context.args[0].replace(Regex("\\D+"),"");
		}
		var users = LorittaLauncher.loritta.lorittaShards.getUsers().filter { it.discriminator.equals(discriminator) }

		var text = "";
		var idx = 0;
		if (users.isNotEmpty()) {
			for (found in users) {
				text += "$idx : ${found.name}#${found.discriminator}\n";
				idx++;
			}
		} else {
			context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + "Ninguém que eu conheça possui o discriminator `#$discriminator`!");
			return;
		}

		context.sendMessage(context.asMention + "```ada\n$text```")
    }
}