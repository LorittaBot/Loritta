package net.perfectdreams.spicymorenitta.routes.christmas2019.scene

import pixi.Container
import pixi.DisplayObject
import pixi.PixiApplication

abstract class Scene(val app: PixiApplication) : Container() {
	val objects = mutableListOf<DisplayObject>()

	abstract fun tick(delta: Float)

	fun addChild(obj: DisplayObject) {
		throw RuntimeException("Please use addObject!")
	}

	fun removeChild(obj: DisplayObject) {
		throw RuntimeException("Please use removeObject!")
	}

	fun addObject(obj: DisplayObject) {
		objects.add(obj)
		updateLayersOrder()
	}

	fun removeObject(obj: DisplayObject) {
		objects.remove(obj)
		// println("Removing $obj (${obj::class.simpleName}) @ ${this::class.simpleName}")
		obj.destroy()
		updateLayersOrder()
	}

	fun updateLayersOrder() {
		/* objects.sortBy {
			if (it is ZIndexed)
				it.index()
			else
				-1
		} */
		removeChildren()
		objects.forEach {
			super.addChild(it)
		}
	}
}