package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.Tuple
import com.aimozg.ktuple.Tuple1
import com.aimozg.ktuple.Tuple2
import com.aimozg.ktuple.tup
import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.Category
import com.aimozg.psvg.model.Context
import com.aimozg.psvg.model.JsTypename
import com.aimozg.psvg.model.PartLoader

/**
 * Created by aimozg on 31.01.2017.
 * Confidential
 */
class ZSegment(ctx: Context, name: String?):
	Segment(ctx, name, emptyList(), false) {
	override fun save(): Tuple = if (name == null) Tuple1(TYPE) else Tuple2(TYPE, name)
	companion object {
		private const val TYPE = "Z"
		val SEGMENT_Z_LOADER = object : PartLoader(Category.SEGMENT, ZSegment::class, TYPE,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) =
					ZSegment(ctx,json.name)

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): ZSegment? {
				if (json is Array<dynamic> && json[0] == TYPE)
					when ((json as Array<*>).size) {
						2 -> return ZSegment(ctx, json[1] as String)
						1 -> return ZSegment(ctx, null)
					}
				return null
			}
		}
	}

	override fun toCmdAndPos(start: TXY): Tuple2<String, TXY> = "Z" tup start
	override fun stop(): TXY = nextInLoop?.start()?:start()

}