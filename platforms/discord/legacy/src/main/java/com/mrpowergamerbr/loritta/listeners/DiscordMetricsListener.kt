package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import net.dv8tion.jda.api.events.*
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.utils.metrics.Prometheus

/**
 * Used to track Discord events to Prometheus
 */
class DiscordMetricsListener(val loritta: Loritta) : ListenerAdapter() {
    override fun onStatusChange(event: StatusChangeEvent) {
        Prometheus.SHARD_STATUS.labels(event.jda.shardInfo.shardId.toString()).set(event.newStatus.ordinal.toDouble())
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        Prometheus.GUILD_COUNT.labels(event.jda.shardInfo.shardId.toString()).set(event.jda.guildCache.size().toDouble())
        Prometheus.USER_COUNT.labels(event.jda.shardInfo.shardId.toString()).set(event.jda.userCache.size().toDouble())
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        Prometheus.GUILD_COUNT.labels(event.jda.shardInfo.shardId.toString()).set(event.jda.guildCache.size().toDouble())
        Prometheus.USER_COUNT.labels(event.jda.shardInfo.shardId.toString()).set(event.jda.userCache.size().toDouble())
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        Prometheus.GUILD_COUNT.labels(event.jda.shardInfo.shardId.toString()).set(event.jda.guildCache.size().toDouble())
        Prometheus.USER_COUNT.labels(event.jda.shardInfo.shardId.toString()).set(event.jda.userCache.size().toDouble())
    }

    override fun onGenericEvent(event: GenericEvent) {
        Prometheus.RECEIVED_JDA_EVENTS.labels(event.jda.shardInfo.shardId.toString()).inc()
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        Prometheus.RECEIVED_GUILD_MESSAGES.labels(event.jda.shardInfo.shardId.toString()).inc()
    }

    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        Prometheus.RECEIVED_PRIVATE_MESSAGES.inc()
    }

    override fun onGatewayPing(event: GatewayPingEvent) {
        Prometheus.GATEWAY_PING.labels(event.jda.shardInfo.shardId.toString()).set(event.newPing.toDouble())
    }

    override fun onReady(event: ReadyEvent) {
        Prometheus.SHARD_EVENTS.labels(event.jda.shardInfo.shardId.toString(), "ready")
                .inc()
    }

    override fun onReconnect(event: ReconnectedEvent) {
        Prometheus.SHARD_EVENTS.labels(event.jda.shardInfo.shardId.toString(), "reconnect")
                .inc()
    }

    override fun onResume(event: ResumedEvent) {
        Prometheus.SHARD_EVENTS.labels(event.jda.shardInfo.shardId.toString(), "resume")
                .inc()
    }

    override fun onDisconnect(event: DisconnectEvent) {
        Prometheus.SHARD_EVENTS.labels(event.jda.shardInfo.shardId.toString(), "disconnect")
                .inc()
    }

    override fun onShutdown(event: ShutdownEvent) {
        Prometheus.SHARD_EVENTS.labels(event.jda.shardInfo.shardId.toString(), "shutdown")
                .inc()
    }
}