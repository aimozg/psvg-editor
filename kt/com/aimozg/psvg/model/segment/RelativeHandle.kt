package com.aimozg.psvg.model.segment

import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.model.*
import com.aimozg.psvg.plus

class RelativeHandle(ctx: Context,
                     name: String,
                     atStart: Boolean,
                     val dx: ValueFloat, val dy: ValueFloat):
		Handle(ctx, name, atStart, listOf(dx.asValDependency,dy.asValDependency)) {
	companion object {
		private const val TYPE = "Rel"
		val HANDLE_REL_LOADER = object: PartLoader(Category.HANDLE,RelativeHandle::class, TYPE) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = RelativeHandle(
					ctx,
					json.name,
					args[0] as Boolean,
					ctx.loadFloat("dx", json.dx, 0),
					ctx.loadFloat("dy", json.dy, 0))
		}.register()
	}

	override fun save(): dynamic = jsobject {
		it.type = TYPE
		it.name = name
		it.dx = dx.save()
		it.dy = dy.save()
	}

	override fun calculate(segment: CubicTo, start: TXY, stop: TXY): TXY =
			(if (atStart) start else stop).plus(dx.get(),dy.get())

	override fun updated(other: ModelElement, attr: String) {
		super.updated(other, attr)
		update("handle")
	}
}