package net.perfectdreams.loritta.parallax.api

import java.lang.RuntimeException

class ParallaxCommandException(message: String, prefix: String? = null) : RuntimeException(message)