@file:JsModule("svgedit-api")
package com.aimozg.psvg

import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
external open class Part {
	val id: Int
	val ctx: ModelContext
	val owner: Part
	val children: Array<Part>
	val category: String
	fun treeNodeFull():JSTreeNodeInit
	fun treeNodeId():String
	fun treeNodeText():String
	fun save():dynamic
}
external open class Value<T> : Part {
	fun get():T
	fun editorElement(): HTMLElement
}
external class ValueFloat : Value<Double> {
	fun set(value:Double)
}
external open class ModelElement : Part {
	val graphic: SVGElement?
}
external open class ModelPoint: Part {

}
external class FixedPoint : Part {
	val x:ValueFloat
	val y:ValueFloat
	fun set(x:Double,y:Double)
}
external class Model : ModelElement{
	fun clone(ctx: ModelContext): Model
	fun display(): SVGElement
	companion object {
		fun load(ctx: ModelContext,json:dynamic):Model
	}
}

external class ModelLoader {
	val cat:String
	val name:String
	val typename:String?
	val objtypes:List<String>
}