package com.aimozg.psvg.parts

import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGLineElement

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class PointAtProjection(ctx: Context,
                        name: String?,
                        val a: Point,
                        val b: Point,
                        val p: Point) :
		Point(ctx, name, listOf(a.asPosDependency, b.asPosDependency, p.asPosDependency)) {
	private var lab:SVGLineElement? = null
	private var lpq:SVGLineElement? = null
	companion object {
		const val POINT_AT_PROJECTION_TYPE = "PROJ"
		val POINT_AT_PROJECTION_LOADER = object : PartLoader(Category.POINT, "PointAtProjection", POINT_AT_PROJECTION_TYPE) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = PointAtProjection(ctx,json.name,
					ctx.loadPoint(json.a),
					ctx.loadPoint(json.b),
					ctx.loadPoint(json.p))
		}.register()
	}

	override fun updated(other: Part, attr: String) {
		super.updated(other, attr)
		update("pos")
	}

	override fun calculate() = ptproj(a.calculate(),b.calculate(),p.calculate())

	override fun draw(g: SVGGElement) {
		super.draw(g)
		lab = null
		lpq = null
		g.appendAll(a.graphic,b.graphic,p.graphic)
	}

	override fun redraw(attr: String, g: SVGGElement) {
		super.redraw(attr, g)
		val a = a.calculate()
		val b = b.calculate()
		val p = p.calculate()
		val q = calculate()
		lab?.remove()
		lpq?.remove()
		lab = SVGLineElement(a.x,a.y,b.x,b.y){
			g.insertBefore(this, g.firstChild)
		}
		lpq = SVGLineElement(p.x,p.y,q.x,q.y){
			g.insertBefore(this, g.firstChild)
		}
	}

	override fun save() = jsobject {
		it.type = POINT_AT_PROJECTION_TYPE
		it.name= name
		it.a = a.save()
		it.b = b.save()
		it.p = p.save()
	}
}