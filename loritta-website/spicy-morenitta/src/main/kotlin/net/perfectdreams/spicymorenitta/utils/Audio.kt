package net.perfectdreams.spicymorenitta.utils

@JsName("Audio")
external class Audio(url: String) {
	fun play()

	var loop: Boolean
	var volume: Number
}