package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.loritta.cinnamon.pudding.tables.SnowflakeTable

object LorittaDailyShopNotificationsConfigs : SnowflakeTable() {
    val notifyShopTrinkets = bool("notify_shop_trinkets").index()
    val shopTrinketsChannelId = long("shop_trinkets_channel").nullable()
    val shopTrinketsMessage = text("shop_trinkets_message").nullable()

    val notifyNewTrinkets = bool("notify_new_trinkets").index()
    val newTrinketsChannelId = long("new_trinkets_channel").nullable()
    val newTrinketsMessage = text("new_trinkets_message").nullable()
}