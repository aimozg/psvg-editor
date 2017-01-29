package com.aimozg.psvg.model.segment

import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.model.*
import com.aimozg.psvg.norm2fixed

/**
 * Created by aimozg on 30.01.2017.
 * Confidential
 */
class FlowHandle(ctx: Context,
                 name: String,
                 atStart: Boolean,
                 val alpha: ValueFloat,
                 val beta: ValueFloat):
		Handle(ctx, name, atStart, listOf(
				alpha.asValDependency,
				beta.asValDependency)) {
	companion object {
		private const val TYPE = "Flow"
		val HANDLE_FLOW_LOADER = object: PartLoader(Category.HANDLE,FlowHandle::class, TYPE) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?): FlowHandle {
				val alpha: dynamic
				val beta: dynamic
				if (json.ab is Array<*>) {
					alpha = json.ab[0]
					beta = json.ab[1]
				} else {
					alpha = json.alpha
					beta = json.beta
				}
				return FlowHandle(
						ctx,
						json.name,
						args[0] as Boolean,
						ctx.loadFloat("alpha", alpha, 0),
						ctx.loadFloat("beta", beta, 0))
			}
		}.register()
	}

	override fun save(): dynamic = jsobject {
		it.type = TYPE
		it.name = name
		it.ab = arrayOf(alpha.save(),beta.save())
	}

	override fun calculate(segment: CubicTo, start: TXY, stop: TXY): TXY =
			if (atStart) norm2fixed(start, stop, alpha.get(), beta.get())
			else norm2fixed(stop, start, alpha.get(), -beta.get())

	override fun updated(other: ModelElement, attr: String) {
		super.updated(other, attr)
		update("*")
	}
}