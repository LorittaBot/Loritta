package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.minecraft.MCUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File

class McMoletomCommand : AbstractCommand("mcmoletom", listOf("mcsweater"), CommandCategory.MINECRAFT) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.mcsweater.description")
	override fun getExamplesKey() = LocaleKeyData("commands.category.minecraft.skinPlayerNameExamples")

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val attached = context.message.attachments.firstOrNull { it.isImage }

		var skin: BufferedImage? = null

		if (attached == null) {
			val nickname = context.args.getOrNull(0)

			if (nickname != null) {
				val profile = MCUtils.getUserProfileFromName(nickname)

				if (profile == null) {
					context.reply(
                            LorittaReply(
									locale["commands.category.minecraft.unknownPlayer", context.args.getOrNull(0)],
                                    Constants.ERROR
                            )
					)
					return
				}

				if (!profile.textures.containsKey("SKIN")) {
					context.reply(
                            LorittaReply(
                                    "Player não possui skin!",
                                    Constants.ERROR
                            )
					)
					return
				}

				skin = LorittaUtils.downloadImage(profile.textures["SKIN"]!!.url)
			} else {
				this.explain(context)
				return
			}
		} else {
			skin = LorittaUtils.downloadImage(attached.url)
		}

		if (skin != null) {
			val moletom = createSkin(skin)

			if (moletom == null) {
				context.reply(
						locale["commands.command.mcsweater.invalidSkin"],
						Constants.ERROR
				)
				return
			}

			val str = "<:loritta:331179879582269451> **|** " + context.getAsMention(true) + locale["commands.command.mcsweater.done"]
			val message = context.sendFile(moletom, "moletom.png", str)

			val image = message.attachments.first()

			message.editMessage(str + " " + locale["commands.command.mcsweater.uploadToMojang"] + " <https://minecraft.net/pt-br/profile/skin/remote/?url=${image.url}>").queue()
		} else {
			context.reply(
                    LorittaReply(
							locale["commands.category.minecraft.unknownPlayer", context.args.getOrNull(0)],
                            Constants.ERROR
                    )
			)
		}
	}

	companion object {
		suspend fun createSkin(originalSkin: BufferedImage?): BufferedImage? {
			if (originalSkin == null || (originalSkin.height != 64 && originalSkin.height != 32) || originalSkin.width != 64)
				return null

			var isPre18 = originalSkin.height == 32

			val skin = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB) // Corrige skins paletadas
			val graphics = skin.graphics as Graphics2D
			graphics.drawImage(originalSkin, 0, 0, null)

			if (isPre18) {
				fun flipAndPaste(bufferedImage: BufferedImage, x: Int, y: Int) {
					graphics.drawImage(bufferedImage, x + bufferedImage.width, y, -bufferedImage.width, bufferedImage.height, null)
				}

				// i have no idea what I'm doing
				var leg0 = deepCopy(skin.getSubimage(0, 16, 16, 4))
				var leg1 = deepCopy(skin.getSubimage(4, 20, 8, 12))
				var leg2 = deepCopy(skin.getSubimage(0, 20, 4, 12))
				var leg3 = deepCopy(skin.getSubimage(12, 20, 4, 12))

				var arm0 = deepCopy(skin.getSubimage(40, 16, 16, 4))
				var arm1 = deepCopy(skin.getSubimage(4 + 40, 20, 8, 12))
				var arm2 = deepCopy(skin.getSubimage(0 + 40, 20, 4, 12))
				var arm3 = deepCopy(skin.getSubimage(12 + 40, 20, 4, 12))

				graphics.drawImage(leg0, 16, 48, null)
				flipAndPaste(leg1, 16, 52)
				flipAndPaste(leg2, 24, 52)
				flipAndPaste(leg3, 28, 52)

				graphics.drawImage(arm0, 32, 48, null)
				flipAndPaste(arm1, 32, 52)
				flipAndPaste(arm2, 40, 52)
				flipAndPaste(arm3, 44, 52)
			}

			val alexTestColor = Color(skin.getRGB(50, 16), true)
			val isAlex = alexTestColor.alpha != 255

			val template = if (isAlex) readImage(File(Loritta.ASSETS, "template_alex.png")) else readImage(File(Loritta.ASSETS, "template_steve.png"))
			val handColor = if (isAlex) {
				Color(skin.getRGB(48, 17), true)
			} else {
				Color(skin.getRGB(49, 17), true)
			}

			var wrong = 0
			for (x in 40..43) {
				val color = Color(skin.getRGB(x, 31))
				if (handColor.red in color.red - 40 until color.red + 40) {
					if (handColor.green in color.green - 40 until color.green + 40) {
						if (handColor.blue in color.blue - 40 until color.blue + 40) {
							continue
						}
					}
				}

				wrong++
			}
			var lowerBarWrong = wrong > 2

			for (x in 40..43) {
				val color = Color(skin.getRGB(x, 30))
				if (handColor.red in color.red - 40 until color.red + 40) {
					if (handColor.green in color.green - 40 until color.green + 40) {
						if (handColor.blue in color.blue - 40 until color.blue + 40) {
							continue
						}
					}
				}

				wrong++
			}

			if (wrong > 2) {
				graphics.color = handColor
				val arm1 = deepCopy(skin.getSubimage(40, 31, if (isAlex) 14 else 16, 1))
				val arm2 = deepCopy(skin.getSubimage(32, 63, if (isAlex) 14 else 16, 1))

				// ARMS
				graphics.fillRect(40, 30, if (isAlex) 14 else 16, if (!lowerBarWrong) 1 else 2)
				graphics.fillRect(32, 62, if (isAlex) 14 else 16, if (!lowerBarWrong) 1 else 2)

				// HANDS
				if (lowerBarWrong) {
					graphics.fillRect(if (isAlex) 47 else 48, 16, if (isAlex) 3 else 4, 4)
					graphics.fillRect(if (isAlex) 39 else 40, 48, if (isAlex) 3 else 4, 4)
				} else {
					// println("Fixing arm by copying lower pixels")
					graphics.drawImage(arm1, 40, 30, null)
					graphics.drawImage(arm2, 32, 62, null)
				}
			}

			graphics.background = Color(255, 255, 255, 0)
			graphics.clearRect(16, 32, 48, 16)
			graphics.clearRect(48, 48, 16, 16)
			graphics.drawImage(template, 0, 0, null)

			return skin
		}

		fun deepCopy(bi: BufferedImage): BufferedImage {
			val cm = bi.colorModel
			val isAlphaPremultiplied = cm.isAlphaPremultiplied
			val raster = bi.copyData(bi.raster.createCompatibleWritableRaster())
			return BufferedImage(cm, raster, isAlphaPremultiplied, null)
		}
	}
}