package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import java.net.UnknownHostException
import java.util.*

class IsUpCommand : CommandBase() {
	override fun getLabel(): String {
		return "isup"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.ISUP_DESCRIPTION
	}

	override fun getExample(): List<String> {
		return Arrays.asList("http://loritta.website/")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			var url = context.args[0];

			if (!url.startsWith("http", true)) {
				url = "http://" + url;
			}
			url = url.toLowerCase();

			try {
				var response = HttpRequest.get(url)
						.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0")
						.connectTimeout(5000)
						.readTimeout(5000)
						.code();

				if (response in 100..308) {
					context.sendMessage(context.getAsMention(true) + context.locale.ISUP_ONLINE.msgFormat(url, response));
				} else {
					context.sendMessage(context.getAsMention(true) + context.locale.ISUP_OFFLINE.msgFormat(url, response));
				}
			} catch (e: Exception) {
				var reason = e.message;
				if (e.cause is UnknownHostException) {
					reason = context.locale.ISUP_UNKNOWN_HOST.msgFormat(url);
				}
				context.sendMessage(context.getAsMention(true) + context.locale.ISUP_OFFLINE.msgFormat(url, reason));
			}
		} else {
			this.explain(context);
		}
	}
}