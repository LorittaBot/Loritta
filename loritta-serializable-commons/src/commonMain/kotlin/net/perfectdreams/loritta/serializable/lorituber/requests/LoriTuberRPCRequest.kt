package net.perfectdreams.loritta.serializable.lorituber.requests

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberContentGenre
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberContentType
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask

@Serializable
sealed class LoriTuberRPCRequest

// ===[ CHARACTERS ]===
@Serializable
class GetCharactersByOwnerRequest(val ownerId: Long) : LoriTuberRPCRequest()

@Serializable
class GetCharacterStatusRequest(val characterId: Long) : LoriTuberRPCRequest()

@Serializable
class CreateCharacterRequest(
    val ownerId: Long,
    val name: String
) : LoriTuberRPCRequest()

@Serializable
class GetMailRequest(val characterId: Long) : LoriTuberRPCRequest()

@Serializable
class AcknowledgeMailRequest(val mailId: Long) : LoriTuberRPCRequest()

@Serializable
class StartTaskRequest(val characterId: Long, val task: LoriTuberTask) : LoriTuberRPCRequest()

@Serializable
class CancelTaskRequest(val characterId: Long) : LoriTuberRPCRequest()

@Serializable
class CreatePendingVideoRequest(val characterId: Long, val channelId: Long, val contentGenre: LoriTuberContentGenre, val contentType: LoriTuberContentType) : LoriTuberRPCRequest()

// ===[ CHANNELS ]===
@Serializable
class GetChannelsByCharacterRequest(val characterId: Long) : LoriTuberRPCRequest()

@Serializable
class GetChannelByIdRequest(val channelId: Long) : LoriTuberRPCRequest()

@Serializable
class CreateChannelRequest(
    val characterId: Long,
    val name: String
) : LoriTuberRPCRequest()

// ===[ MISC ]===
@Serializable
class GetServerInfoRequest : LoriTuberRPCRequest()