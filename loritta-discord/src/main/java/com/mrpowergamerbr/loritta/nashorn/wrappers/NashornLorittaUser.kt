package com.mrpowergamerbr.loritta.nashorn.wrappers

import net.dv8tion.jda.api.entities.Member

class NashornLorittaUser(private val backedMember: Member) : NashornMember(backedMember)