package net.perfectdreams.loritta.cinnamon.dashboard.frontend.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.util.date.*
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.datetime.Clock
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.entities.Entity
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.entities.LorittaPlayer
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.entities.PlayerMovementState
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.render.RenderedEntity
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.render.RenderedLorittaPlayer
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.render.RenderedObject
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.Application
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.Container
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import kotlin.random.Random

class GameState(
    val app: Application
) {
    // Inspired by: https://www.twitch.tv/fiotebeardev
    var ticks = 0
    val entities = mutableListOf<Entity>()
    val random = Random(getTimeMillis())
    // We don't use the app's width/height because that's not the *real* width/height of the screen, if the pixel resolution is not 1
    // Can't be a fixed value because the screen can resize!
    val width get() = window.innerWidth
    val height get() = window.innerHeight
    val groundY: Int
        get() = height

    val renderedObjects = mutableListOf<RenderedObject>()
    // Separated to keep them in different z-indexes (the nametags should always be on top)
    val lorittaPetsContainer = Container()
    val lorittaNametagsContainer = Container()
    val textures = GameTextures()

    var totalElapsedMS: Long = 0
    var oldTime = getTimeMillis()

    var elapsedTicks = 0

    var solidGround: List<FixedRectangle> = listOf()
    private val queries = listOf(
        "button",
        "input",
        ".user-info-wrapper",
        ".select-wrapper"
    )

    val activityLevelState = mutableStateOf(ActivityLevel.MEDIUM)
    var activityLevel by activityLevelState
    val horizontalScaleState = mutableStateOf(1.0)
    var horizontalScale by horizontalScaleState
    val verticalScaleState = mutableStateOf(1.0)
    var verticalScale by verticalScaleState

    init {
        // We need to manually add the font to the document body, if else, the font won't be loaded, because the font is only loaded if it is
        // being used in a div
        val fontFace = eval(
            """
                new FontFace(
                  "m5x7",
                  "url(${window.location.origin}/assets/css/m5x7.woff2)"
                );
            """.trimIndent()
        )

        fontFace.load()

        document.asDynamic().fonts.add(fontFace)
        // After this, the game ticker will check if the font is loaded and, if it is, it will render the players
        // Kinda hacky, could be better, but it does work

        app.stage.addChild(lorittaPetsContainer)
        app.stage.addChild(lorittaNametagsContainer)

        // Spawn how many players the user had
        for (type in LorittaPlayer.PlayerType.values()) {
            val count = localStorage.getItem("dashboard.pocketLoritta.${type.name.lowercase()}SpawnQuantity")?.toIntOrNull()

            if (count != null) {
                repeat(count) {
                    spawnPlayer(type)
                }
            }
        }

        // Get the activity level
        val activityLevelAsString = localStorage.getItem("dashboard.pocketLoritta.activityLevel")
        if (activityLevelAsString != null) {
            activityLevel = ActivityLevel.valueOf(activityLevelAsString)
        }

        // Get the size
        val xScale = localStorage.getItem("dashboard.pocketLoritta.xScale")
        if (xScale != null) {
            horizontalScale = xScale.toDouble()
        }
        val yScale = localStorage.getItem("dashboard.pocketLoritta.yScale")
        if (yScale != null) {
            verticalScale = yScale.toDouble()
        }

        // Process the game world on each render
        app.ticker.add { delta ->
            val newTime = getTimeMillis()
            if (document.asDynamic().visibilityState != "visible") {
                val newOldTime = getTimeMillis()
                println("Document is not focused, so we will set the oldTime to now and retry later (diff: ${newOldTime - oldTime}ms), to avoid using too much CPU processing the entities... - ${Clock.System.now()}")
                oldTime = newOldTime
                return@add
            }

            // println("oldTime: ${oldTime} x newTime: $newTime")
            val deltaMS = newTime - oldTime
            oldTime = newTime

            var isGameLogicUpdate = false

            // println("Elapsed: $deltaMS - Delta: $delta")

            while (totalElapsedMS >= 50) { // game world will be updated every 50ms (20 ticks per second)
                // We will only run the collision recalculation code if there's an entity alive
                if (entities.isNotEmpty()) {
                    // println("Running game logic... Total Elapsed MS: $totalElapsedMS")
                    // Remove dead entities
                    entities.removeAll { it.dead }

                    // Because the elements are dynamic (it changes between pages, they aren't fixed, etc), we will create the fixed rectangles manually
                    // on every game tick
                    solidGround = buildList {
                        // Ground
                        add(FixedRectangle(0, groundY, width, 1000))

                        // Buttons
                        queries.flatMap { document.querySelectorAll(it).asList() }
                            .filterIsInstance<HTMLElement>()
                            .forEach {
                                // Ever since we changed the CSS to NOT use sticky, we don't need to check these anymore, because the bounding rect
                                // now returns the correct value
                                // val scrollLeft = document.documentElement!!.scrollLeft
                                // val scrollTop = document.documentElement!!.scrollTop

                                val boundingRect = it.getBoundingClientRect()

                                // val x = (scrollLeft + boundingRect.left).toInt()
                                // val y = (scrollTop + boundingRect.top).toInt()
                                val x = boundingRect.left.toInt()
                                val y = boundingRect.top.toInt()
                                val width = boundingRect.width.toInt()
                                val height = boundingRect.height.toInt()

                                add(FixedRectangle(x, y, width, height))
                            }
                    }

                    // Update dynamic rectangles
                    // solidGround.filterIsInstance<ElementBoundingBoxRectangle>().forEach { it.updateCachedCoordinates() }

                    // Tick entities
                    entities.forEach {
                        it.tick()
                    }
                }

                saveSpawnedEntities()

                isGameLogicUpdate = true
                totalElapsedMS -= 50
                elapsedTicks++
            }

            val isFontLoaded = document.asDynamic().fonts.check("24px m5x7") as Boolean

            // Very hacky solution to not render any players until the font is actually loaded
            if (isFontLoaded) {
                entities.forEach {
                    val playerRenderedEntity =
                        renderedObjects.filterIsInstance<RenderedEntity<*>>().firstOrNull { re -> re.entity == it }

                    if (playerRenderedEntity == null) {
                        println("Creating rendered entity for ${it}")
                        val re = when (it) {
                            is LorittaPlayer -> {
                                RenderedLorittaPlayer(
                                    this,
                                    textures,
                                    random.nextInt(0, 100),
                                    lorittaPetsContainer,
                                    lorittaNametagsContainer,
                                    it
                                )
                            }

                            else -> {
                                error("Unsupported entity ${it::class}")
                            }
                        }

                        renderedObjects.add(re)
                    }
                }

                val renderedEntitiesThatDoNotExist = renderedObjects.filter { it.shouldBeRemoved }
                for (entity in renderedEntitiesThatDoNotExist) {
                    entity.destroy()
                }
                renderedObjects.removeAll(renderedEntitiesThatDoNotExist)

                // println("Objects to be rendered: ${renderedObjects.size} - Removed objects from rendering: ${renderedEntitiesThatDoNotExist.size}")

                for (re in renderedObjects) {
                    re.render(isGameLogicUpdate, totalElapsedMS, 0.02)
                }
            }

            totalElapsedMS += deltaMS
        }

        ticks++
    }

    fun spawnPlayer(type: LorittaPlayer.PlayerType) {
        val fromX = 40
        // I don't think someone has a screen with less than 40 pixels (unless if they are doing it on purpose)
        // But let's handle it anyway
        var toX = width - 39
        if (40 >= toX)
            toX = 41

        val player = LorittaPlayer(
            this,
            type.longName,
            random.nextInt(fromX, toX), // Spawn the player in a different x coordinate every time
            0,
            type
        )
        entities.add(player)
    }

    fun isGround(x: Int, y: Int): Rectangle? {
        for (ground in solidGround) {
            if (x in ground.x..(ground.x + ground.width) && y in ground.y..(ground.y + ground.width)) {
                return ground
            }
        }
        return null
    }

    fun isMultiGround(x: Int, y: Int): List<Rectangle> = solidGround.filter { ground ->
        x in ground.x..(ground.x + ground.width) && y in ground.y..(ground.y + ground.width)
    }

    fun isCollidingOnIdleState(player: LorittaPlayer, other: LorittaPlayer) = player.movementState is PlayerMovementState.IdleState && other.movementState is PlayerMovementState.IdleState && player.x in other.x..(other.x + other.width) && player.y in other.y..(other.y + other.width)

    private fun saveSpawnedEntities() {
        for (type in LorittaPlayer.PlayerType.values()) {
            val count = entities.filterIsInstance<LorittaPlayer>().count { it.playerType == type }.toString()
            localStorage.setItem("dashboard.pocketLoritta.${type.name.lowercase()}SpawnQuantity", count)
        }
        localStorage.setItem("dashboard.pocketLoritta.activityLevel", activityLevel.name)
        localStorage.setItem("dashboard.pocketLoritta.xScale", horizontalScale.toString())
        localStorage.setItem("dashboard.pocketLoritta.yScale", verticalScale.toString())
    }

    sealed class Rectangle {
        abstract val x: Int
        abstract val y: Int
        abstract val width: Int
        abstract val height: Int
    }

    data class FixedRectangle(
        override val x: Int,
        override val y: Int,
        override val width: Int,
        override val height: Int
    ) : Rectangle()

    data class ElementBoundingBoxRectangle(
        val element: HTMLDivElement
    ) : Rectangle() {
        override var x = 0
        override var y = 0
        override var width = 0
        override var height = 0

        fun updateCachedCoordinates() {
            val scrollLeft = document.documentElement!!.scrollLeft
            val scrollTop = document.documentElement!!.scrollTop

            val boundingRect = element.getBoundingClientRect()

            x = (scrollLeft + boundingRect.left).toInt()
            y = (scrollTop + boundingRect.top).toInt()
            width = boundingRect.width.toInt()
            height = boundingRect.height.toInt()
        }
    }

    enum class ActivityLevel(
        val title: StringI18nData,

        // These are in ticks
        val minElapsed: Int,
        val maxElapsed: Int
    ) {
        LOW(
            I18nKeysData.Website.Dashboard.LorittaSpawner.ActivityLevel.Types.Low,

            // 20s -> 45s
            400,
            900
        ),

        MEDIUM(
            I18nKeysData.Website.Dashboard.LorittaSpawner.ActivityLevel.Types.Medium,

            // 0s -> 15s
            0, 300
        ),

        HIGH(
            I18nKeysData.Website.Dashboard.LorittaSpawner.ActivityLevel.Types.High,

            // 0s -> 3s
            0, 60
        )
    }
}