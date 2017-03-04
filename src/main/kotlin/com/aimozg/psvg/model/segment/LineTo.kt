package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.*
import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.*
import com.aimozg.psvg.model.point.Point

@Suppress("DEPRECATION")
@Deprecated("Use CubicTo")
class LineTo(ctx: Context,
             name: String?,
             val pt: Point) : Segment(ctx, name, listOf(pt.asPosDependency)) {
	companion object {
		private const val TYPE = "L"
		val SEGMENT_L_LOADER = object : PartLoader(Category.SEGMENT, LineTo::class, TYPE,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = LineTo(ctx, json.name, ctx.loadPoint(json.pt)!!)
			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): LineTo? {
				if (json is Array<dynamic> && json[0] == TYPE) {
					when ((json as Array<*>).size) {
						2 -> {
							val a1: Tuple2<String, dynamic> = json
							return LineTo(ctx, null, ctx.loadPoint(a1.i1)!!)
						}
						3 -> {
							val a2: Tuple3<String, String?, dynamic> = json
							return LineTo(ctx, a2.i1, ctx.loadPoint(a2.i2)!!)
						}
					}
				}
				return null
			}
		}
	}

	override fun save(): Tuple {
		return if (name == null) Tuple[TYPE, pt.save()] else Tuple[TYPE, name, pt.save()]
	}

	override fun toCmdAndPos(start: TXY): Tuple2<String, TXY> {
		val pos = pt.calculate()
		return Tuple["${TYPE} $pos",pos]
	}

	override fun stop(): TXY = pt.calculate()

	override fun updated(other: ModelElement, attr: Attribute) {
		super.updated(other, attr)
		if (attr eq Attribute.POS) update(Attribute.POS)
	}
}