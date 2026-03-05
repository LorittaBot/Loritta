package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object DropChatChoiceParticipants : LongIdTable() {
    val userId = long("user_id").index()
    val dropChatChoice = reference("drop_chat_choice", DropChatChoices)
    val choice = text("choice")
    val won = bool("won")
}
