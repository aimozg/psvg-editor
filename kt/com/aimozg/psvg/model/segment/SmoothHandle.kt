package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.get
import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.model.*
import com.aimozg.psvg.model.values.FixedFloat
import com.aimozg.psvg.smoothHandles

/**
 * Created by aimozg on 30.01.2017.
 * Confidential.
 */
class SmoothHandle(ctx: Context,
                   name: String?,
                   atStart: Boolean,
                   val size: ValueFloat,
                   val rot: ValueFloat) :
		Handle(ctx, name, atStart, listOf(
				size.asValDependency,
				rot.asValDependency,
				ItemDeclaration.Deferred {
					it as SmoothHandle
					if (atStart) it.segment?.prevInPath?.asStartDependency
					else it.segment?.nextInPath?.asStopDependency
				}
		)) {
	companion object {
		private const val TYPE = "Smooth"
		val HANDLE_SMOOTH_LOADER = object : PartLoader(Category.HANDLE, SmoothHandle::class, TYPE,
				JsTypename.OBJECT, JsTypename.STRING) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) =
					SmoothHandle(ctx,
							json.name,
							args[0] as Boolean,
							ctx.loadFloat("size", json.size, 0.3),
							ctx.loadFloat("rot", json.rot, 0)
					)

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): SmoothHandle? {
				if (json === TYPE) return SmoothHandle(ctx,null,
						args[0] as Boolean,
						ctx.loadFloat("size",null,0.3),
						ctx.loadFloat("rot",null,0))
				if (json is Array<*>) {
					val array: Array<*> = json
					if (array[0] != TYPE) return null
					val atStart = args[0] as Boolean
					val name: String?
					val size: Any?
					val rot: Any?
					when (array.size) {
						1 -> {
							name = null
							size = null
							rot = null
						}
						2 -> {
							name = null
							size = array[1]
							rot = null
						}
						3 -> {
							name = null
							size = array[1]
							rot = array[2]
						}
						4 -> {
							name = array[1] as String?
							size = array[2]
							rot = array[3]
						}
						else -> return null
					}
					return SmoothHandle(ctx, name,
							atStart,
							ctx.loadFloat("size", size, 0.3),
							ctx.loadFloat("rot", rot, 0))
				}
				return null
			}
		}
	}

	override fun save(): dynamic =
			if (name == null && (size as? FixedFloat)?.get() == 0.3 && (rot as? FixedFloat)?.get() == 0.0) TYPE
			else jsobject {
				it.type = TYPE
				it.name = name
				it.size = size.save()
				it.rot = rot.save()
			}

	override fun calculate(segment: CubicTo, start: TXY, stop: TXY): TXY {
		val b: TXY
		val a: TXY
		val c: TXY
		if (atStart) {
			b = segment.prevInPath?.start() ?: return start
			a = segment.start()
			c = segment.stop()
		} else {
			b = segment.start()
			a = segment.stop()
			c = segment.nextInPath?.stop() ?: return a
		}
		return smoothHandles(b, a, c, size.get(), size.get(), rot.get())[
				if (atStart) 1 else 0]
	}

	override fun updated(other: ModelElement, attr: String) {
		super.updated(other, attr)
		update("handle")
	}
}