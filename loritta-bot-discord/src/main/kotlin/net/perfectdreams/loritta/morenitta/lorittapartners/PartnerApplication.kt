package net.perfectdreams.loritta.morenitta.lorittapartners

import net.perfectdreams.loritta.partnerapplications.PartnerApplicationResult
import net.perfectdreams.loritta.partnerapplications.PartnerPermissionLevel
import java.time.OffsetDateTime

data class PartnerApplication(
    val id: Long,
    val submittedBy: Long,
    val guildId: Long,
    val languageId: String,
    val inviteLink: String,
    val serverPurpose: String,
    val whyPartner: String,
    val submittedAt: OffsetDateTime,
    val reviewedBy: Long?,
    val reviewedAt: OffsetDateTime?,
    val reviewerNotes: String?,
    val applicationResult: PartnerApplicationResult,
    val submitterPermissionLevel: PartnerPermissionLevel
)