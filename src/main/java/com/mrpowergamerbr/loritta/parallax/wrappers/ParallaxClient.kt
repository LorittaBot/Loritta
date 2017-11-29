package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.utils.LORITTA_SHARDS

class ParallaxClient {
	val user: ParallaxUser = ParallaxUser(LORITTA_SHARDS.shards[0].selfUser)
}