package com.aimozg.psvg.model

import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGElement

class Model(ctx: Context,
            name: String?,
            ownOrigin: Point?,
            val store: List<ModelElement>,
            val paths: List<AbstractPath>,
            val parameters: List<Parameter>) :
		VisibleElement(ctx, name, ownOrigin, paths.map { it.asDependency } +store.map { it.asDependency (null)}) {
	override val category: Category = Category.MODEL
	override fun save(): dynamic = jsobject {
		it.name = name
		it.paths = paths.map { it.save() }.toTypedArray()
		it.params = parameters.map { it.save() }.toTypedArray()
		it.store = jsobject { o->
			for (part in store) {
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

	override fun draw(g: SVGGElement) {
		/*g.style["stroke"] = "transparent"
		g.style["fill"] = "transparent"
		g.style["stroke-opacity"] = "1"
		g.style["fill-opacity"] = "1"
		g.style["opacity"] = "1"
		g.style["stroke-width"] = "1"*/
		g.appendAll(paths.map{it.graphic})
		g.appendAll(store.map{(it as? VisibleElement)?.graphic})
	}

	override fun display() = SVGGElement {
		style["stroke"] = "transparent"
		style["fill"] = "transparent"
		style["stroke-opacity"] = "1"
		style["fill-opacity"] = "1"
		style["opacity"] = "1"
		style["stroke-width"] = "1"
		appendAll(paths.map{it.display()})
	}

	override fun redraw(attr: String, g: SVGGElement) {
	}

	fun clone(ctx: Context): Model = ctx.loadModel(save()) // TODO optimize

}