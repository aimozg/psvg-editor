package com.aimozg.psvg.model.node

import com.aimozg.ktuple.Tuple2
import com.aimozg.ktuple.component1
import com.aimozg.ktuple.component2
import com.aimozg.psvg.*
import com.aimozg.psvg.model.Context
import com.aimozg.psvg.model.point.Point
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGLineElement
import org.w3c.dom.svg.SVGUseElement

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
abstract class CommonNode(
		ctx: Context,
		name: String?,
		val pos: Point,
		items: List<ItemDeclaration?>
) : ModelNode(ctx, name, items + pos.asPosDependency) {
	protected var l1: SVGLineElement? = null
	protected var l2: SVGLineElement? = null
	protected var u0: SVGUseElement? = null

	override fun draw(g: SVGGraphicsElement) {
		l1 = null
		l2 = null
		u0 = SVGUseElement("#svg_$classname"){}
		super.draw(g)
	}

	override fun center(): TXY = pos.calculate()

	override fun doRedraw(attr: Attribute, g: SVGGraphicsElement) {
		val u0 = u0
		val xy = pos.calculate()
		if (u0!=null){
			u0.transform.set(tftranslate(xy))
			val (h1,h2) = calcHandles()
			l1?.remove()
			l2?.remove()
			l1 = null
			l2 = null
			if (!first) {
				l1 = SVGLineElement(xy.x,xy.y,h1.x,h1.y) {
					classList += "handle"
					g.insertBefore(this,g.firstChild)
				}
			}
			if (!last) {
				l2 = SVGLineElement(xy.x,xy.y,h2.x,h2.y) {
					classList += "handle"
					g.insertBefore(this,g.firstChild)
				}
			}
		}
	}

	override fun toDNode(): DNode {
		val xy = pos.calculate()
		val (h1,h2) = calcHandles()
		return DNode(xy,h1,h2)
	}

	protected abstract fun calcHandles(): Tuple2<TXY, TXY>
}