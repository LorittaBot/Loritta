package net.perfectdreams.loritta.cinnamon.pudding.tables.servers

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.TextColumnType

object Giveaways : LongIdTable() {
	val guildId = long("guild").index()
	val textChannelId = long("channel")
	val messageId = long("message")

	val reason = text("reason")
	val description = text("description")
	val numberOfWinners = integer("number_of_winners")
	val reaction = text("reaction")
	val imageUrl = text("image_url").nullable()
	val thumbnailUrl = text("thumbnail_url").nullable()
	val color = text("color").nullable()
	val finishAt = long("finish_at")
	val customMessage = text("custom_message").nullable()
	val locale = text("locale")
	val roleIds = array<String>("roles", TextColumnType()).nullable()
	val allowedRoles = jsonb("allowed_roles").nullable()
	var deniedRoles = jsonb("denied_roles").nullable()
	var needsToGetDailyBeforeParticipating = bool("needs_to_get_daily_before_participating").nullable() // Can be nullable due to old giveaways
	var selfServerEmojiFightBetVictories = integer("self_server_emoji_fight_bet_victories").nullable()
	var selfServerEmojiFightBetLosses = integer("self_server_emoji_fight_bet_losses").nullable()
	var messagesRequired = integer("messages_required").nullable()
	var messagesTimeThreshold = long("messages_time_threshold").nullable()
	val extraEntriesShouldStack = bool("extra_entries_should_stack")
	val createdAt = timestampWithTimeZone("created_at").nullable() // Can be nullable due to old giveaways

	val finished = bool("finished").default(false).index()

	val version = integer("version").default(1)
}