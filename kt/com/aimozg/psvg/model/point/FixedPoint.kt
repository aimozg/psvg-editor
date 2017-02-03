package com.aimozg.psvg.model.point

import com.aimozg.ktuple.*
import com.aimozg.psvg.TXY
import com.aimozg.psvg.makeDraggable
import com.aimozg.psvg.model.*
import com.aimozg.psvg.model.values.FixedFloat
import com.aimozg.psvg.onsdrag
import com.aimozg.psvg.onsdragstart
import org.w3c.dom.svg.SVGGraphicsElement


class FixedPoint(ctx: Context,
                 name:String?,
                 val x: ValueFloat,
                 val y: ValueFloat) :
		Point(ctx,name,listOf(x.asValDependency,y.asValDependency)) {

	override fun draw(g: SVGGraphicsElement) {
		super.draw(g)
		g.makeDraggable()
		g.onsdragstart { _, d ->
			val pos = calculate()
			d.start.x = pos.x
			d.start.y = pos.y
		}
		g.onsdrag { e,d->
			e.preventDefault()
			set(d.start.x+d.movement.x,d.start.y+d.movement.y)
		}
	}

	fun set(x:Double, y:Double) {
		if (this.x is FixedFloat && this.y is FixedFloat) {
			this.x.set(x, true)
			this.y.set(y, true)
			update(Attribute.POS)
		}
	}
	fun set(xy: TXY) {
		set(xy.x,xy.y)
	}

	override fun calculate(): TXY = TXY(x.get(),y.get())

	override fun updated(other: ModelElement, attr: Attribute) {
		super.updated(other, attr)
		if (other == x || other == y) {
			update(Attribute.POS)
		} else {
			set(x.get(), y.get())
		}
	}

	override fun save(): dynamic {
		if (name == null) return arrayOf(x.save(),y.save())
		return arrayOf(name,x.save(),y.save())
	}

	companion object {
		val POINT_FIXED_LOADER = object: PartLoader(Category.POINT,FixedPoint::class,"F",
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: FixedPointJson, vararg args: Any?): ModelElement = FixedPoint(ctx,
					json.name,
					ctx.loadFloat("x",json.pt[0]),
					ctx.loadFloat("y",json.pt[1]))

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): ModelElement? {
				val name:String?
				val x:dynamic
				val y:dynamic
				if (json is Array<Any?>) {
					val tuple: Tuple = json
					when (tuple.length) {
						2 -> {
							@Suppress("UNCHECKED_CAST_TO_NATIVE_INTERFACE")
							tuple as Tuple2<*, *>
							name = null
							x = tuple.i0
							y = tuple.i1
						}
						3 -> {
							@Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_NATIVE_INTERFACE")
							tuple as Tuple3<String, *, *>
							name = "" + tuple.i0
							x = tuple.i1
							y = tuple.i2
						}
						else -> return null
					}
				} else return null
				return FixedPoint(ctx,name,ctx.loadFloat("x",x),ctx.loadFloat("y",y))
			}
		}
	}
	interface FixedPointJson : PointJson {
		var pt: Array<Number>
	}
}
