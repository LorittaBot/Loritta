package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KLogger
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.PutGuildRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.responses.PutGuildResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.DeviousGuildDataWrapper
import net.perfectdreams.loritta.deviouscache.server.utils.GuildAndUserPair
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey
import net.perfectdreams.loritta.deviouscache.server.utils.extensions.runIfDifferentAndNotNull

class PutGuildProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}

        fun processGuild(logger: KLogger, log: Boolean, m: DeviousCache, request: PutGuildRequest): Boolean {
            if (log)
                logger.info { "Updating guild with ID ${request.id}" }

            val cachedGuild = m.guilds[request.id]
            val isNewGuild = cachedGuild == null
            val wrapper = DeviousGuildDataWrapper(
                request.data,
                // TODO: This feels weird
                cachedGuild?.channelIds ?: request.channels?.map { it.id }?.toSet() ?: emptySet()
            )
            m.guilds[request.id] = wrapper
            m.dirtyGuilds.add(request.id)

            val currentEmotes = m.emotes[request.id]

            runIfDifferentAndNotNull(currentEmotes?.values, request.emojis) {
                m.emotes[request.id] = it.associateBy { it.id }
                m.dirtyEmojis.add(request.id)
            }

            val currentRoles = m.roles[request.id]
            runIfDifferentAndNotNull(currentRoles?.values, request.roles) {
                m.roles[request.id] = it.associateBy { it.id }
                m.dirtyRoles.add(request.id)
            }

            val members = request.members
            if (members != null) {
                val currentMembers = m.members[request.id]
                m.members[request.id] = (currentMembers ?: emptyMap())
                    .toMutableMap()
                    .also {
                        for ((id, member) in members) {
                            val currentMember = it[id]
                            if (currentMember != member) {
                                it[id] = member
                                m.dirtyMembers.add(GuildAndUserPair(request.id, id))
                            }
                        }
                    }
            }

            val channels = request.channels
            if (channels != null) {
                val oldChannelIds = cachedGuild?.channelIds?.toSet()

                // Remove removed channels from the global channel cache
                if (oldChannelIds != null) {
                    for (channelId in (channels.map { it.id } - oldChannelIds)) {
                        m.channels.remove(channelId)
                        m.dirtyChannels.add(channelId)
                    }
                }

                // Add all channels to the guild channel cache
                val channelMappedByIds = channels.associateBy { it.id }

                for ((channelId, data) in channelMappedByIds) {
                    val currentChannel = m.channels[channelId]
                    if (data != currentChannel) {
                        m.channels[channelId] = data
                        m.dirtyChannels.add(channelId)
                    }
                }
            }

            val currentVoiceStates = m.voiceStates[request.id]
            runIfDifferentAndNotNull(currentVoiceStates?.values, request.voiceStates) {
                m.voiceStates[request.id] = it.associateBy { it.userId }
                m.dirtyVoiceStates.add(request.id)
            }

            return isNewGuild
        }
    }

    suspend fun process(request: PutGuildRequest): DeviousResponse {
        m.withLock(GuildKey(request.id)) {
            m.awaitForEntityPersistenceModificationMutex()

            val isNewGuild = processGuild(logger, true, m, request)

            return PutGuildResponse(isNewGuild)
        }
    }
}