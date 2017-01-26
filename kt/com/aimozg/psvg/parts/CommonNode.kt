package com.aimozg.psvg.parts

import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGLineElement
import org.w3c.dom.svg.SVGUseElement

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
abstract class CommonNode(
		ctx: ModelContext,
		name: String?,
		ownOrigin: ModelPoint?,
		val pos: ModelPoint,
		items: List<ItemDeclaration?>
) : ModelNode(ctx, name, ownOrigin, items + pos.asPosDependency) {
	protected var l1: SVGLineElement? = null
	protected var l2: SVGLineElement? = null
	protected var u0: SVGUseElement? = null

	override fun draw(): SVGGElement {
		l1 = null
		l2 = null
		return SVGGElement {
			classList += "node"
			u0 = SVGUseElement("#svg_$classname"){}
			appendAll(u0,pos.graphic)
		}
	}

	override fun center(): TXY = pos.calculate()

	override fun redraw(attr: String, graphic: SVGGraphicsElement?) {
		val u0 = u0
		val xy = pos.calculate()
		if (graphic!=null && u0!=null){
			u0.transform.set(tftranslate(xy))
			val (h1,h2) = calcHandles()
			l1?.remove()
			l2?.remove()
			l1 = null
			l2 = null
			if (!first) {
				l1 = SVGLineElement(xy.x,xy.y,h1.x,h1.y) {
					classList += "handle"
					graphic.insertBefore(this,graphic.firstChild)
				}
			}
			if (!last) {
				l2 = SVGLineElement(xy.x,xy.y,h2.x,h2.y) {
					classList += "handle"
					graphic.insertBefore(this,graphic.firstChild)
				}
			}
		}
	}

	override fun toDNode(): DNode {
		val xy = pos.calculate()
		val (h1,h2) = calcHandles()
		return DNode(xy,h1,h2)
	}

	protected abstract fun calcHandles():Pair<TXY,TXY>
}