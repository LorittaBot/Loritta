package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Marriage
import com.mrpowergamerbr.loritta.dao.ShipEffect
import com.mrpowergamerbr.loritta.tables.Marriages
import com.mrpowergamerbr.loritta.tables.ShipEffects
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.ImageFormat
import net.perfectdreams.loritta.utils.extensions.getEffectiveAvatarUrl
import net.perfectdreams.loritta.utils.extensions.readImage
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*

class ShipCommand : AbstractCommand("ship", listOf("shippar"), CommandCategory.FUN) {
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
		var user1Name: String? = context.rawArgs.getOrNull(0)
		var user2Name: String? = context.rawArgs.getOrNull(1)
		var user1AvatarUrl: String? = context.userHandle.defaultAvatarUrl
		var user2AvatarUrl: String? = context.userHandle.defaultAvatarUrl

		val user1 = context.getUserAt(0)
		var user2 = context.getUserAt(1)

		if (user1 != null) {
			user1Name = user1.name
			user1AvatarUrl = user1.getEffectiveAvatarUrl(ImageFormat.PNG, 128)
		}

		if (user1Name != null && user2Name == null && user2 == null) {
			// If the user2 is null, but user1 is not null, we are going to use user2 to the user that executed the command
			//
			// So, if I use "+ship @Loritta" it would be the same thing as using "+ship @Loritta @YourUserHere"
			user2 = context.userHandle
		}

		if (user2 != null) {
			user2Name = user2.name
			user2AvatarUrl = user2.getEffectiveAvatarUrl(ImageFormat.PNG, 128)
		}

		if (user1Name != null && user2Name != null && user1Name.isNotEmpty() && user2Name.isNotEmpty()) {
			var texto = context.getAsMention(true) + "\n💖 **${context.locale["commands.command.ship.newCouple"]}** 💖\n"

			texto += "`${user1Name.stripCodeMarks()}`\n`${user2Name.stripCodeMarks()}`\n"

			var name1 = user1Name.substring(0..(user1Name.length / 2))
			var name2 = user2Name.substring(user2Name.length / 2..user2Name.length - 1)
			val shipName = (name1 + name2).stripCodeMarks()

			// Para motivos de cálculos, nós iremos criar um "real ship name"
			// Que é só o nome do ship... mas em ordem alfabética!
			var realShipName = shipName
			if (1 > user2Name.compareTo(user1Name)) {
				val reversedMentionedUsers = mutableListOf<String>()
				reversedMentionedUsers.add(user2Name)
				reversedMentionedUsers.add(user1Name)
				name1 = reversedMentionedUsers[0].substring(0..(reversedMentionedUsers[0].length / 2))
				name2 = reversedMentionedUsers[1].substring(reversedMentionedUsers[1].length / 2..reversedMentionedUsers[1].length - 1)
				realShipName = name1 + name2
			}

			val random = SplittableRandom(realShipName.hashCode().toLong() + 1)

			var percentage = random.nextInt(0, 101)

			// Loritta presa amanhã por manipulação de resultados
			if (user1 != null && user2 != null) {
				val marriage = loritta.newSuspendedTransaction {
					Marriage.find {
						(Marriages.user1 eq user1.idLong and (Marriages.user2 eq user2.idLong)) or
								(Marriages.user2 eq user1.idLong and (Marriages.user1 eq user2.idLong))
					}.firstOrNull()
				}

				// If the user is married, we are going to set the ship % to 100
				if (marriage != null)
					percentage = 100

				// But effects can override the percentage!
				val effect = loritta.newSuspendedTransaction {
					ShipEffect.find {
						(((ShipEffects.user1Id eq user1.idLong) and (ShipEffects.user2Id eq user2.idLong)) or
								(ShipEffects.user2Id eq user1.idLong and (ShipEffects.user1Id eq user2.idLong))) and
								(ShipEffects.expiresAt greaterEq System.currentTimeMillis())
					}.sortedByDescending { it.expiresAt }.firstOrNull()
				}

				if (effect != null) {
					percentage = effect.editedShipValue
				}

				if ((user1.id == loritta.discordConfig.discord.clientId || user2.id == loritta.discordConfig.discord.clientId) && (!loritta.config.isOwner(user1.id) && !loritta.config.isOwner(user2.id))) {
					percentage = random.nextInt(0, 51)
				}
			}

			if (Loritta.RANDOM.nextInt(0, 50) == 9 && context.lorittaUser.profile.money >= 3000) {
				context.reply(
					LorittaReply(
						context.locale["commands.command.ship.bribeLove", "${loritta.instanceConfig.loritta.website.url}user/@me/dashboard/ship-effects"]
					)
				)
			}

			val friendzone = if (random.nextBoolean()) {
				user1Name
			} else {
				user2Name
			}

			val messages: List<String> = when {
				percentage >= 90 -> context.locale.getList("commands.command.ship.value90")
				percentage >= 80 -> context.locale.getList("commands.command.ship.value80")
				percentage >= 70 -> context.locale.getList("commands.command.ship.value70")
				percentage >= 60 -> context.locale.getList("commands.command.ship.value60")
				percentage >= 50 -> context.locale.getList("commands.command.ship.value50")
				percentage >= 40 -> context.locale.getList("commands.command.ship.value40")
				percentage >= 30 -> context.locale.getList("commands.command.ship.value30")
				percentage >= 20 -> context.locale.getList("commands.command.ship.value20")
				percentage >= 10 -> context.locale.getList("commands.command.ship.value10")
				percentage >= 0  -> context.locale.getList("commands.command.ship.value0")
				else -> {
					throw RuntimeException("Can't find ship value for percentage $percentage")
				}
			}

			val emoji = when {
				percentage >= 50 -> {
					readImage(File(Loritta.ASSETS + "heart.png"))
				}
				percentage >= 30 -> {
					readImage(File(Loritta.ASSETS + "shrug.png"))
				}
				else -> {
					readImage(File(Loritta.ASSETS + "crying.png"))
				}
			}

			val resizedEmoji = emoji.getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH)

