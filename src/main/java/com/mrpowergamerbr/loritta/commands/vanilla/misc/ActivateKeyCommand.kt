package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import org.bson.Document

class ActivateKeyCommand : AbstractCommand("activatekey", listOf("ativarkey"), category = CommandCategory.MISC) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["ACTIVATEKEY_Description"]
    }

    override fun run(context: CommandContext, locale: BaseLocale) {
	    val premiumKeyName = context.args.getOrNull(0)

	    if (premiumKeyName != null) {
		    val premiumKey = loritta.getPremiumKey(premiumKeyName)

		    if (premiumKey == null) {
			    context.reply(
					    LoriReply(
							    locale["ACTIVATEKEY_InvalidKey"],
							    Constants.ERROR
					    )
			    )
			    return
		    }

		    // Desativar a key em outro servidor, caso também use
		    loritta.serversColl.updateMany(
				    Filters.eq("premiumKey", premiumKeyName),
				    Document("\$set", Document("premiumKey", null))
		    )

		    context.config.premiumKey = premiumKeyName

		    loritta save context.config

		    context.reply(
				    LoriReply(
						    locale["ACTIVATEKEY_Success"],
						    "\uD83C\uDF89"
				    )
		    )
	    } else {
		    context.explain()
	    }
    }
}