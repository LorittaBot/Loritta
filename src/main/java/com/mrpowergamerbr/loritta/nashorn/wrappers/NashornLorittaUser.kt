package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.dv8tion.jda.core.entities.Member

class NashornLorittaUser(private val backedMember: Member, private val serverConfig: ServerConfig) : NashornMember(backedMember) {}