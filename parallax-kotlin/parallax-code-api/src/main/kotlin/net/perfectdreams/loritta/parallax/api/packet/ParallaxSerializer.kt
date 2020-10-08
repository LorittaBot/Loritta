package net.perfectdreams.loritta.parallax.api.packet

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

object ParallaxSerializer {
    val json = Json(
            context = SerializersModule {
                polymorphic(ParallaxPacket::class) {
                    ParallaxAckPacket::class with ParallaxAckPacket.serializer()

                    ParallaxSendMessagePacket::class with ParallaxSendMessagePacket.serializer()
                    ParallaxAckSendMessagePacket::class with ParallaxAckSendMessagePacket.serializer()

                    ParallaxPutRolePacket::class with ParallaxPutRolePacket.serializer()

                    ParallaxDeleteRolePacket::class with ParallaxDeleteRolePacket.serializer()

                    ParallaxLogPacket::class with ParallaxLogPacket.serializer()
                    ParallaxThrowablePacket::class with ParallaxThrowablePacket.serializer()
                }
            }
    )

    fun toJson(packet: ParallaxPacket, packetId: Long) = json.stringify(PacketWrapper.serializer(), PacketWrapper(packet, packetId))
}