package com.aimozg.psvg.model.segment

import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.model.*
import com.aimozg.psvg.model.point.Point

class AbsoluteHandle(ctx: Context,
                     name: String?,
                     atStart: Boolean, val pt: Point):
		Handle(ctx, name, atStart, listOf(pt.asPosDependency)) {
	companion object {
		private const val TYPE = "Abs"
		val HANDLE_ABS_LOADER = object: PartLoader(Category.HANDLE,AbsoluteHandle::class, TYPE,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = AbsoluteHandle(
					ctx,
					json.name,
					args[0] as Boolean,
					ctx.loadPoint(json.pt)!!)

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): AbsoluteHandle? {
				if (json is Array<*>) {
					val array: Array<*> = json
					if (array.size != 2 || array[0] != TYPE) return null
					return AbsoluteHandle(ctx,
							null,
							args[0] as Boolean,
							ctx.loadPoint(array[1])!!)
				}
				return null
			}
		}
	}

	override fun save(): dynamic = jsobject {
		it.type = TYPE
		it.name = name
		it.pt = pt.save()
	}

	override fun calculate(segment: CubicTo, start: TXY, stop: TXY): TXY = pt.calculate()

	override fun updated(other: ModelElement, attr: Attribute) {
		super.updated(other, attr)
		update(Attribute.HANDLE)
	}
}