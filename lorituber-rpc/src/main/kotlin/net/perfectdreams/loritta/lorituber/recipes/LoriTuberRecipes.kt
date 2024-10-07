package net.perfectdreams.loritta.lorituber.recipes

import net.perfectdreams.loritta.lorituber.items.LoriTuberItem
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import net.perfectdreams.loritta.lorituber.items.LoriTuberItems

object LoriTuberRecipes {
    private val recipes = mutableMapOf<String, LoriTuberRecipe>()
    val allRecipes
        get() = recipes.values

    val GESSY_CEREAL_WITH_MILK = register(
        "gessy_cereal_with_milk",
        LoriTuberItems.GESSY_CEREAL_WITH_MILK,
        listOf(LoriTuberItems.GESSY_CEREAL, LoriTuberItems.MILK),
        10
    )

    val GESSY_CEREAL_WITH_STRAWBERRY_YOGURT = register(
        "gessy_cereal_with_strawberry_yogurt",
        LoriTuberItems.GESSY_CEREAL_WITH_STRAWBERRY_YOGURT,
        listOf(LoriTuberItems.GESSY_CEREAL, LoriTuberItems.STRAWBERRY_YOGURT),
        10
    )

    fun register(
        id: String,
        targetItem: LoriTuberItem,
        requiredItems: List<LoriTuberItem>,
        ticks: Long
    ): LoriTuberRecipe {
        val itemWithNamespace = "lorituber:${id}"

        val recipe = LoriTuberRecipe(
            itemWithNamespace,
            targetItem.id,
            requiredItems.map { it.id },
            ticks
        )

        recipes[itemWithNamespace] = recipe
        return recipe
    }

    fun getMatchingRecipeForItems(items: List<LoriTuberItemId>): LoriTuberRecipe? {
        val selectedItemIds = items

        for (recipe in allRecipes.sortedBy { it.requiredItemIds.size }) {
            val hasAllRequiredItems = recipe.requiredItemIds.size == selectedItemIds.size && recipe.requiredItemIds.all { it in selectedItemIds }

            if (hasAllRequiredItems)
                return recipe
        }

        return null
    }

    fun getByIdOrNull(id: String): LoriTuberRecipe? = recipes[id]
    fun getById(id: String) = getByIdOrNull(id) ?: error("Unknown recipe \"$id\"!")
}