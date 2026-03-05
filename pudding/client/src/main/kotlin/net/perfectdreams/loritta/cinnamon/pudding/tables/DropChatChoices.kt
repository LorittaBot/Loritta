package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object DropChatChoices : LongIdTable() {
    val guildId = long("guild_id").index()
    val channelId = long("channel_id").index()
    val messageId = long("message_id").nullable().index()
    val startedById = long("started_by_id").index()
    val moneySourceId = long("money_source_id").nullable().index()
    val startedAt = timestampWithTimeZone("started_at").index()
    val endedAt = timestampWithTimeZone("ended_at").index()
    val participantPayout = long("participant_payout")
    val maxParticipants = integer("max_participants").nullable()
    val participants = integer("participants")
    val winners = integer("winners")
    val choices = jsonb("choices")
    val correctChoice = text("correct_choice")
    val variant = enumerationByName<DropChatChoiceVariantType>("variant", 32)

    enum class DropChatChoiceVariantType {
        GENERIC,
        EVEN_ODD,
        JANKENPON
    }
}
