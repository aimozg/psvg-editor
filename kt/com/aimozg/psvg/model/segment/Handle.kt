package com.aimozg.psvg.model.segment

import com.aimozg.psvg.SVGLineElement
import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.*
import com.aimozg.psvg.plusAssign
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGLineElement

abstract class Handle(ctx: Context,
                      name: String?,
                      val atStart: Boolean,
                      items: List<ItemDeclaration?>) :
		VisibleElement(ctx,name,null,items) {
	override val category: Category get() = Category.HANDLE
	override var owner: ModelElement?
		get() = super.owner
		set(value) {
			super.owner = value
			segment = value as? CubicTo?
		}
	var segment: CubicTo? = null
		private set

	val asHandleDependency get() = asDependency("handle")
	abstract fun calculate(segment: CubicTo, start: TXY, stop: TXY): TXY

	private var line: SVGLineElement? = null
	override fun draw(g: SVGGElement) {
		line = null
		super.draw(g)
	}
	override fun redraw(attr: String, g: SVGGElement) {
		line?.remove()
		line = null
		val segment = segment ?: return
		val start = segment.start()
		val stop = segment.stop()
		val cp = calculate(segment,start,stop)
		if (atStart) {
			line= SVGLineElement(start.x, start.y, cp.x, cp.y) {
				classList += "handle"
				g.insertBefore(this, g.firstChild)
			}
		} else {
			line= SVGLineElement(stop.x, stop.y, cp.x, cp.y) {
				classList += "handle"
				g.insertBefore(this, g.firstChild)
			}
		}
	}
}

