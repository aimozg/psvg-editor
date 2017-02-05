package com.aimozg.psvg.model.segment

import com.aimozg.psvg.SVGLineElement
import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.Category
import com.aimozg.psvg.model.Context
import com.aimozg.psvg.model.ModelElement
import com.aimozg.psvg.model.VisibleElement
import com.aimozg.psvg.plusAssign
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGLineElement

abstract class Handle(ctx: Context,
                      name: String?,
                      val atStart: Boolean,
                      items: List<ItemDeclaration?>) :
		VisibleElement(ctx, name, items) {
	override val category: Category get() = Category.HANDLE
	override var owner: ModelElement?
		get() = super.owner
		set(value) {
			super.owner = value
			segment = value as? CubicTo?
		}
	var segment: CubicTo? = null
		private set

	val asHandleDependency get() = asDependency(Attribute.HANDLE)
	abstract fun calculate(segment: CubicTo, start: TXY, stop: TXY): TXY

	private var line: SVGLineElement? = null
	override fun draw(g: SVGGraphicsElement) {
		line = null
		super.draw(g)
	}
	fun redraw() {
		this.redraw(Attribute.HANDLE,this.graphic)
	}
	override fun doRedraw(attr: Attribute, g: SVGGraphicsElement) {
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

