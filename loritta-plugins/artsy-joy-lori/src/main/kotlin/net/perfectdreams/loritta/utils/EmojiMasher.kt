package net.perfectdreams.loritta.emojimasher

import com.github.salomonbrys.kotson.fromJson
import com.mrpowergamerbr.loritta.utils.gson
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class EmojiMasher(val path: File) {
	private val emojis by lazy {
		gson.fromJson<List<StoredEmoji>>(File(path, "emojis.json").readText())
	}

	private val baseFolder = File(path, "base")
	private val eyesFolder = File(path, "eyes")
	private val mouthFolder = File(path, "mouth")
	private val detailFolder = File(path, "detail")

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
		// https://emojipedia.org/serious-face-with-symbols-covering-mouth/ (Não tem no Discord)

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
		val sameBase = emojiParts1.baseName == emojiParts2.baseName

		// Caso seja a mesma base...

		var base: EmojiParts = emojiParts1
		var eyes: EmojiParts = emojiParts2
		var mouth: EmojiParts = (emojiParts3 ?: emojiParts1)
		var detail: EmojiParts = (emojiParts4 ?: emojiParts2)

		if (emojiParts4 != null && emojiParts3 != null) {
			println("All parts available")
			base = emojiParts1
			eyes = emojiParts2
			mouth = emojiParts3
			detail = emojiParts4
		} else if (emojiParts3 != null) {
			println("All parts up to 3 available")
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
			println("Only two parts available, same base? $sameBase")
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
		var newEmoji = BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB)

		var newEmojiGraphics = newEmoji.graphics

		newEmojiGraphics.drawImage(
				emoji1.base,
				0,
				0,
				null
		)

		if (emoji1.unicode != "1f920") { // cowboy
			if (emoji4.detail != null)
				newEmojiGraphics.drawImage(
						emoji4.detail,
						0,
						0,
						null
				)
		}

		newEmojiGraphics.drawImage(
				emoji2.eyes,
				0,
				0,
				null
		)

		newEmojiGraphics.drawImage(
				emoji3.mouth,
				0,
				0,
				null
		)

		if (emoji1.unicode == "1f920") { // cowboy
			val cowboyEmoji = BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB)
			cowboyEmoji.graphics.drawImage(
					newEmoji.getScaledInstance(
							200,
							200,
							BufferedImage.SCALE_SMOOTH
					),
					20,
					40,
					null
			)
			newEmoji = cowboyEmoji
			newEmojiGraphics = newEmoji.graphics

			if (emoji4.detail != null)
				newEmojiGraphics.drawImage(
						emoji4.detail,
						0,
						0,
						null
				)
		}

		return newEmoji
	}

	fun loadEmojiParts(code: String): EmojiParts? {
		val emoji = emojis.firstOrNull { it.unicode == code } ?: return null

		return EmojiParts(
				emoji.unicode,
				emoji.base,
				ImageIO.read(File(baseFolder, emoji.base)),
				ImageIO.read(File(eyesFolder, emoji.eyes)),
				ImageIO.read(File(mouthFolder, emoji.mouth)),
				try { ImageIO.read(File(detailFolder, emoji.detail)) } catch (e: Exception) { null }
		)
	}

	fun isEmojiSupported(code: String) = emojis.any { it.unicode == code }

	data class EmojiParts(
			val unicode: String,
			val baseName: String,
			val base: BufferedImage,
			val eyes: BufferedImage,
			val mouth: BufferedImage,
			val detail: BufferedImage?
	)

	data class StoredEmoji(
			val unicode: String,
			val base: String,
			val eyes: String,
			val mouth: String,
			val detail: String?
	)
}