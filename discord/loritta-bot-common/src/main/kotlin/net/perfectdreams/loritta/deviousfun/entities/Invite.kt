package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordInviteWithMetadata
import net.perfectdreams.loritta.deviousfun.DeviousShard

class Invite(val deviousShard: DeviousShard, val inviter: User?, val invite: DiscordInviteWithMetadata) {
    val code: String
        get() = invite.code
    val uses: Int
        get() = invite.uses
    val maxUses: Int
        get() = invite.maxUses
}