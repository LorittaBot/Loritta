package net.perfectdreams.loritta.parallax.api.packet

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

object ParallaxSerializer {
    val json = Json(
            context = SerializersModule {
                polymorphic(ParallaxPacket::class) {
                    ParallaxSendMessagePacket::class with ParallaxSendMessagePacket.serializer()
                    ParallaxLogPacket::class with ParallaxLogPacket.serializer()
                    ParallaxAckPacket::class with ParallaxAckPacket.serializer()
                    ParallaxAckSendMessagePacket::class with ParallaxAckSendMessagePacket.serializer()
                }
            }
    )

    fun toJson(packet: ParallaxPacket, packetId: Long) = json.stringify(PacketWrapper.serializer(), PacketWrapper(packet, packetId))
}