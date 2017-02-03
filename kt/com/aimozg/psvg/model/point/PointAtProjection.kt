package com.aimozg.psvg.model.point

import com.aimozg.psvg.*
import com.aimozg.psvg.model.*
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGLineElement

/**
 * Projects 'p' onto the line 'ab' into point 'q', and stretches 'pq' by factor 'scale'
 */
class PointAtProjection(ctx: Context,
                        name: String?,
                        val a: Point,
                        val b: Point,
                        val p: Point,
                        val scale: ValueFloat) :
		Point(ctx, name, listOf(a.asPosDependency, b.asPosDependency, p.asPosDependency, scale.asValDependency)) {
	private var lab: SVGLineElement? = null
	private var lpq: SVGLineElement? = null
	companion object {
		const val POINT_AT_PROJECTION_TYPE = "PROJ"
		val POINT_AT_PROJECTION_LOADER = object : PartLoader(Category.POINT,PointAtProjection::class,
				POINT_AT_PROJECTION_TYPE) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = PointAtProjection(ctx,json.name,
					ctx.loadPoint(json.a)!!,
					ctx.loadPoint(json.b)!!,
					ctx.loadPoint(json.p)!!,
					ctx.loadFloat("scale",json.scale,1.0))
		}
	}

	override fun updated(other: ModelElement, attr: Attribute) {
		super.updated(other, attr)
		update(Attribute.POS)
	}

	override fun calculate(): TXY {
		val p = p.calculate()
		val q = ptproj(a.calculate(),b.calculate(), p)
		return p + (q-p)*scale.get()
	}

	override fun draw(g: SVGGraphicsElement) {
		super.draw(g)
		lab = null
		lpq = null
	}

	override fun redraw(attr: Attribute, g: SVGGraphicsElement) {
		super.redraw(attr, g)
		val a = a.calculate()
		val b = b.calculate()
		val p = p.calculate()
		val q = calculate()
		lab?.remove()
		lpq?.remove()
		lab = SVGLineElement(a.x,a.y,b.x,b.y){
			classList += "lineref"
			g.insertBefore(this, g.firstChild)
		}
		lpq = SVGLineElement(p.x,p.y,q.x,q.y){
			classList += "lineref"
			g.insertBefore(this, g.firstChild)
		}
	}

	override fun save(): dynamic = jsobject {
		it.type = POINT_AT_PROJECTION_TYPE
		it.name= name
		it.a = a.save()
		it.b = b.save()
		it.p = p.save()
		it.scale = scale.save()
	}
}