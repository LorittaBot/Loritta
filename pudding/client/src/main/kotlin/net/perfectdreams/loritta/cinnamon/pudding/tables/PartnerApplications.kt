package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.partnerapplications.PartnerApplicationResult
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object PartnerApplications : LongIdTable() {
    val submittedBy = long("submitted_by").index()
    val guildId = long("guild_id").index()

    val languageId = text("language_id")
    val inviteLink = text("invite_link")
    val serverPurpose = text("server_purpose")
    val whyPartner = text("why_partner")

    val submittedAt = timestampWithTimeZone("submitted_at").index()

    val reviewedBy = long("reviewed_by").index().nullable()
    val reviewedAt = timestampWithTimeZone("reviewed_at").index().nullable()
    val reviewerNotes = text("reviewer_notes").nullable()
    val applicationResult = enumerationByName<PartnerApplicationResult>("application_result", 64)
}
