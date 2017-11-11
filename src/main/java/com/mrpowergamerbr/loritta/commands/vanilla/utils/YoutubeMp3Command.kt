package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale


class YoutubeMp3Command : CommandBase("ytmp3") {
	override fun getUsage(): String {
		return "link"
	}

	override fun getAliases(): List<String> {
		return listOf("youtube2mp3", "youtubemp3")
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.get("YOUTUBEMP3_DESCRIPTION")
	}

	override fun getExample(): List<String> {
		return listOf("https://youtu.be/BaUwnmncsrc");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			MiscUtils.sendYouTubeVideoMp3(context, context.args[0])
		} else {
			this.explain(context);
		}
	}
}