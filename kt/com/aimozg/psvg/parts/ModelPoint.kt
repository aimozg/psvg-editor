package com.aimozg.psvg.parts

import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGUseElement

abstract class ModelPoint(ctx: ModelContext,
                          name: String?,
                          items: List<ItemDeclaration?>) :
		ModelElement(ctx, name, null, items) {
	private var use: SVGUseElement? = null
	override val category: PartCategory = PartCategory.POINT
	abstract fun calculate(): TXY
	override fun draw(): SVGGElement = SVGGElement {
		classList.add("elem", "point")
		use = SVGUseElement("#svg_$classname") {}
		appendChild(use!!)
	}

	override fun redraw(attr: String, graphic: SVGGraphicsElement?) {
		use?.transform?.set(tftranslate(calculate()))
	}

	val asPosDependency get() = ItemDeclaration.Instant(this, "pos")
}