package net.perfectdreams.loritta.api.entities

interface Message {
	val author: User
	val content: String
}