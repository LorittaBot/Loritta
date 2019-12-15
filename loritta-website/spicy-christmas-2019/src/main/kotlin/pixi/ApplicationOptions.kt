package pixi

class ApplicationOptions(
		val width: Int = 800,
		val height: Int = 600,
		val antialias: Boolean = false,
		val transparent: Boolean = false,
		val resolution: Int = 1,
		var legacy: Boolean = false
)