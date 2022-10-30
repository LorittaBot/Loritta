package net.perfectdreams.loritta.morenitta.website

class OptimizeAssetsTask : Runnable {
    override fun run() {
        OptimizeAssets.optimizeCss()
    }
}