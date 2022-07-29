package net.perfectdreams.loritta.cinnamon.microservices.interactionshttpcoordinator

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.loritta.cinnamon.microservices.interactionshttpcoordinator.config.InteractionsHttpCoordinatorConfig
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val fileConfig = ConfigFactory.parseFile(File("./interactions-http-coordinator.conf"))
    val config = Hocon.decodeFromConfig<InteractionsHttpCoordinatorConfig>(fileConfig)

    if (config.instances.isEmpty())
        error("No instances are configured!")

    val interactionsHttpCoordinator = InteractionsHttpCoordinator(config)
    interactionsHttpCoordinator.start()
}