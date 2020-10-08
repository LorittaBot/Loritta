package net.perfectdreams.loritta.embededitor

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import net.perfectdreams.loritta.embededitor.data.crosswindow.CrossWindowPacket
import net.perfectdreams.loritta.embededitor.data.crosswindow.MessageSetupPacket
import net.perfectdreams.loritta.embededitor.data.crosswindow.ReadyPacket
import net.perfectdreams.loritta.embededitor.data.crosswindow.UpdatedMessagePacket

object EmbedEditorCrossWindow {
    val communicationJson = Json {
        serializersModule = SerializersModule {
            polymorphic(CrossWindowPacket::class) {
                subclass(ReadyPacket::class, ReadyPacket.serializer())
                subclass(MessageSetupPacket::class, MessageSetupPacket.serializer())
                subclass(UpdatedMessagePacket::class, UpdatedMessagePacket.serializer())
            }
        }
    }
}