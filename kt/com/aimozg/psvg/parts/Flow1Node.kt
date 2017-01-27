package com.aimozg.psvg.parts

import com.aimozg.psvg.PartLoader
import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.norm2fixed
import org.w3c.dom.svg.SVGGElement

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class Flow1Node(ctx: Context,
                name:String?,
                ownOrigin: Point?,
                pos: Point,
                val h1a: ValueFloat?,
                val h1b: ValueFloat?,
                val h2a: ValueFloat?,
                val h2b: ValueFloat?):
CommonNode(ctx,name,ownOrigin,pos,listOf(
		h1a?.asValDependency,
		h1b?.asValDependency,
		h2a?.asValDependency,
		h2b?.asValDependency,
		ItemDeclaration.Deferred{(it as Flow1Node).asPosDependency})){

	override fun updated(other: Part, attr: String) {
		super.updated(other, attr)
		if (other is Point) update("*")
		if (other is PathNode && (attr == "pos" || attr=="*") || other is ValueFloat) update("handle")
	}

	override fun draw(g: SVGGElement) {
		super.draw(g) // TODO draggable ctrl points
	}

	override fun calcHandles(): Pair<TXY, TXY> {
		val pos = center()
		val a1 = h1a?.get()
		val b1 = h1b?.get()
		val a2 = h2a?.get()
		val b2 = h2b?.get()
		val prev = prevNode.center()
		val next = nextNode.center()
		return (if (a1 != null && b1 != null) norm2fixed(prev,pos,a1,b1) else pos) to
				(if (a2 != null && b2 != null) norm2fixed(pos,next,a2,b2) else pos)
	}

	override fun save(): dynamic = jsobject {
		it.type = NODE_FLOW1_TYPE
		it.name = name
		it.pos = pos.save()
		it.h1ab = if (h1a != null && h1b != null) arrayOf(h1a.save(),h1b.save()) else null
		it.h2ab = if (h2a != null && h2b != null) arrayOf(h2a.save(),h2b.save()) else null
	}

	companion object {
		const val NODE_FLOW1_TYPE = "flow1"
		val NODE_FLOW1_LOADER = object: PartLoader(Category.NODE,"Flow1Node", NODE_FLOW1_TYPE) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = Flow1Node(
					ctx,
					json.name,
					ctx.loadPointOrNull(json.origin),
					ctx.loadPoint(json.pos),
					if (json.h1ab) ctx.loadFloat("prev_tangent",json.h1ab[0]) else null,
					if (json.h1ab) ctx.loadFloat("prev_normal",json.h1ab[1]) else null,
					if (json.h2ab) ctx.loadFloat("next_tangent",json.h2ab[0]) else null,
					if (json.h2ab) ctx.loadFloat("next_normal",json.h2ab[1]) else null
			)
		}.register()
	}
}