@file:JsModule("svgedit-ctx")
package com.aimozg.psvg

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
typealias DisplayMode = String
external class ModelContext(mode: DisplayMode) {
	var onUpdate: (part:Part) -> Unit
	val parts: Array<Part?>
	companion object {
		fun loadersFor(category:String):Array<ModelLoader>
	}
}
