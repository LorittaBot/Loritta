package net.perfectdreams.loritta.morenitta.banappeals

import net.perfectdreams.loritta.banappeals.BanAppealResult
import net.perfectdreams.loritta.serializable.UserBannedState
import java.time.OffsetDateTime

data class BanAppeal(
    val id: Long,
    val submittedBy: Long,
    val userId: Long,
    val whatDidYouDo: String,
    val whyDidYouBreakThem: String,
    val accountIds: List<Long>,
    val whyShouldYouBeUnbanned: String,
    val additionalComments: String,
    val files: List<String>,
    val banEntry: UserBannedState,
    val submittedAt: OffsetDateTime,
    val reviewedBy: Long?,
    val reviewedAt: OffsetDateTime?,
    val reviewerNotes: String?,
    val appealResult: BanAppealResult
)