package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.get
import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.model.*
import com.aimozg.psvg.smoothHandles

/**
 * Created by aimozg on 30.01.2017.
 * Confidential.
 */
class SmoothHandle(ctx: Context,
                   name: String,
                   atStart: Boolean,
                   val size: ValueFloat,
                   val rot: ValueFloat):
	Handle(ctx, name, atStart, listOf(
			size.asValDependency,
			rot.asValDependency,
			ItemDeclaration.Deferred {
				it as SmoothHandle
				if (atStart) it.segment?.prevInPath?.asStartDependency
				else it.segment?.nextInPath?.asStopDependency
			}
	)){
	companion object {
		private const val TYPE = "Smooth"
		val HANDLE_SMOOTH_LOADER = object: PartLoader(Category.HANDLE,SmoothHandle::class,TYPE) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) =
					SmoothHandle(ctx,
							json.name,
							args[0] as Boolean,
							ctx.loadFloat("size", json.size, 0.3),
							ctx.loadFloat("rot", json.rot, 0)
			)
		}
	}

	override fun save(): dynamic  = jsobject{
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