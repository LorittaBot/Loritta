package com.mrpowergamerbr.loritta.parallax.wrappers

class ParallaxRateLimiter {
	var limitCounter = 0

	fun addAndCheck() {
		limitCounter++
	}
}