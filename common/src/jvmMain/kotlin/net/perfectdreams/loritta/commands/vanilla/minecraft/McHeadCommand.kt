package net.perfectdreams.loritta.commands.vanilla.minecraft

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.commands.vanilla.minecraft.declarations.MinecraftCommandDeclaration

class McHeadCommand(m: LorittaBot) : CrafatarCommandBase(
    m,
    "renders/head",
    MinecraftCommandDeclaration.Head
)