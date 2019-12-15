package net.perfectdreams.spicymorenitta.routes.christmas2019

import net.perfectdreams.spicymorenitta.routes.christmas2019.scene.Christmas2019Viewer
import net.perfectdreams.spicymorenitta.routes.christmas2019.scene.Scene
import net.perfectdreams.spicymorenitta.utils.Logging
import pixi.ApplicationOptions
import pixi.PIXI
import pixi.PixiApplication
import kotlin.browser.document
import kotlin.browser.window

class Christmas2019App : Logging {
	companion object {
		val ticks: Int
			get() = totalDelta.toInt()
		var totalDelta = 0f
	}

	lateinit var app: PixiApplication;

	fun start() {
		PIXI.loader.add("${window.location.origin}/assets/christmas2019/img/backdrop.png")
				// .add("assets/snd/happy.mp3")
				.load { any, any2 ->
					info("Starting the cool christmas stuff :3")

					app = PixiApplication(
							ApplicationOptions(
							)
					)

					println(document.body)
					document.body?.append(app.view)

					app.renderer.backgroundColor = 0x8c1308

					app.stage = Christmas2019Viewer(app)

					app.ticker.add {
						totalDelta += it

						val stage = app.stage

						if (stage is Scene) {
							stage.tick(it)
						} else {
							println("Current stage doesn't extend Scene!")
						}
					}

					resize()
				}

	}

	fun resize() {
		app.renderer.resize(window.innerWidth, window.innerHeight)
	}
}