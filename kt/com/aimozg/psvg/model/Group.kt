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
            items: List<ModelElement>) : VisibleElement(ctx, name,
		items.map { it.asDependency(null) } + listOf(origin?.asPosDependency)) {
	override val category: Category get() = Category.GROUP

	companion object {
		private const val TYPE = "group"
		val GROUP_LOADER = object : PartLoader(Category.GROUP, Group::class, TYPE) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?): Group =
					Group(ctx, json.name,
							ctx.loadPoint(json.origin),
							(json.items as Array<*>).map { ctx.loadAnyPart(null, it) })
		}
	}

	override fun update(attr: Attribute) {
		super.update(attr)
	}

	override fun display() = SVGGElement {
		appendAll(this@Group.children.map { (it as? VisibleElement)?.export() })
		//translate(this)
	}

	override fun updated(other: ModelElement, attr: Attribute) {
		super.updated(other, attr)
		if (other == origin) update(Attribute.ALL)
	}

	private fun translate(g: SVGGraphicsElement?) {
		val pos = origin
		if (pos != null) g?.transform?.set(tftranslate(pos.calculate()))
	}

	override fun doRedraw(attr: Attribute, g: SVGGraphicsElement) {
		translate(g)
	}

	override fun save(): dynamic = jsobject {
		it.type = TYPE
		it.name = name
		it.origin = origin?.save()
		it.items = children.map { arrayOf(it.category.toString(), it.save()) }.toTypedArray()
	}
}