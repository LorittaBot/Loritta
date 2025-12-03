package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.banappeals.BanAppealResult
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object BanAppeals : LongIdTable() {
    val submittedBy = long("submitted_by").index()
    val userId = long("user").index()

    val languageId = text("language_id")
    val whatDidYouDo = text("what_did_you_do")
    val whyDidYouBreakThem = text("why_did_you_break_them")
    val accountIds = array<Long>("account_ids")
    val whyShouldYouBeUnbanned = text("why_should_you_be_unbanned")
    val additionalComments = text("additional_comments")
    val files = array<String>("files")
    val submittedAt = timestampWithTimeZone("submitted_at").index()
    val banEntry = reference("ban_entry", BannedUsers)

    val reviewedBy = long("reviewed_by").index().nullable()
    val reviewedAt = timestampWithTimeZone("reviewed_at").index().nullable()
    val reviewerNotes = text("reviewer_notes").nullable()
    val appealResult = enumerationByName<BanAppealResult>("appeal_result", 64)
}