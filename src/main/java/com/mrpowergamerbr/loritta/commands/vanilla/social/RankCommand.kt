package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.*
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

class RankCommand : CommandBase() {
	override fun getLabel():String {
		return "rank";
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.RANK_DESCRIPTION;
	}

	override fun getAliases(): List<String> {
		return listOf("top", "leaderboard", "ranking")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.SOCIAL;
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		val list = mutableListOf<RankWrapper>()

		var global = false

		if (context.args.isNotEmpty() && context.args[0] == "global") {
			global = true
			val map = mutableMapOf<String, Int>()

			for (document in loritta.mongo.getDatabase("loritta").getCollection("servers").find()) {
				var config = loritta.ds.get(ServerConfig::class.java, document["_id"])
				config.userData.forEach {
					map.put(it.key, it.value.xp + map.getOrDefault(it.key, 0))
				}
			}

			for ((id, xp) in map) {
				val dummy = LorittaServerUserData()
				dummy.xp = xp
				list.add(RankWrapper(id, dummy))
			}

			println(list.size)
		} else {
			context.config.userData
					.forEach { list.add(RankWrapper(it.key, it.value)) }
		}

		list.sortBy { it.userData.xp }
		list.reverse()

		var idx = 0;

		var currentIndex = 0;
		var currentUserData: LorittaServerUserData? = null;

		for (entry in list) {
			if (entry.id == context.userHandle.id) {
				currentUserData = entry.userData;
				break;
			}
			currentIndex++;
		}

		val image = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB_PRE)
		val graphics = image.graphics as Graphics2D

		graphics.color = Color.BLACK
		graphics.font = Font.createFont(Font.TRUETYPE_FONT,
				FileInputStream(File(Loritta.FOLDER + "whitney_500.ttf"))).deriveFont(14F)
		graphics.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		var currentY = 48;

		var serverIcon = LorittaUtils.downloadImage(if (context.guild.iconUrl != null) context.guild.iconUrl.replace("jpg", "png") else "https://loritta.website/assets/img/unknown.png")
				.getScaledInstance(40, 40, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(9999)

		for ((id, userData) in list) {
			if (idx >= 6) {
				break;
			}
			var member = lorittaShards.getUserById(id)

			if (member != null) {
				var image = LorittaUtils.downloadImage(member.effectiveAvatarUrl)
						.getScaledInstance(40, 40, BufferedImage.SCALE_SMOOTH)
						.toBufferedImage()
						.makeRoundedCorners(9999)

				val userProfile = loritta.getLorittaProfileForUser(id)
				val file = java.io.File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png");
				val imageUrl = if (file.exists()) "http://loritta.website/assets/img/backgrounds/" + userProfile.userId + ".png?time=" + System.currentTimeMillis() else "http://loritta.website/assets/img/backgrounds/default_background.png";

				val rankBackground = LorittaUtils.downloadImage(imageUrl)
				graphics.drawImage(rankBackground.getScaledInstance(400, 300, BufferedImage.SCALE_SMOOTH)
						.toBufferedImage()
						.getSubimage(0, idx * 42, 400, 42), 0, currentY, null)
				graphics.color = Color(255, 255, 255, 230)

				var solidBackground = BufferedImage(400, 45, BufferedImage.TYPE_INT_ARGB_PRE)
				var solidGraphics = solidBackground.graphics as Graphics2D
				solidGraphics.color = Color.BLACK

				val path = Path2D.Double()
				path.moveTo(0.0, 0.0)
				path.lineTo(0.0, 42.0)
				path.lineTo(235.0, 42.0)
				path.lineTo(277.0, 0.0)
				path.closePath()

				solidGraphics.clip = path
				var gp = GradientPaint(
						0.0f, 0.0f,
						Color(250, 250, 250, 240),
						0.0f, 45.0f,
						Color(243, 243, 243, 240));
				solidGraphics.paint = gp;
				solidGraphics.fillRect(0, 0, 400, 45)

				graphics.drawImage(solidBackground, 0, currentY, null)
				graphics.color = Color.BLACK
				graphics.drawImage(image, 2, currentY + 1, null)
				graphics.drawStringWrap(member.name + if (currentUserData == userData) { " 👈" } else "", 62, currentY + graphics.fontMetrics.ascent + 2)

				val text = context.locale.RANK_INFO.msgFormat(userData.xp, userData.getCurrentLevel().currentLevel)

				graphics.color = Color(115, 127, 141, 75)
				graphics.drawString(text, 62, currentY + 39)

				graphics.color = Color(115, 127, 141)
				graphics.drawString(text, 62, currentY + 38)

				graphics.color = Color.BLACK
				currentY += 42;
			}
			idx++;
		}

		graphics.drawImage(ImageIO.read(File(Loritta.FOLDER, "rank_wrapper.png")), 0, 0, null)
		graphics.drawImage(serverIcon, 2, 3, null)

		val bebasNeue = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
				java.io.FileInputStream(java.io.File(com.mrpowergamerbr.loritta.Loritta.FOLDER + "BebasNeue.otf")))
				.deriveFont(32F)
		graphics.font = bebasNeue
		graphics.color = Color(210, 210, 210)
		val titleText = if (global) "Ranking Global" else context.locale.RANK_SERVER_RANK.f(context.guild.name)
		ImageUtils.drawCenteredString(graphics, titleText, Rectangle(0, 1, 400, 46), bebasNeue)

		graphics.color = Color.WHITE
		ImageUtils.drawCenteredString(graphics, titleText, Rectangle(0, 0, 400, 45), bebasNeue)

		context.sendFile(image.makeRoundedCorners(15), "rank.png", context.getAsMention(true))
	}

	data class RankWrapper(
			val id: String,
			val userData: LorittaServerUserData)
}