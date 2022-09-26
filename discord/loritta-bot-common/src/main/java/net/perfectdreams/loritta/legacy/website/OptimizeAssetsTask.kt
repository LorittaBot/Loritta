package net.perfectdreams.loritta.legacy.website

class OptimizeAssetsTask : Runnable {
	override fun run() {
		OptimizeAssets.optimizeCss()
	}
}