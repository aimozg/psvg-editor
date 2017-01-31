package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.*
import com.aimozg.psvg.SVGLineElement
import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.*
import com.aimozg.psvg.plusAssign
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGLineElement

class CubicTo(ctx: Context,
              name: String?,
              val cp1: Handle?,
              val cp2: Handle?,
              val pt: Point) :
		Segment(ctx, name, listOf(
				cp1?.asHandleDependency,
				cp2?.asHandleDependency,
				pt.asPosDependency)) {
	override fun save(): Tuple =
			if (name == null) Tuple4(TYPE, cp1?.save(), cp2?.save(), pt.save())
			else Tuple5(TYPE, name, cp1?.save(), cp2?.save(), pt.save())

	companion object {
		private const val TYPE = "C"
		val SEGMENT_C_LOADER = object : PartLoader(Category.SEGMENT, CubicTo::class, TYPE,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) =
					CubicTo(ctx,
							json.name,
							ctx.loadHandle(json.cp1, true),
							ctx.loadHandle(json.cp2, false),
							ctx.loadPoint(json.pt)!!)

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): CubicTo? {
				if (json is Array<dynamic> && json[0] == TYPE) {
					when ((json as Array<*>).size) {
						4 -> {
							val a1: Tuple4<String, dynamic, dynamic, dynamic> = json
							return CubicTo(ctx,
									null,
									ctx.loadHandle(a1.i1, true),
									ctx.loadHandle(a1.i2, false),
									ctx.loadPoint(a1.i3)!!)
						}
						5 -> {
							val a2: Tuple5<String, String?, dynamic, dynamic, dynamic> = json
							return CubicTo(ctx,
									a2.i1,
									ctx.loadHandle(a2.i2, true),
									ctx.loadHandle(a2.i3, false),
									ctx.loadPoint(a2.i4)!!)
						}
					}
				}
				return null
			}
		}
	}

	protected var l1: SVGLineElement? = null
	protected var l2: SVGLineElement? = null
	override fun draw(g: SVGGElement) {
		l1 = null
		l2 = null
		super.draw(g)
	}

	private fun next(): TXY {
		return pt.calculate()//?:nextInLoop?.start()?:start()
	}

	override fun redraw(attr: String, g: SVGGElement) {
		super.redraw(attr, g)
		l1?.remove()
		l2?.remove()
		l1 = null
		l2 = null
		val prev = prevInList?.stop() ?: TXY(0, 0)
		val next = next()
		val cp1xy = cp1?.calculate(this, prev, next)
		val cp2xy = cp2?.calculate(this, prev, next)
		if (cp1xy != null) {
			l1 = SVGLineElement(prev.x, prev.y, cp1xy.x, cp1xy.y) {
				classList += "handle"
				g.insertBefore(this, g.firstChild)
			}
		}
		if (cp2xy != null) {
			l2 = SVGLineElement(next.x, next.y, cp2xy.x, cp2xy.y) {
				classList += "handle"
				g.insertBefore(this, g.firstChild)
			}
		}
	}

	override fun toCmdAndPos(start: TXY): Tuple2<String, TXY> {
		val ptxy = next()
		val cp1xy = cp1?.calculate(this, start, ptxy) ?: start
		val cp2xy = cp2?.calculate(this, start, ptxy) ?: ptxy
		return "$TYPE $cp1xy $cp2xy $ptxy" tup ptxy
	}

	override fun stop(): TXY = next()

	override fun updated(other: ModelElement, attr: String) {
		super.updated(other, attr)
		if (attr == "*" || attr == "pos") update("pos")
		if (attr == "*" || attr == "handle") update("handle")
	}
}