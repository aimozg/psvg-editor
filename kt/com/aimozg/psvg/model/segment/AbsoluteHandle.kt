package com.aimozg.psvg.model.segment

import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.model.*

class AbsoluteHandle(ctx: Context,
                     name: String?,
                     atStart: Boolean, val pt: Point):
		Handle(ctx, name, atStart, listOf(pt.asPosDependency)) {
	companion object {
		private const val TYPE = "Abs"
		val HANDLE_ABS_LOADER = object: PartLoader(Category.HANDLE,AbsoluteHandle::class, TYPE) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = AbsoluteHandle(
					ctx,
					json.name,
					args[0] as Boolean,
					ctx.loadPoint(json.pt)!!)
		}
	}

	override fun save(): dynamic = jsobject {
		it.type = TYPE
		it.name = name
		it.pt = pt.save()
	}

	override fun calculate(segment: CubicTo, start: TXY, stop: TXY): TXY = pt.calculate()

	override fun updated(other: ModelElement, attr: String) {
		super.updated(other, attr)
		update("handle")
	}
}