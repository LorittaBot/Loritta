package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.msgFormat
import com.mrpowergamerbr.loritta.utils.save

class HelloWorldCommand : CommandBase() {
    override fun getLabel(): String {
        return "helloworld"
    }

    override fun getDescription(locale: BaseLocale): String {
        return locale.HELLO_WORLD_DESCRIPTION
    }

    override fun run(context: CommandContext) {
		if (context.userHandle.id == Loritta.config.ownerId && context.args.isNotEmpty()) {
			var newLocale = context.args[0]

			if (newLocale == "info") {
				var text = "**Progresso de tradução das localizações:**\n"
				val default = loritta.getLocaleById("default")
				val strings = default::class.java.declaredFields.size
				text += "**Número de Textos:** $strings\n\n"
				context.sendMessage(text)
				for ((id, locale) in loritta.locales) {
					text = ""
					if (id != "default") {
						var translatedStrings = 0
						var missing = mutableListOf<String>();

						for (field in default::class.java.declaredFields) {
							field.isAccessible = true
							println("${field.get(locale)} - ${field.get(default)}")
							if (field.get(locale) != field.get(default)) {
								translatedStrings++
							} else {
								missing.add(field.name)
							}
						}

						text += "**Locale $id:** $translatedStrings de $strings textos traduzidos\n"
						if (missing.isNotEmpty()) {
							text += "**Textos faltando...** " + missing.joinToString(", ") + "\n"
						}
						text += "\n"
						context.sendMessage(if (text.length > 2000) text.substring(0..1999) else text)
					}
				}
				return
			}
			context.config.localeId = newLocale

			loritta save context.config

			context.sendMessage(context.locale.USING_LOCALE.msgFormat(context.config.localeId))
			return
		}
        context.sendMessage(context.locale.HELLO_WORLD.msgFormat("\uD83D\uDE04"))
    }
}