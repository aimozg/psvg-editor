package com.aimozg.psvg.model

import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGraphicsElement

class Model(ctx: Context,
            name: String?,
            val items: List<ModelElement>) :
		VisibleElement(ctx, name, items.map { it.asDependency (null)}) {
	override val category: Category = Category.MODEL
	override fun save(): dynamic = jsobject {
		it.name = name
		it.items = jsobject { o->
			for (part in items) {
				val name = part.name ?: "#${part.id}"
				val save = part.save()
				val type = part.category.toString()
				if (save is Array<Any?> && save[0] === name) {
					val array:Array<Any?> = save
					o[name] = arrayOf(type) + array.sliceFrom(1)
				} else {
					o[name] = arrayOf(type,save)
				}
			}
		}
	}

	override fun updated(other: ModelElement, attr: String) {}

	override fun draw(g: SVGGraphicsElement) {
		g.appendAll(items.map{(it as? VisibleElement)?.graphic})
	}

	override fun display() = SVGGElement {
		style["stroke"] = "transparent"
		style["fill"] = "transparent"
		style["stroke-opacity"] = "1"
		style["fill-opacity"] = "1"
		style["opacity"] = "1"
		style["stroke-width"] = "1"
		appendAll(items.map{(it as? VisibleElement)?.export()})
	}

	override fun redraw(attr: String, g: SVGGraphicsElement) {
	}

	fun clone(ctx: Context): Model = ctx.loadModel(save()) // TODO optimize

}