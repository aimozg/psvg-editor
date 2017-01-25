@file:JsModule("svgedit-api")
package com.aimozg.psvg

import org.w3c.dom.svg.SVGElement

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */

external interface Model {
	fun clone(ctx: ModelContext): Model
	fun display(): SVGElement
}

