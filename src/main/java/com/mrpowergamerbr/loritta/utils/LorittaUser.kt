package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.userdata.ServerConfig

import lombok.Getter
import net.dv8tion.jda.core.entities.Member

/**
 * Um usuário que está comunicando com a Loritta
 */
class LorittaUser(val member: Member, val config: ServerConfig) {

    val asMention: String
        get() = getAsMention(false)

    fun getAsMention(addSpace: Boolean): String {
        return if (config.mentionOnCommandOutput) member.asMention + (if (addSpace) " " else "") else ""
    }
}