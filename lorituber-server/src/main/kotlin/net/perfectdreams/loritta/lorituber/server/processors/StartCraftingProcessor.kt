package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.recipes.LoriTuberRecipes
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.rpc.packets.StartCraftingRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.StartCraftingResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class StartCraftingProcessor(val m: LoriTuberServer) : PacketProcessor<StartCraftingRequest> {
    override suspend fun process(request: StartCraftingRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        val matchedRecipe = LoriTuberRecipes.getMatchingRecipeForItems(request.items)

        character.setTask(
            LoriTuberTask.PreparingFood(
                matchedRecipe?.id,
                request.items,
                m.gameState.worldInfo.currentTick
            )
        )

        return StartCraftingResponse
    }
}