package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.Marriage
import net.perfectdreams.loritta.morenitta.dao.ShipEffect
import net.perfectdreams.loritta.morenitta.tables.Marriages
import net.perfectdreams.loritta.morenitta.tables.ShipEffects
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.escapeMentions
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.ShipCommand
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.getEffectiveAvatarUrl
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*

class ShipCommand(loritta: LorittaBot) : AbstractCommand(loritta, "ship", listOf("shippar"), net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.ship.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.ship.examples")

	override fun getUsage() = arguments {
		argument(ArgumentType.USER) {}
		argument(ArgumentType.USER) {
			optional = true
		}
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "ship")

		val user1Name: String? = context.rawArgs.getOrNull(0)
		var user2Name: String? = context.rawArgs.getOrNull(1)

		val user1 = context.getUserAt(0)
		var user2 = context.getUserAt(1)

		if (user1Name != null && user2Name == null && user2 == null) {
			// If the user2 is null, but user1 is not null, we are going to use user2 to the user that executed the command
			//
			// So, if I use "+ship @Loritta" it would be the same thing as using "+ship @Loritta @YourUserHere"
			user2 = context.userHandle
		}

		if (user2 != null) {
			user2Name = user2.name
		}

		if (user1Name != null && user2Name != null && user1Name.isNotEmpty() && user2Name.isNotEmpty()) {
			ShipCommand.executeCompat(
				CommandContextCompat.LegacyMessageCommandContextCompat(context),
				if (user1 != null)
					ShipCommand.UserResult(user1)
				else
					ShipCommand.StringResult(user1Name),
				if (user2 != null)
					ShipCommand.UserResult(user2)
				else
					ShipCommand.StringResult(user2Name)
			)
		} else {
			this.explain(context)
		}
	}
}