			var message = messages[random.nextInt(messages.size)]
			message = message.replace("%user%", friendzone.escapeMentions())
			message = message.replace("%ship%", "`$shipName`")
			texto += message

			val avatar1Old = user1AvatarUrl?.let { LorittaUtils.downloadImage(it) } ?: Constants.DEFAULT_DISCORD_BLUE_AVATAR
			val avatar2Old = user2AvatarUrl?.let { LorittaUtils.downloadImage(it) } ?: Constants.DEFAULT_DISCORD_BLUE_AVATAR

			var avatar1 = avatar1Old
			var avatar2 = avatar2Old

			if (avatar1.height != 128 && avatar1.width != 128) {
				avatar1 = ImageUtils.toBufferedImage(avatar1.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH))
			}

			if (avatar2.height != 128 && avatar2.width != 128) {
				avatar2 = ImageUtils.toBufferedImage(avatar2.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH))
			}

			val image = BufferedImage(384, 128, BufferedImage.TYPE_INT_ARGB)
			val graphics = image.graphics
			graphics.drawImage(avatar1, 0, 0, null)
			graphics.drawImage(resizedEmoji, 142, 10, null)
			graphics.drawImage(avatar2, 256, 0, null)

			val r = (255.0 * (percentage / 100.0)).toInt()
			val g = (132.0 * (percentage / 100.0)).toInt()
			val b = (188.0 * (percentage / 100.0)).toInt()

			val embed = EmbedBuilder()
			embed.setColor(Color(r,g,b))

			var text = "[`"
			for (i in 0..100 step 10) {
				text += if (percentage >= i) {
					"█"
				} else {
					"."
				}
			}
			text += "`]"
			embed.setDescription("**$percentage%** $text")
			embed.setImage("attachment://ships.png")
			val msgBuilder = MessageBuilder()
				.append(texto)
				.allowMentions(Message.MentionType.EMOTE)
			msgBuilder.setEmbed(embed.build())
			context.sendFile(image, "ships.png", msgBuilder.build())
		} else {
			this.explain(context)
		}
	}
}