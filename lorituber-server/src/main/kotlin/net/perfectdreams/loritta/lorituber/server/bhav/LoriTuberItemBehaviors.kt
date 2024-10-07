package net.perfectdreams.loritta.lorituber.server.bhav

import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import net.perfectdreams.loritta.lorituber.items.LoriTuberItems
import net.perfectdreams.loritta.lorituber.server.bhav.behaviors.*

object LoriTuberItemBehaviors {
    val itemToBehaviors = mapOf<LoriTuberItemId, ItemBehavior>(
        LoriTuberItems.SLICED_BREAD.id to FoodBehavior.GenericFood,
        LoriTuberItems.GESSY_CEREAL.id to FoodBehavior.GenericFood,
        LoriTuberItems.MILK.id to FoodBehavior.GenericFood,
        LoriTuberItems.STRAWBERRY_YOGURT.id to FoodBehavior.GenericFood,
        LoriTuberItems.GESSY_CEREAL_WITH_MILK.id to FoodBehavior.GenericFood,
        LoriTuberItems.GESSY_CEREAL_WITH_STRAWBERRY_YOGURT.id to FoodBehavior.GenericFood,
        LoriTuberItems.SLOP.id to FoodBehavior.GenericFood,

        LoriTuberItems.COMPUTER.id to ComputerBehavior.BasicComputer,
        LoriTuberItems.PHONE.id to PhoneBehavior.BasicPhone,
        LoriTuberItems.CHEAP_TOILET.id to ToiletBehavior.CheapToilet,
        LoriTuberItems.CHEAP_SHOWER.id to ShowerBehavior.CheapShower,
        LoriTuberItems.CHEAP_BED.id to BedBehavior.CheapBed,
        LoriTuberItems.CHEAP_FRIDGE.id to FridgeBehavior.CheapFridge,

        LoriTuberItems.ITEM_STORE.id to ItemStoreBehavior,
        LoriTuberItems.CHARACTER_PORTAL.id to CharacterPortalBehavior,
        LoriTuberItems.BEACH_OCEAN.id to BeachOceanBehavior,
        LoriTuberItems.DEBUG_MODE.id to DebugModeBehavior
    )
}