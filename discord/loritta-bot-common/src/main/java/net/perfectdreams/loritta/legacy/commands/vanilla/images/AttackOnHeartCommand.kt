package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class AttackOnHeartCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("attackonheart"),
	1,
	"commands.command.attackonheart.description",
	"/api/v1/videos/attack-on-heart",
	"attack_on_heart.mp4",
	category = CommandCategory.VIDEOS,
	slashCommandName = "attackonheart"
)