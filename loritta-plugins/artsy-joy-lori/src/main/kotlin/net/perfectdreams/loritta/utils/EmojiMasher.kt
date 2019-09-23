package net.perfectdreams.loritta.emojimasher

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class EmojiMasher(val path: File) {
	fun mashupEmojis(emoji1: String, emoji2: String, emoji3: String?, emoji4: String?): BufferedImage {
		// MISSING:
		// https://emojipedia.org/rolling-on-the-floor-laughing/ (Fazer que rotacione)
		// https://emojipedia.org/upside-down-face/ (Fazer que fique invertido)

		// https://emojipedia.org/grinning-face-with-star-eyes/ (Não tem no Discord)
		// https://emojipedia.org/hugging-face/ (Formato Diferente)
		// https://emojipedia.org/smiling-face-with-smiling-eyes-and-hand-covering-mouth/ (Não tem no Discord)
		// https://emojipedia.org/face-with-finger-covering-closed-lips/ (Não tem no Discord)
		// https://emojipedia.org/face-with-one-eyebrow-raised/ (Não tem no Discord)
		// https://emojipedia.org/lying-face/ (Formato Diferente)
		// https://emojipedia.org/freezing-face/ (Não tem no Discord)
		// https://emojipedia.org/face-with-uneven-eyes-and-wavy-mouth/ (Não tem no Discord)
		// https://emojipedia.org/shocked-face-with-exploding-head/ (Não tem no Discord)
		// https://emojipedia.org/face-with-party-horn-and-party-hat/ (Não tem no Discord)
		// https://emojipedia.org/face-with-pleading-eyes/ (Não tem no Discord)

		// Falta fazer Face Screaming in Fear para frente

		// If the emojis have different bases
		// It takes the eyes and mouth from the first emoji
		// And the base from the second emoji

		// If the emojis have the same bases
		// It takes the eyes from the first emoji, and the mouth from the second emoji

		// For the last piece it gets the detail from the second emoji (or first, if the second doesn't have any)

		val emojiParts1 = loadEmojiParts(emoji1)!!
		val emojiParts2 = loadEmojiParts(emoji2)!!
		val emojiParts3 = emoji3?.let { loadEmojiParts(it) }
		val emojiParts4 = emoji4?.let { loadEmojiParts(it) }

		println("HERE's THE BASE SIZE")
		println("basesize1: ${emojiParts1.baseSize}")
		println("basesize2: ${emojiParts2.baseSize}")
		val sameBase = emojiParts1.baseSize == emojiParts2.baseSize
		// Caso seja a mesma base...

		var base: EmojiParts = emojiParts1
		var eyes: EmojiParts = emojiParts2
		var mouth: EmojiParts = (emojiParts3 ?: emojiParts1)
		var detail: EmojiParts = (emojiParts4 ?: emojiParts2)

		if (emojiParts4 != null && emojiParts3 != null) {
			base = emojiParts1
			eyes = emojiParts2
			mouth = emojiParts3
			detail = emojiParts4
		} else if (emojiParts3 != null) {
			base = emojiParts1
			eyes = emojiParts2
			mouth = emojiParts3

			detail = if (emojiParts4?.detail != null) {
				emojiParts4
			} else if (emojiParts3.detail != null) {
				emojiParts3
			} else if (emojiParts2.detail != null) {
				emojiParts2
			} else { emojiParts1 }
		} else {
			if (sameBase) {
				eyes = emojiParts1
				mouth = emojiParts2
				base = emojiParts1
			} else {
				eyes = emojiParts1
				mouth = emojiParts1
				base = emojiParts2
			}

			detail = if (emojiParts2.detail != null) {
				emojiParts2
			} else { emojiParts1 }
		}

		return mashupEmojis(base, eyes, mouth, detail)
	}

	fun mashupEmojis(emoji1: EmojiParts, emoji2: EmojiParts, emoji3: EmojiParts, emoji4: EmojiParts): BufferedImage {
		val newEmoji = BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB)

		val newEmojiGraphics = newEmoji.graphics

		newEmojiGraphics.drawImage(
				emoji1.base,
				0,
				0,
				null
		)

		if (emoji4.detail != null)
			newEmojiGraphics.drawImage(
					emoji4.detail,
					0,
					0,
					null
			)
		
		newEmojiGraphics.drawImage(
				emoji2.mouth,
				0,
				0,
				null
		)

		newEmojiGraphics.drawImage(
				emoji3.eyes,
				0,
				0,
				null
		)

		return newEmoji
	}

	fun loadEmojiParts(code: String): EmojiParts? {
		val emojiFolder = File(path, code)

		if (!emojiFolder.exists())
			return null

		println(emojiFolder)
		println(File(emojiFolder, "base.png").length())

		return EmojiParts(
				File(emojiFolder, "base.png").length(),
				ImageIO.read(File(emojiFolder, "base.png")),
				ImageIO.read(File(emojiFolder, "eyes.png")),
				ImageIO.read(File(emojiFolder, "mouth.png")),
				try { ImageIO.read(File(emojiFolder, "detail.png")) } catch (e: Exception) { null }
		)
	}

	data class EmojiParts(
			val baseSize: Long,
			val base: BufferedImage,
			val eyes: BufferedImage,
			val mouth: BufferedImage,
			val detail: BufferedImage?
	)
}