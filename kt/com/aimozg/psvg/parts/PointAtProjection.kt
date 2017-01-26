package com.aimozg.psvg.parts

import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGLineElement

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class PointAtProjection(ctx: ModelContext,
                        name: String?,
                        val a: ModelPoint,
                        val b: ModelPoint,
                        val p: ModelPoint) :
		ModelPoint(ctx, name, listOf(a.asPosDependency, b.asPosDependency, p.asPosDependency)) {
	private var lab:SVGLineElement? = null
	private var lpq:SVGLineElement? = null
	companion object {
		const val POINT_AT_PROJECTION_TYPE = "PROJ"
		val POINT_AT_PROJECTION_LOADER = object : ModelLoader(PartCategory.POINT, "PointAtProjection", POINT_AT_PROJECTION_TYPE) {
			override fun loadStrict(ctx: ModelContext, json: dynamic, vararg args: Any?) = PointAtProjection(ctx,json.name,
					ctx.loadPoint(json.a),
					ctx.loadPoint(json.b),
					ctx.loadPoint(json.p))
		}.register()
	}

	override fun updated(other: Part, attr: String) {
		update("pos")
	}

	override fun calculate() = ptproj(a.calculate(),b.calculate(),p.calculate())

	override fun draw(): SVGGElement {
		lab = null
		lpq = null
		return super.draw().appendAll(a.graphic,b.graphic,p.graphic)
	}

	override fun redraw(attr: String, graphic: SVGGraphicsElement?) {
		super.redraw(attr,graphic)
		val a = a.calculate()
		val b = b.calculate()
		val p = p.calculate()
		val q = calculate()
		lab?.remove()
		lpq?.remove()
		lab = SVGLineElement(a.x,a.y,b.x,b.y){
			graphic?.insertBefore(this,graphic.firstChild)
		}
		lpq = SVGLineElement(p.x,p.y,q.x,q.y){
			graphic?.insertBefore(this,graphic.firstChild)
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