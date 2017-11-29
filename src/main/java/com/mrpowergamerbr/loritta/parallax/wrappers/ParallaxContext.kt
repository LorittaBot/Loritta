package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.commands.CommandContext

class ParallaxContext(private val context: CommandContext) {
	private val rateLimiter = ParallaxRateLimiter()

	val message = ParallaxMessage(context.message)
	val guild = ParallaxGuild(context.guild)
	val member = ParallaxMember(context.handle)
}