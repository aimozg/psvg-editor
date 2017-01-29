package com.aimozg.psvg.model.segment

import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.*
import org.w3c.dom.svg.SVGGElement

abstract class Handle(ctx: Context,
                      name: String?,
                      val atStart: Boolean,
                      items: List<ItemDeclaration?>) :
		VisibleElement(ctx,name,null,items) {
	override val category: Category get() = Category.HANDLE

	val asPosDependency get() = asDependency("pos")
	abstract fun calculate(segment: CubicTo, start: TXY, stop: TXY): TXY
	override fun redraw(attr: String, g: SVGGElement) {
	}
}

