package net.perfectdreams.loritta.dao.servers.moduleconfigs

import com.mrpowergamerbr.loritta.utils.counter.CounterUtils
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.tables.servers.moduleconfigs.MemberCounterChannelConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class MemberCounterChannelConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, MemberCounterChannelConfig>(MemberCounterChannelConfigs)

	var guild by MemberCounterChannelConfigs.guild
	var channelId by MemberCounterChannelConfigs.channelId
	var padding by MemberCounterChannelConfigs.padding
	var theme by MemberCounterChannelConfigs.theme
	var topic by MemberCounterChannelConfigs.topic

	fun getFormattedTopic(guild: Guild): String {
		val emojis = CounterUtils.getEmojis(theme)

		return topic.replace("{guildsize}", guild.memberCount.toString())
				.replace("{guild-size}", guild.memberCount.toString())
				.replace("{counter}", CounterUtils.generatePrettyCounter(guild.memberCount, emojis, padding))
	}
}