package net.perfectdreams.loritta.lorituber.items

object LoriTuberItems {
    private val items = mutableMapOf<String, LoriTuberItem>()
    val allItems
        get() = items.values

    val SLICED_BREAD = register(
        "sliced_bread",
        // "Pão de Forma",
        // "Apenas um simples Pão de Forma",
        100,
        LoriTuberItem.FoodAttributes(
            3,
            20
        )
    )

    // TODO: Update the name + description
    val GESSY_CEREAL = register(
        "gessy_cereal",
        // "Cereal do Gessy",
        // "Apenas um simples Pão de Forma",
        100,
        LoriTuberItem.FoodAttributes(
            2,
            20
        )
    )

    // TODO: Update the name + description
    val MILK = register(
        "milk",
        // "Leite",
        // "Apenas um simples Pão de Forma",
        100,
        LoriTuberItem.FoodAttributes(
            2,
            20
        )
    )

    val STRAWBERRY_YOGURT = register(
        "strawberry_yogurt",
        // "Iogurte de Morango",
        // "Apenas um simples Iogurte delisia",
        150,
        LoriTuberItem.FoodAttributes(
            3,
            20
        )
    )

    // TODO: Update the name + description
    val GESSY_CEREAL_WITH_MILK = register(
        "gessy_cereal_with_milk",
        // "Cereal do Gessy com Leite",
        // "Cereal com leite delícia",
        100,
        LoriTuberItem.FoodAttributes(
            2,
            20
        )
    )

    val GESSY_CEREAL_WITH_STRAWBERRY_YOGURT = register(
        "gessy_cereal_with_strawberry_yogurt",
        // "Cereal do Gessy com Iogurte de Morango",
        // "Cereal com iogurte delícia",
        100,
        LoriTuberItem.FoodAttributes(
            2,
            20
        )
    )

    // TODO: Update the name + description
    val SLOP = register(
        "slop",
        // "Gororoba",
        // "Uma Gororoba",
        0,
        LoriTuberItem.FoodAttributes(
            2,
            100
        )
    )

    fun register(
        id: String,
        price: Long,
        foodAttributes: LoriTuberItem.FoodAttributes? = null
    ): LoriTuberItem {
        val itemWithNamespace = "lorituber:${id}"

        val item = LoriTuberItem(
            LoriTuberItemId(itemWithNamespace),
            price,
            foodAttributes
        )

        items[itemWithNamespace] = item
        return item
    }

    fun getByIdOrNull(id: String): LoriTuberItem? = items[id]
    fun getById(id: String) = getByIdOrNull(id) ?: error("Unknown item \"$id\"!")
    fun getById(ref: LoriTuberItemId) = getByIdOrNull(ref.id) ?: error("Unknown item \"$ref\"!")
}