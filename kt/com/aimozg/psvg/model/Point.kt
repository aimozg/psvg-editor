package com.aimozg.psvg.model

import com.aimozg.psvg.SVGUseElement
import com.aimozg.psvg.TXY
import com.aimozg.psvg.set
import com.aimozg.psvg.tftranslate
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGUseElement
import kotlin.dom.appendTo

abstract class Point(ctx: Context,
                     name: String?,
                     items: List<ItemDeclaration?>) :
		VisibleElement(ctx, name, null, items) {
	private var use: SVGUseElement? = null
	override val category: Category = Category.POINT
	abstract fun calculate(): TXY
	override fun draw(g: SVGGElement) {
		use = SVGUseElement("#svg_$classname") {appendTo(g)}
	}

	override fun redraw(attr: String, g: SVGGElement) {
		use?.transform?.set(tftranslate(calculate()))
	}

	val asPosDependency get() = ItemDeclaration.Instant(this, "pos")
}
interface PointJson : VisualElementJson {

}
