package net.perfectdreams.loritta.morenitta.lorittapartners

import net.perfectdreams.loritta.partnerapplications.PartnerApplicationResult
import java.time.OffsetDateTime

data class PartnerApplicationData(
    val id: Long,
    val guildId: Long,
    val inviteLink: String,
    val serverPurpose: String,
    val whyPartner: String,
    val result: PartnerApplicationResult,
    val reviewedBy: Long?,
    val reviewedAt: OffsetDateTime?,
    val reviewerNotes: String?
)