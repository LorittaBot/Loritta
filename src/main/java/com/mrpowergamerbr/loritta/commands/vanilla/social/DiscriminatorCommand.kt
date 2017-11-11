package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat

class DiscriminatorCommand : CommandBase("discriminator") {
	override fun getUsage(): String {
		return "<usuário>"
	}

    override fun getAliases(): List<String> {
        return listOf("discrim", "discriminador");
    }

    override fun getDescription(locale: BaseLocale): String {
        return locale.DISCRIM_DESCRIPTION
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
			context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.DISCRIM_NOBODY.msgFormat(discriminator));
			return;
		}

		context.sendMessage(context.asMention + "```ada\n$text```")
    }
}