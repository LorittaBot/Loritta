package net.perfectdreams.loritta.common.entities

class AllowedMentions(
    val users: Set<User>,
    val repliedUser: Boolean
)