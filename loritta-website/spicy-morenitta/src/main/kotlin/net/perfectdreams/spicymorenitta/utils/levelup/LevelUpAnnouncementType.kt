package net.perfectdreams.spicymorenitta.utils.levelup

import kotlinx.serialization.Serializable

@Serializable
enum class LevelUpAnnouncementType {
	DISABLED,
	SAME_CHANNEL,
	DIRECT_MESSAGE,
	DIFFERENT_CHANNEL
}