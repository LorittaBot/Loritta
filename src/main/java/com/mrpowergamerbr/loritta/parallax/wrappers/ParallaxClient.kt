package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.utils.lorittaShards

class ParallaxClient {
	val user: ParallaxUser = ParallaxUser(lorittaShards.shards[0].selfUser)
}