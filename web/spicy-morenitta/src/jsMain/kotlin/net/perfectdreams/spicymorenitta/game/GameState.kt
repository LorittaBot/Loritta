package net.perfectdreams.spicymorenitta.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.util.date.*
import kotlinx.browser.document
import kotlinx.browser.window
import net.perfectdreams.loritta.serializable.PocketLorittaSettings
import net.perfectdreams.spicymorenitta.game.entities.Entity
import net.perfectdreams.spicymorenitta.game.entities.LorittaPlayer
import net.perfectdreams.spicymorenitta.game.entities.PlayerMovementState
import net.perfectdreams.spicymorenitta.game.render.RenderedEntity
import net.perfectdreams.spicymorenitta.game.render.RenderedLorittaPlayer
import net.perfectdreams.spicymorenitta.game.render.RenderedObject
import net.perfectdreams.spicymorenitta.utils.FontFace
import org.w3c.dom.*
import kotlin.random.Random

// Loritta shimeji-like overlay
// This variation...
// * Uses vanilla canvas elements, because pixi.js's bundle size is BIG as fucc
// * Removes the kotlinx.datetime call from the game loop to avoid including it on the bundle
// * Removes "eval" calls because for some reason that is increasing the bundle size a lot (why?)
class GameState {
    var canvas: HTMLCanvasElement? = null

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
    // val lorittaPetsContainer = Container()
    // val lorittaNametagsContainer = Container()

    var totalElapsedMS: Long = 0
    var oldTime = getTimeMillis()

    var elapsedTicks = 0

    var solidGround: List<FixedRectangle> = listOf()
    private val queries = listOf(
        "button",
        "input",
        ".user-info-wrapper",
        ".select-wrapper",
        ".shop-item-entry",
        ".discord-invite-wrapper",
        ".sonhos-bundle",
        "details"
    )

    val activityLevelState = mutableStateOf(ActivityLevel.MEDIUM)
    var activityLevel by activityLevelState
    val horizontalScaleState = mutableStateOf(1.0)
    var horizontalScale by horizontalScaleState
    val verticalScaleState = mutableStateOf(1.0)
    var verticalScale by verticalScaleState
    var addedToTheDOM = false
    var scaled = false

    fun start() {
        // DON'T USE EVAL!!!
        // it increases the bundle size (maybe DCE goes crazy?)
        val fontFace = FontFace("m5x7", "url(${window.location.origin}/lori-slippy/assets/css/m5x7.woff2)")

        fontFace.load()

        document.asDynamic().fonts.add(fontFace)

        // Process the game world on each render
        window.requestAnimationFrame { gameLoop() }
        ticks++
    }

    fun setCanvas(canvas: HTMLCanvasElement) {
        this.canvas = canvas
        updateCanvasSize()
    }

    fun updateCanvasSize() {
        val canvas = this.canvas ?: error("Canvas is not set!")
        canvas.width = (window.innerWidth * window.devicePixelRatio).toInt()
        canvas.height = (window.innerHeight * window.devicePixelRatio).toInt()
        // Updating the width/height resets the scale
        scaled = false
    }

    private fun gameLoop() {
        val newTime = getTimeMillis()
        if (document.asDynamic().visibilityState != "visible") {
            val newOldTime = getTimeMillis()
            println("Document is not focused, so we will set the oldTime to now and retry later (diff: ${newOldTime - oldTime}ms), to avoid using too much CPU processing the entities... - ${getTimeMillis()}")
            oldTime = newOldTime
            window.requestAnimationFrame { gameLoop() }
            return
        }

        if (!addedToTheDOM) {
            val newOldTime = getTimeMillis()
            println("Canvas is not added to the DOM (diff: ${newOldTime - oldTime}ms), to avoid using too much CPU processing the entities... - ${getTimeMillis()}")
            oldTime = newOldTime
            window.requestAnimationFrame { gameLoop() }
            return
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

            isGameLogicUpdate = true
            totalElapsedMS -= 50
            elapsedTicks++
        }

        renderGame(isGameLogicUpdate)

        totalElapsedMS += deltaMS
        window.requestAnimationFrame { gameLoop() }
    }

    private fun renderGame(isGameLogicUpdate: Boolean) {
        val canvas = this.canvas
        if (canvas == null) {
            println("Canvas target is not set, ignoring game render...")
            return
        }

        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        if (!scaled) {
            ctx.scale(window.devicePixelRatio, window.devicePixelRatio)
            scaled = true
        }

        ctx.clearRect(0.0, 0.0, width.toDouble(), height.toDouble())
        // No need to render dead entities
        entities.filter { !it.dead }.forEach {
            val playerRenderedEntity = renderedObjects.filterIsInstance<RenderedEntity<*>>().firstOrNull { re -> re.entity == it }

            if (playerRenderedEntity == null) {
                println("Creating rendered entity for ${it}")
                val re = when (it) {
                    is LorittaPlayer -> {
                        RenderedLorittaPlayer(
                            this,
                            random.nextInt(0, 100),
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

        // Entities should be rendered from top to bottom
        for (re in renderedObjects.sortedBy { it.zIndex }) {
            re.render(ctx, isGameLogicUpdate, totalElapsedMS, 0.02)
        }
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

    fun syncStateWithSettings(settings: PocketLorittaSettings) {
        // Sync entities
        syncPlayerTypeToCount(LorittaPlayer.PlayerType.LORITTA, settings.lorittaCount)
        syncPlayerTypeToCount(LorittaPlayer.PlayerType.PANTUFA, settings.pantufaCount)
        syncPlayerTypeToCount(LorittaPlayer.PlayerType.GABRIELA, settings.gabrielaCount)
    }

    private fun syncPlayerTypeToCount(type: LorittaPlayer.PlayerType, targetCount: Int) {
        // First, we will get all types of entities we want to match
        val lorittaPlayers = entities.filterIsInstance<LorittaPlayer>()
        val lorittaKindPlayers = lorittaPlayers.filter { it.playerType == type }
            .toMutableList()

        // Now we want to know... do we need to kill, spawn, or do nothing?
        val lorittaPlayerDiff = targetCount - lorittaKindPlayers.size
        println("Target count $targetCount for $type, diff: $lorittaPlayerDiff")
        if (lorittaPlayerDiff == 0) {
            // Do nothing
        } else if (lorittaPlayerDiff > 0) {
            repeat(lorittaPlayerDiff) {
                spawnPlayer(type)
            }
        } else {
            val positiveDiff = lorittaPlayerDiff * -1
            repeat(positiveDiff) {
                if (lorittaKindPlayers.isEmpty()) {
                    println("Trying to remove more (current index: $it, target was $positiveDiff $type entities that we don't have!")
                    return@repeat
                }

                lorittaKindPlayers.removeAt(0).remove()
            }
        }
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
        // These are in ticks
        val minElapsed: Int,
        val maxElapsed: Int
    ) {
        LOW(
            // 20s -> 45s
            400,
            900
        ),

        MEDIUM(
            // 0s -> 15s
            0, 300
        ),

        HIGH(
            // 0s -> 3s
            0, 60
        )
    }
}