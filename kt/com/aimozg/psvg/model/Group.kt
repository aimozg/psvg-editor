package com.aimozg.psvg.model

import com.aimozg.psvg.*
import com.aimozg.psvg.model.point.Point
import org.w3c.dom.svg.SVGGraphicsElement

/**
 * Created by aimozg on 31.01.2017.
 * Confidential
 */
class Group(ctx: Context,
            name: String?,
            val origin: Point?,
            val items: List<ModelElement>) : VisibleElement(ctx, name, items.map { it.asDependency }+listOf(origin?.asPosDependency)) {
	override val category: Category get() = Category.GROUP

	companion object {
		private const val TYPE = "group"
		val GROUP_LOADER = object : PartLoader(Category.GROUP, Group::class, TYPE) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?): Group =
					Group(ctx, json.name, ctx.loadPoint(json.origin), (json.items as Array<*>).map { ctx.loadAnyPart(null, it) })
		}
	}

	override fun update(attr: String) {
		super.update(attr)
		translate(graphic)
	}

	override fun display() = SVGGElement {
		appendAll(items.map { (it as? VisibleElement)?.export() })
		translate(this)
	}

	override fun updated(other: ModelElement, attr: String) {
		if (other == origin) {
			translate(graphic)
			update("*")
		}
	}

	private fun translate(g: SVGGraphicsElement?) {
		val pos = origin
		if (pos != null) g?.transform?.set(tftranslate(pos.calculate()))
	}

	override fun redraw(attr: String, g: SVGGraphicsElement) {
		translate(g)
	}

	override fun save(): dynamic = jsobject {
		it.type = TYPE
		it.name = name
		it.origin = origin?.save()
		it.items = items.map { arrayOf(it.category.toString(),it.save()) }.toTypedArray()
	}
}