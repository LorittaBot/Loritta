package com.mrpowergamerbr.loritta.parallax.wrappers

import net.dv8tion.jda.core.entities.Member

class ParallaxMember(private val member: Member) : ParallaxUser(member.user) {

}