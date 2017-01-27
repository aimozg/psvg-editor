package com.aimozg.psvg.parts

import com.aimozg.psvg.SVGGElement
import com.aimozg.psvg.appendAll
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.set
import org.w3c.dom.svg.SVGGElement

class Model(ctx: Context,
            name: String?,
            ownOrigin: Point?,
            val paths: List<Path>,
            val parameters: List<Parameter>) :
		VisiblePart(ctx, name, ownOrigin, paths.map(Path::asDependency)) {
	override val category: Category = Category.MODEL
	override fun save(): dynamic = jsobject {
		it.name = name
		it.paths = paths.map { it.save() }.toTypedArray()
		it.params = parameters.map { it.save() }.toTypedArray()
	}

	override fun updated(other: Part, attr: String) {}

	override fun draw(g: SVGGElement) {
		/*g.style["stroke"] = "transparent"
		g.style["fill"] = "transparent"
		g.style["stroke-opacity"] = "1"
		g.style["fill-opacity"] = "1"
		g.style["opacity"] = "1"
		g.style["stroke-width"] = "1"*/
		g.appendAll(paths.map{it.graphic})
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