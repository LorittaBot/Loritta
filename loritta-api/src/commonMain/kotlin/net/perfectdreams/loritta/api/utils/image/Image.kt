package net.perfectdreams.loritta.api.utils.image

interface Image {
	enum class ScaleType {
		SMOOTH
	}

	fun getScaledInstance(width: Int, height: Int, scaleType: ScaleType): Image

	fun createGraphics(): Graphics

	fun toByteArray(): ByteArray
}