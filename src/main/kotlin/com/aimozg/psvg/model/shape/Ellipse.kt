package com.aimozg.psvg.model.shape

import com.aimozg.psvg.SVGEllipseElement
import com.aimozg.psvg.appendTo
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.model.*
import com.aimozg.psvg.model.point.Point
import com.aimozg.psvg.u
import org.w3c.dom.svg.SVGEllipseElement
import org.w3c.dom.svg.SVGGraphicsElement

/**
 * Created by aimozg on 07.02.2017.
 * Confidential
 */
class Ellipse(ctx: Context,
              name: String?,
              style: Style?,
              val center: Point,
              val rx: ValueFloat,
              val ry: ValueFloat) :
		Shape(ctx, name, style, listOf(center.asPosDependency, rx.asValDependency, ry.asValDependency)) {
	companion object {
		private const val TYPE = "ellipse"
		val ELLIPSE_LOADER = object: PartLoader(Category.SHAPE, Ellipse::class, TYPE){
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?): Ellipse {
				val radius: Any = json.radius
				val rx: ValueFloat
				val ry: ValueFloat
				if (radius is Array<dynamic>) {
					rx = ctx.loadFloat("rx",radius[0])
					ry = ctx.loadFloat("ry",radius[1])
				} else {
					rx = ctx.loadFloat("rx",radius)
					ry = ctx.loadFloat("ry",rx.save())
				}
				return Ellipse(ctx,
						json.name as String?,
						ctx.loadStyle(json.style),
						ctx.loadPoint(json.center)!!,
						rx,
						ry)
			}
		}
	}

	override fun save(): dynamic = jsobject {
		it.name = name
		it.type = TYPE
		it.style = style?.save()
		it.center = center.save()
		it.radius = arrayOf(rx.save(),ry.save())
	}

	private var shape: SVGEllipseElement? = null
	override fun draw(g: SVGGraphicsElement) {
		super.draw(g)
		shape = SVGEllipseElement { appendTo(g) }
	}

	override fun display(): SVGGraphicsElement? {
		return SVGEllipseElement { shape = this }
	}

	override fun updated(other: ModelElement, attr: Attribute) {
		super.updated(other, attr)
		if (attr eq Attribute.VAL || attr eq Attribute.POS) update(Attribute.POS)
	}

	override fun doRedraw(attr: Attribute, g: SVGGraphicsElement) {
		shape?.apply {
			rx.u = this@Ellipse.rx.get().toFloat()
			ry.u = this@Ellipse.ry.get().toFloat()
			val c = center.calculate()
			cx.u = c.x.toFloat()
			cy.u = c.y.toFloat()
			if (displayMode) {
				this@Ellipse.style?.applyTo(style)
			}
		}
	}

}