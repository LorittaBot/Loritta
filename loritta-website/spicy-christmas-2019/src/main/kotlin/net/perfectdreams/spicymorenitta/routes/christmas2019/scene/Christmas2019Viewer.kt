package net.perfectdreams.spicymorenitta.routes.christmas2019.scene

import net.perfectdreams.spicymorenitta.utils.Logging
import pixi.PIXI
import pixi.PixiApplication
import pixi.Sprite
import kotlin.browser.window

class Christmas2019Viewer(app: PixiApplication) : Scene(app), Logging {
	val backdropTexture = PIXI.Texture.fromImage("${window.location.origin}/assets/christmas2019/img/backdrop.png")

	init {
		debug("Starting Christmas Viewer...")

		this.addObject(Sprite(backdropTexture))
	}

	override fun tick(delta: Float) {
	}
}