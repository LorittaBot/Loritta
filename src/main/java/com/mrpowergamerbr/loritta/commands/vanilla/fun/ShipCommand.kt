package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.ShipEffect
import com.mrpowergamerbr.loritta.modules.InviteLinkModule
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.ShipEffects
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class ShipCommand : AbstractCommand("ship", listOf("shippar"), CommandCategory.FUN) {
    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale["SHIP_DESCRIPTION"]
    }

	override fun getExamples(): List<String> {
		return listOf("@Loritta @SparklyBot")
	}

	override fun getUsage(): String {
		return "<usuário 1> <usuário 2>"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

    override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var user1Name: String? = context.rawArgs.getOrNull(0)
		var user2Name: String? = context.rawArgs.getOrNull(1)
		var user1AvatarUrl: String? = context.userHandle.defaultAvatarUrl
		var user2AvatarUrl: String? = context.userHandle.defaultAvatarUrl

		val user1 = context.getUserAt(0)
		val user2 = context.getUserAt(1)

		if (user1 != null) {
			user1Name = user1.name
			user1AvatarUrl = user1.effectiveAvatarUrl
		}

		if (user2 != null) {
			user2Name = user2.name
			user2AvatarUrl = user2.effectiveAvatarUrl
		}

		if (user1Name != null && user2Name != null && user1Name.isNotEmpty() && user2Name.isNotEmpty()) {
			var texto = context.getAsMention(true) + "\n💖 **${context.legacyLocale["SHIP_NEW_COUPLE"]}** 💖\n"

			texto += "`${user1Name}`\n`${user2Name}`\n"

			var name1 = user1Name.substring(0..(user1Name.length / 2))
			var name2 = user2Name.substring(user2Name.length / 2..user2Name.length - 1)
			var shipName = name1 + name2

			// Para motivos de cálculos, nós iremos criar um "real ship name"
			// Que é só o nome do ship... mas em ordem alfabética!
			var realShipName = shipName
			if (1 > user2Name.compareTo(user1Name)) {
				var reversedMentionedUsers = mutableListOf<String>()
				reversedMentionedUsers.add(user2Name)
				reversedMentionedUsers.add(user1Name)
				name1 = reversedMentionedUsers[0].substring(0..(reversedMentionedUsers[0].length / 2))
				name2 = reversedMentionedUsers[1].substring(reversedMentionedUsers[1].length / 2..reversedMentionedUsers[1].length - 1)
				realShipName = name1 + name2
			}

			var random = SplittableRandom(realShipName.hashCode().toLong() + 1)

			var percentage = random.nextInt(0, 101)

			// Loritta presa amanhã por manipulação de resultados
			if (user1 != null && user2 != null) {
				val effect = transaction(Databases.loritta) {
					ShipEffect.find {
						(((ShipEffects.user1Id eq user1.idLong) and (ShipEffects.user2Id eq user2.idLong)) or
								(ShipEffects.user2Id eq user1.idLong and (ShipEffects.user1Id eq user2.idLong))) and
								(ShipEffects.expiresAt greaterEq System.currentTimeMillis())
					}.sortedByDescending { it.expiresAt } .firstOrNull()
				}

				if (effect != null) {
					percentage = effect.editedShipValue
				}

				if (user1.id == Loritta.config.clientId || user2.id == Loritta.config.clientId) {
					if ((user1.id != Loritta.config.ownerId && user2.id != Loritta.config.ownerId) && (user1.id != "377571754698080256" && user2.id != "377571754698080256")) {
						percentage = random.nextInt(0, 51)
					}
				}
			}

			if (Loritta.RANDOM.nextInt(0, 50) == 9 && context.lorittaUser.profile.money >= 3000) {
				context.reply(
						LoriReply(
								context.locale["commands.fun.ship.bribeLove", "${Loritta.config.websiteUrl}user/@me/dashboard/ship-effects"]
						)
				)
			}


			var friendzone: String

			friendzone = if (random.nextBoolean()) {
				user1Name
			} else {
				user2Name
			}

			var messages = listOf("Isto nunca deverá aparecer!")
			if (percentage >= 90) {
				messages = context.legacyLocale.SHIP_valor90
			} else if (percentage >= 80) {
				messages = context.legacyLocale.SHIP_valor80
			} else if (percentage >= 70) {
				messages = context.legacyLocale.SHIP_valor70
			} else if (percentage >= 60) {
				messages = context.legacyLocale.SHIP_valor60
			} else if (percentage >= 50) {
				messages = context.legacyLocale.SHIP_valor50
			} else if (percentage >= 40) {
				messages = context.legacyLocale.SHIP_valor40
			} else if (percentage >= 30) {
				messages = context.legacyLocale.SHIP_valor30
			} else if (percentage >= 20) {
				messages = context.legacyLocale.SHIP_valor20
			} else if (percentage >= 10) {
				messages = context.legacyLocale.SHIP_valor10
			} else if (percentage >= 0) {
				messages = context.legacyLocale.SHIP_valor0
			}

			var emoji: BufferedImage
			if (percentage >= 50) {
				emoji = ImageIO.read(File(Loritta.ASSETS + "heart.png"))
			} else if (percentage >= 30) {
				emoji = ImageIO.read(File(Loritta.ASSETS + "shrug.png"))
			} else {
				emoji = ImageIO.read(File(Loritta.ASSETS + "crying.png"))
			}

			var resizedEmoji = emoji.getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH)

			var message = messages[random.nextInt(messages.size)]
			message = message.replace("%user%", friendzone.escapeMentions())
			message = message.replace("%ship%", "`$shipName`")
			texto += "$message"

			var avatar1Old = LorittaUtils.downloadImage("$user1AvatarUrl?size=128")
			var avatar2Old = LorittaUtils.downloadImage("$user2AvatarUrl?size=128")

			var avatar1 = avatar1Old
			var avatar2 = avatar2Old

			if (avatar1!!.height != 128 && avatar1.width != 128) {
				avatar1 = ImageUtils.toBufferedImage(avatar1.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH))
			}

			if (avatar2!!.height != 128 && avatar2.width != 128) {
				avatar2 = ImageUtils.toBufferedImage(avatar2.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH))
			}

			var image = BufferedImage(384, 128, BufferedImage.TYPE_INT_ARGB)
			var graphics = image.graphics
			graphics.drawImage(avatar1, 0, 0, null)
			graphics.drawImage(resizedEmoji, 142, 10, null)
			graphics.drawImage(avatar2, 256, 0, null)

			var embed = EmbedBuilder()
			embed.setColor(Color(255, 132, 188))

			var text = "[`"
			for (i in 0..100 step 10) {
				if (percentage >= i) {
					text += "█"
				} else {
					text += "."
				}
			}
			text += "`]"
			embed.setDescription("**$percentage%** $text")
			embed.setImage("attachment://ships.png")
			var msgBuilder = MessageBuilder().append(texto)
			msgBuilder.setEmbed(embed.build())
			context.sendFile(image, "ships.png", msgBuilder.build())
		} else {
			this.explain(context)
		}
    }
}
