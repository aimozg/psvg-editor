package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.*
import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.*
import com.aimozg.psvg.model.point.Point

class MoveTo(ctx: Context,
             name: String?,
             val pt: Point) : Segment(ctx, name, listOf(pt.asPosDependency), false) {
	companion object {
		private const val TYPE = "M"
		val SEGMENT_M_LOADER = object : PartLoader(Category.SEGMENT, MoveTo::class, TYPE,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = MoveTo(ctx, json.name, ctx.loadPoint(json.pt)!!)
			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): MoveTo? {
				if (json is Array<dynamic> && json[0] == TYPE) {
					when ((json as Array<*>).size) {
						2 -> {
							val a1: Tuple2<String, dynamic> = json
							return MoveTo(ctx, null, ctx.loadPoint(a1.i1)!!)
						}
						3 -> {
							val a2: Tuple3<String, String?, dynamic> = json
							return MoveTo(ctx, a2.i1, ctx.loadPoint(a2.i2)!!)
						}
					}
				}
				return null
			}
		}
	}

	override fun updated(other: ModelElement, attr: ModelElement.Attribute) {
		super.updated(other, attr)
		if (attr eq ModelElement.Attribute.POS) update(ModelElement.Attribute.POS)
	}

	override fun start(): TXY = prevInList?.stop() ?: pt.calculate()
	override fun stop(): TXY = pt.calculate()

	override fun save() = if (name == null) Tuple[TYPE, pt.save()] else Tuple[TYPE, name, pt.save()]

	override fun toCmdAndPos(start: TXY): Tuple2<String, TXY> {
		val pos = pt.calculate()
		return Tuple["$TYPE $pos",pos]
	}
}