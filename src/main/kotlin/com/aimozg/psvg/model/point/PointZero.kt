package com.aimozg.psvg.model.point

import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.Category
import com.aimozg.psvg.model.Context
import com.aimozg.psvg.model.JsTypename
import com.aimozg.psvg.model.PartLoader

/**
 * Created by aimozg on 07.02.2017.
 * Confidential
 */
class PointZero(ctx: Context) : Point(ctx, null, emptyList()) {
	companion object {
		private const val TYPE = "zero"
		val POINT_ZERO_LOADER = object: PartLoader(Category.POINT,PointZero::class, TYPE, JsTypename.NUMBER) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = PointZero(ctx)
			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): PointZero? {
				if (json is Double && json == 0) return PointZero(ctx)
				return null
			}
		}
	}

	override fun save() = 0

	override fun calculate() = TXY(0,0)
}