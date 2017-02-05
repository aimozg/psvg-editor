package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.*
import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.*
import com.aimozg.psvg.model.point.Point
import org.w3c.dom.svg.SVGGraphicsElement

class CubicTo(ctx: Context,
              name: String?,
              val cp1: Handle?,
              val cp2: Handle?,
              val pt: Point?) :
		Segment(ctx, name, listOf(
				cp1?.asHandleDependency,
				cp2?.asHandleDependency,
				pt?.asPosDependency?:ItemDeclaration.Deferred{
					(it as Segment).segsOfPath?.firstOrNull()?.run { asStartDependency ?: asStopDependency}
				})) {
	override fun save(): Tuple =
			if (name == null) Tuple4(TYPE, cp1?.save(), cp2?.save(), pt?.save())
			else Tuple5(TYPE, name, cp1?.save(), cp2?.save(), pt?.save())

	companion object {
		private const val TYPE = "C"
		val SEGMENT_C_LOADER = object : PartLoader(Category.SEGMENT, CubicTo::class, TYPE,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) =
					CubicTo(ctx,
							json.name,
							ctx.loadHandle(json.cp1, true),
							ctx.loadHandle(json.cp2, false),
							ctx.loadPoint(json.pt))

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): CubicTo? {
				if (json is Array<dynamic> && json[0] == TYPE) {
					when ((json as Array<*>).size) {
						4 -> {
							val a1: Tuple4<String, dynamic, dynamic, dynamic> = json
							return CubicTo(ctx,
									null,
									ctx.loadHandle(a1.i1, true),
									ctx.loadHandle(a1.i2, false),
									ctx.loadPoint(a1.i3))
						}
						5 -> {
							val a2: Tuple5<String, String?, dynamic, dynamic, dynamic> = json
							return CubicTo(ctx,
									a2.i1,
									ctx.loadHandle(a2.i2, true),
									ctx.loadHandle(a2.i3, false),
									ctx.loadPoint(a2.i4))
						}
					}
				}
				return null
			}
		}
	}

	override fun draw(g: SVGGraphicsElement) {
		super.draw(g)
	}

	override fun doRedraw(attr: Attribute, g: SVGGraphicsElement) {
		super.doRedraw(attr, g)
		cp1?.redraw()
		cp2?.redraw()
	}

	override fun toCmdAndPos(start: TXY): Tuple2<String, TXY> {
		val ptxy = stop()
		val cp1xy = cp1?.calculate(this, start, ptxy) ?: start
		val cp2xy = cp2?.calculate(this, start, ptxy) ?: ptxy
		return "$TYPE $cp1xy $cp2xy $ptxy" tup ptxy
	}

	override fun stop(): TXY = pt?.calculate()?: segsOfPath?.firstOrNull()?.start()?: TXY(0,0)

	override fun updated(other: ModelElement, attr: Attribute) {
		super.updated(other, attr)
		if (attr eq Attribute.POS) {
			if (other == pt) update(Attribute.POS)
			else update(Attribute.HANDLE)
		}
		if (attr eq Attribute.HANDLE) update(Attribute.HANDLE)
	}
}