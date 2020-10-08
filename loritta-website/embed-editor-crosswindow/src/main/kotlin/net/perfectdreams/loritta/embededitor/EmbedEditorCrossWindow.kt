package net.perfectdreams.loritta.embededitor

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import net.perfectdreams.loritta.embededitor.data.crosswindow.CrossWindowPacket
import net.perfectdreams.loritta.embededitor.data.crosswindow.MessageSetupPacket
import net.perfectdreams.loritta.embededitor.data.crosswindow.ReadyPacket
import net.perfectdreams.loritta.embededitor.data.crosswindow.UpdatedMessagePacket

object EmbedEditorCrossWindow {
    val communicationJson = Json(
            context = SerializersModule {
                polymorphic(CrossWindowPacket::class) {
                    ReadyPacket::class with ReadyPacket.serializer()

                    MessageSetupPacket::class with MessageSetupPacket.serializer()
                    UpdatedMessagePacket::class with UpdatedMessagePacket.serializer()
                }
            }
    )
}