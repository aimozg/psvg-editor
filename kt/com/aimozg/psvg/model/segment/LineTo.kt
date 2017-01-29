package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.*
import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.*

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
		}.register()
	}

	override fun save(): Tuple {
		return if (name == null) TYPE tup pt.save() else Tuple3(TYPE, name, pt.save())
	}

	override fun toCmdAndPos(start: TXY): Tuple2<String, TXY> {
		val pos = pt.calculate()
		return "$TYPE $pos" tup pos
	}

	override fun updated(other: ModelElement, attr: String) {
		super.updated(other, attr)
		if (attr == "*" || attr == "pos") update()
	}
}