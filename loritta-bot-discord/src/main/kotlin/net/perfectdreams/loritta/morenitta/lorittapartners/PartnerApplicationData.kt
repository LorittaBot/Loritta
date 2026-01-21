package net.perfectdreams.loritta.morenitta.lorittapartners

import net.perfectdreams.loritta.partnerapplications.PartnerApplicationResult

data class PartnerApplicationData(
    val id: Long,
    val guildId: Long,
    val inviteLink: String,
    val serverPurpose: String,
    val whyPartner: String,
    val result: PartnerApplicationResult
)