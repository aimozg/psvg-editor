package com.aimozg.psvg.model.point

import com.aimozg.psvg.SVGUseElement
import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.Category
import com.aimozg.psvg.model.Context
import com.aimozg.psvg.model.VisibleElement
import com.aimozg.psvg.set
import com.aimozg.psvg.tftranslate
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGUseElement
import kotlin.dom.appendTo

abstract class Point(ctx: Context,
                     name: String?,
                     items: List<ItemDeclaration?>) :
		VisibleElement(ctx, name, items) {
	private var use: SVGUseElement? = null
	override val category: Category = Category.POINT
	abstract fun calculate(): TXY
	override fun draw(g: SVGGraphicsElement) {
		use = SVGUseElement("#svg_$classname") {appendTo(g)}
		super.draw(g)
	}

	override fun redraw(attr: Attribute, g: SVGGraphicsElement) {
		use?.transform?.set(tftranslate(calculate()))
	}

	val asPosDependency get() = ItemDeclaration.Instant(this, Attribute.POS)
	interface PointJson : VisualElementJson {

	}
}
