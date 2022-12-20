package net.perfectdreams.loritta.lorituber.server.processors

class Processors {
    val createCharacterRequestProcessor = CreateCharacterRequestProcessor()
    val createChannelRequestProcessor = CreateChannelRequestProcessor()
    val getChannelByIdRequestProcessor = GetChannelByIdRequestProcessor()
    val getMailRequestProcessor = GetMailRequestProcessor()
    val acknowledgeMailRequestProcessor = AcknowledgeMailRequestProcessor()
    val startTaskRequestProcessor = StartTaskRequestProcessor()
    val cancelTaskRequestProcessor = CancelTaskRequestProcessor()
    val getCharacterStatusRequestProcessor = GetCharacterStatusRequestProcessor()
    val createPendingVideoRequestProcessor = CreatePendingVideoRequestProcessor()
    val getPendingVideosByChannelRequestProcessor = GetPendingVideosByChannelRequestProcessor()
}