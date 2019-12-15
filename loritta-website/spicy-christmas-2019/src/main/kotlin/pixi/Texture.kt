package pixi

@JsName("Texture")
external class Texture {
	fun fromImage(imageUrl: String): Texture
	fun fromFrame(imageUrl: String): Texture
}