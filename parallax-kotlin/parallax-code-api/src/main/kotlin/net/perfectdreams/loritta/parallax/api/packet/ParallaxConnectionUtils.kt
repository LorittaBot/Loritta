package net.perfectdreams.loritta.parallax.api.packet

object ParallaxConnectionUtils {
    private var currentPacketId = 0L

    fun <T> sendPacket(packet: ParallaxPacket): T {
        println(ParallaxSerializer.toJson(packet, currentPacketId++))

        val line = readLine()!!
        val receivedPacket = ParallaxSerializer.json.parse(PacketWrapper.serializer(), line)

        return receivedPacket.m as T
    }
}