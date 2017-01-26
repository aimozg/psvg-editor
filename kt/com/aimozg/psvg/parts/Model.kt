package com.aimozg.psvg.parts

import com.aimozg.psvg.SVGGElement
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.set
import org.w3c.dom.svg.SVGGraphicsElement

class Model(ctx: ModelContext,
            name: String?,
            ownOrigin: ModelPoint?,
            val paths: List<ModelPath>,
            val params: List<ModelParam>) :
		ModelElement(ctx, name, ownOrigin, paths.map(ModelPath::asDependency)) {
	override val category: PartCategory = PartCategory.MODEL
	override fun save(): dynamic = jsobject {
		it.name = name
		it.paths = paths.map { it.save() }.toTypedArray()
		it.params = params.map { it.save() }.toTypedArray()
	}

	override fun updated(other: Part, attr: String) {}

	override fun draw() = SVGGElement {
		style["stroke"] = "transparent"
		style["fill"] = "transparent"
		style["stroke-opacity"] = "1"
		style["fill-opacity"] = "1"
		style["opacity"] = "1"
		style["stroke-width"] = "1"
		for (path in paths) append(path.graphic)
	}

	override fun redraw(attr: String, graphic: SVGGraphicsElement?) {
	}

	fun clone(ctx: ModelContext): Model = load(ctx, save()) // TODO optimize

	companion object {
		fun load(ctx: ModelContext, json: dynamic) = Model(ctx,
				json.name ?: "unnamed",
				null,
				(json["paths"] as Array<dynamic>?)?.map { ctx.loadPath(it) } ?: emptyList(),
				(json["params"] as Array<dynamic>?)?.map { ctx.loadParam(it) } ?: emptyList())
	}
}