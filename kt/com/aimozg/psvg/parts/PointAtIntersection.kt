package com.aimozg.psvg.parts

import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGLineElement

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class PointAtIntersection(ctx: ModelContext,
                          name: String?,
                          val a1: ModelPoint,
                          val a2: ModelPoint,
                          val b1: ModelPoint,
                          val b2: ModelPoint) :
		ModelPoint(ctx, name, listOf(a1.asPosDependency, a2.asPosDependency, b1.asPosDependency, b2.asPosDependency)) {
	private var l1: SVGLineElement? = null
	private var l2: SVGLineElement? = null
	override fun updated(other: Part, attr: String) {
		update("pos")
	}

	override fun calculate(): TXY = v22intersect(
			a1.calculate(), a2.calculate(),
			b1.calculate(), b2.calculate())

	override fun draw(): SVGGElement {
		l1 = null
		l2 = null
		return super.draw().appendAll(a1.graphic,a2.graphic,b1.graphic,b2.graphic)
	}

	override fun redraw(attr: String, graphic: SVGGraphicsElement?) {
		super.redraw(attr)
		l1?.remove()
		l2?.remove()
		val a1 = a1.calculate()
		val a2 = a2.calculate()
		val b1 = b1.calculate()
		val b2 = b2.calculate()
		l1 = SVGLineElement(a1.x,a1.y,a2.x,a2.y) {
			classList += "handle"
			graphic?.insertBefore(this,graphic.firstChild)
		}
		l2 = SVGLineElement(b1.x,b1.y,b2.x,b2.y) {
			classList += "handle"
			graphic?.insertBefore(this,graphic.firstChild)
		}
	}

	override fun save(): dynamic = jsobject{
		it.type = POINT_AT_INTERSECTION_TYPE
		it.name = name
		it.a1 = a1.save()
		it.a2 = a2.save()
		it.b1 = b1.save()
		it.b2 = b2.save()
	}
	companion object {
		val POINT_AT_INTERSECTION_TYPE = "I"
		val POINT_AT_INTERSECTION_LOADER = object:ModelLoader(PartCategory.POINT,"PointAtIntersection", POINT_AT_INTERSECTION_TYPE){
			override fun loadStrict(ctx: ModelContext, json: dynamic, vararg args: Any?) = PointAtIntersection(ctx,
					json.name,
					ctx.loadPoint(json.a1),
					ctx.loadPoint(json.a2),
					ctx.loadPoint(json.b1),
					ctx.loadPoint(json.b2))
		}.register()
	}
}