package com.aimozg.psvg.model

import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGLineElement

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class PointAtIntersection(ctx: Context,
                          name: String?,
                          val a1: Point,
                          val a2: Point,
                          val b1: Point,
                          val b2: Point) :
		Point(ctx, name, listOf(a1.asPosDependency, a2.asPosDependency, b1.asPosDependency, b2.asPosDependency)) {
	private var l1: SVGLineElement? = null
	private var l2: SVGLineElement? = null
	override fun updated(other: ModelElement, attr: String) {
		super.updated(other, attr)
		update("pos")
	}

	override fun calculate(): TXY = v22intersect(
			a1.calculate(), a2.calculate(),
			b1.calculate(), b2.calculate())

	override fun draw(g: SVGGElement) {
		super.draw(g)
		l1 = null
		l2 = null
	}

	override fun redraw(attr: String, g: SVGGElement) {
		super.redraw(attr,g)
		l1?.remove()
		l2?.remove()
		val a1 = a1.calculate()
		val a2 = a2.calculate()
		val b1 = b1.calculate()
		val b2 = b2.calculate()
		l1 = SVGLineElement(a1.x,a1.y,a2.x,a2.y) {
			classList += "handle"
			g.insertBefore(this, g.firstChild)
		}
		l2 = SVGLineElement(b1.x,b1.y,b2.x,b2.y) {
			classList += "handle"
			g.insertBefore(this, g.firstChild)
		}
	}

	override fun save(): PointAtIntersectionJson = jsobject2 {
		it.type = POINT_AT_INTERSECTION_TYPE
		it.name = name
		it.a1 = a1.save()
		it.a2 = a2.save()
		it.b1 = b1.save()
		it.b2 = b2.save()
	}
	companion object {
		val POINT_AT_INTERSECTION_TYPE = "I"
		val POINT_AT_INTERSECTION_LOADER = object: PartLoader(Category.POINT,PointAtIntersection::class,POINT_AT_INTERSECTION_TYPE){
			override fun loadStrict(ctx: Context, json: PointAtIntersectionJson, vararg args: Any?) = PointAtIntersection(ctx,
					json.name,
					ctx.loadPoint(json.a1)!!,
					ctx.loadPoint(json.a2)!!,
					ctx.loadPoint(json.b1)!!,
					ctx.loadPoint(json.b2)!!)
		}
	}
	interface PointAtIntersectionJson : PointJson {
		var a1:PointJson
		var a2:PointJson
		var b1:PointJson
		var b2:PointJson
	}
}
