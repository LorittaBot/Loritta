package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import net.dv8tion.jda.core.entities.Member

class NashornLorittaUser(private val backedMember: Member, private val serverConfig: MongoServerConfig) : NashornMember(backedMember) {}