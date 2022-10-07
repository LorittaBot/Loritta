package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordInviteWithMetadata
import net.perfectdreams.loritta.deviousfun.JDA

class Invite(val jda: JDA, val inviter: User?, val invite: DiscordInviteWithMetadata) {
    val code: String
        get() = invite.code
    val uses: Int
        get() = invite.uses
    val maxUses: Int
        get() = invite.maxUses
}