package com.mrpowergamerbr.loritta.website

class OptimizeAssetsTask : Runnable {
	override fun run() {
		OptimizeAssets.optimizeCss()
	}
}