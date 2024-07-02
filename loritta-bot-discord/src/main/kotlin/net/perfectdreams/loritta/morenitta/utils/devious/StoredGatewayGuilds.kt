package net.perfectdreams.loritta.morenitta.utils.devious

import kotlinx.serialization.Serializable

/**
 * Used to store a list of ByteArrays on the disk using Protobuf
 */
@Serializable
class StoredGatewayGuilds(
    val guilds: List<ByteArray> = emptyList() // you have to explicitly add to any property of List type a default value equals to emptyList()
)