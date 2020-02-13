package com.mrpowergamerbr.loritta.parallax.wrappers

import net.dv8tion.jda.api.entities.VoiceChannel

class ParallaxVoiceChannel(private val channel: VoiceChannel) {

    val name get() = channel.name
    val id get() = channel.id

    val bitrate get() = channel.bitrate
    val calculatedPosition get() = channel.position
    val full get() = channel.members.size >= channel.userLimit
    val members get() = channel.members.map { ParallaxMember(it) }

    val userLimit get() = channel.userLimit

    fun setName(name: String, reason: String? = null) {
        channel.manager.setName(name)
                .reason(reason)
                .queue()
    }

    fun setPosition(position: Int) {
        channel.manager.setPosition(position).queue()
    }

    fun setTopic(topic: String, reason: String? = null) {
        channel.manager.setTopic(topic)
                .reason(reason)
                .queue()
    }

    fun setUserLimit(userLimit: Int, reason: String? = null) {
        channel.manager.setUserLimit(userLimit)
                .reason(reason)
                .queue()
    }

    fun join() {
        channel.guild.audioManager.openAudioConnection(channel)
    }

    fun leave() {
        channel.guild.audioManager.closeAudioConnection()
    }
}