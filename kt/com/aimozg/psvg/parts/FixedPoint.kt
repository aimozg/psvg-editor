package com.aimozg.psvg.parts

import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGElement


class FixedPoint(ctx:ModelContext,
                 name:String?,
                 val x: ValueFloat,
                 val y: ValueFloat) :
		ModelPoint(ctx,name,listOf(x.asValDependency,y.asValDependency)) {

	override fun draw(): SVGGElement {
		val g = super.draw()
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
		return g
	}

	fun set(x:Double, y:Double) {
		this.x.set(x,true)
		this.y.set(y,true)
		update("pos")
	}
	fun set(xy: TXY) {
		set(xy.x,xy.y)
	}

	override fun calculate(): TXY = TXY(x.get(),y.get())

	override fun updated(other: Part, attr: String) {
		set(x.get(),y.get())
	}

	override fun save(): dynamic {
		if (name == null) return arrayOf(x.save(),y.save())
		return arrayOf(name,x.save(),y.save())
	}

	companion object {
		val POINT_FIXED_LOADER = object:ModelLoader(PartCategory.POINT,"FixedPoint","F",JsTypename.OBJECT) {
			override fun loadStrict(ctx: ModelContext, json: dynamic, vararg args: Any?): Part = FixedPoint(ctx,
					json.name,
					ctx.loadFloat("x",json.pt[0]),
					ctx.loadFloat("y",json.pt[1]))

			override fun loadRelaxed(ctx: ModelContext, json: dynamic, vararg args: Any?): Part? {
				val name:String?
				val x:dynamic
				val y:dynamic
				val length = json.length
				if (length == 2) {
					name = null
					x = json[0]
					y = json[1]
				} else if (length == 3) {
					name = ""+json[0]
					x = json[1]
					y = json[2]
				} else return null
				return FixedPoint(ctx,name,ctx.loadFloat("x",x),ctx.loadFloat("y",y))
			}
		}.register()
	}
}