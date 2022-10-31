package net.perfectdreams.loritta.deviouscache.server.processors

import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.processors.channels.DeleteChannelProcessor
import net.perfectdreams.loritta.deviouscache.server.processors.channels.GetChannelProcessor
import net.perfectdreams.loritta.deviouscache.server.processors.channels.PutChannelProcessor
import net.perfectdreams.loritta.deviouscache.server.processors.guilds.*
import net.perfectdreams.loritta.deviouscache.server.processors.misc.GetMiscellaneousDataProcessor
import net.perfectdreams.loritta.deviouscache.server.processors.misc.PutMiscellaneousDataProcessor
import net.perfectdreams.loritta.deviouscache.server.processors.users.GetUserProcessor
import net.perfectdreams.loritta.deviouscache.server.processors.users.PutUserProcessor

class Processors(val m: DeviousCache) {
    val getUserProcessor = GetUserProcessor(m)
    val putUserProcessor = PutUserProcessor(m)

    val getGuildProcessor = GetGuildProcessor(m)
    val getIfGuildExistsProcessor = GetIfGuildExistsProcessor(m)
    val getGuildWithEntitiesProcessor = GetGuildWithEntitiesProcessor(m)
    val putGuildProcessor = PutGuildProcessor(m)
    val putGuildsBulkProcessor = PutGuildsBulkProcessor(m)
    val deleteGuildProcessor = DeleteGuildProcessor(m)
    val putGuildMemberProcesor = PutGuildMemberProcessor(m)
    val deleteGuildMemberProcessor = DeleteGuildMemberProcessor(m)
    val getGuildCountProcessor = GetGuildCountProcessor(m)
    val getGuildIdsOfShardProcessor = GetGuildIdsOfShardProcessor(m)
    val getGuildMembersProcessor = GetGuildMembersProcessor(m)
    val getGuildMemberProcessor = GetGuildMemberProcessor(m)
    val getGuildMembersWithRolesProcessor = GetGuildMembersWithRolesProcessor(m)
    val getGuildBoostersProcessor = GetGuildBoostersProcessor(m)
    val getVoiceStateProcessor = GetVoiceStateProcessor(m)
    val putVoiceStateProcessor = PutVoiceStateProcessor(m)

    val putGuildRoleProcessor = PutGuildRoleProcessor(m)
    val deleteGuildRoleProcessor = DeleteGuildRoleProcessor(m)

    val putGuildEmojisProcessor = PutGuildEmojisProcessor(m)

    val getChannelProcessor = GetChannelProcessor(m)
    val putChannelProcessor = PutChannelProcessor(m)
    val deleteChannelProcessor = DeleteChannelProcessor(m)

    val getGatewaySessionProcessor = GetGatewaySessionProcessor(m)
    val putGatewaySessionProcessor = PutGatewaySessionProcessor(m)
    val putGatewaySequenceProcessor = PutGatewaySequenceProcessor(m)
    val lockConcurrentLoginProcessor = LockConcurrentLoginProcessor(m)
    val unlockConcurrentLoginProcessor = UnlockConcurrentLoginProcessor(m)

    val getMiscellaneousDataProcessor = GetMiscellaneousDataProcessor(m)
    val putMiscellaneousDataProcessor = PutMiscellaneousDataProcessor(m)
}