package com.aimozg.psvg.model

import com.aimozg.ktuple.Tuple2
import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.smoothHandles

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class SmoothNode(ctx: Context,
                 name: String?,
                 ownOrigin: Point?,
                 pos: Point,
                 val abq: ValueFloat,
                 val acq: ValueFloat,
                 val rot: ValueFloat) :
		CommonNode(ctx, name, ownOrigin, pos,
				listOf(abq.asValDependency,
						acq.asValDependency,
						rot.asValDependency,
						ItemDeclaration.Deferred { (it as ModelNode).prevNode.asPosDependency},
						ItemDeclaration.Deferred { (it as ModelNode).nextNode.asPosDependency})) {
	override fun updated(other: ModelElement, attr: String) {
		super.updated(other, attr)
		if (other is Point) update("*")
		if (other is ModelNode && (attr == "pos" || attr=="*") || other is ValueFloat) update("handle")
	}

	override fun calcHandles(): Tuple2<TXY, TXY> = smoothHandles(
			prevNode.center(),
			center(),
			nextNode.center(),
			abq.get(),
			acq.get(),
			rot.get())

	override fun save(): dynamic = jsobject{
		it.type = NODE_SMOOTH_TYPE
		it.name = name
		it.origin = ownOrigin?.save()
		it.pos = pos.save()
		it.b = abq.save()
		it.c = acq.save()
		it.rot = rot.save()
	}
	companion object {
		const val NODE_SMOOTH_TYPE = "smooth"
		val NODE_SMOOTH_LOADER = object: PartLoader(
				Category.NODE,
				SmoothNode::class.simpleName!!,
				NODE_SMOOTH_TYPE) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = SmoothNode(ctx,
					json.name,
					ctx.loadPoint(json.origin),
					ctx.loadPoint(json.pos)!!,
					ctx.loadFloat("prev",json.b,0.3),
					ctx.loadFloat("next",json.c,0.3),
					ctx.loadFloat("rotation",json.rot,0))
		}
	}
	interface SmoothNodeJson : ModelNodeJson {

	}
}
