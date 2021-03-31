package net.perfectdreams.loritta.commands.vanilla.minecraft

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.commands.vanilla.minecraft.declarations.MinecraftCommandDeclaration

class McBodyCommand(m: LorittaBot) : CrafatarCommandBase(
    m,
    "renders/body",
    MinecraftCommandDeclaration.Body
)