package com.aimozg.psvg.model

import com.aimozg.ktuple.Tuple2
import com.aimozg.ktuple.get
import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGLineElement

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class Flow1Node(ctx: Context,
                name: String?,
                ownOrigin: Point?,
                pos: Point,
                val h1a: ValueFloat?,
                val h1b: ValueFloat?,
                val h2a: ValueFloat?,
                val h2b: ValueFloat?) :
		CommonNode(ctx, name, ownOrigin, pos, listOf(
				h1a?.asValDependency,
				h1b?.asValDependency,
				h2a?.asValDependency,
				h2b?.asValDependency,
				ItemDeclaration.Deferred { (it as Flow1Node).prevNode.asPosDependency },
				ItemDeclaration.Deferred { (it as Flow1Node).nextNode.asPosDependency })) {
	private var ltan1: SVGLineElement? = null
	private var lnorm1: SVGLineElement? = null
	private var ltan2: SVGLineElement? = null
	private var lnorm2: SVGLineElement? = null

	override fun updated(other: ModelElement, attr: String) {
		super.updated(other, attr)
		if (other is Point) update("*")
		if (other is PathNode && (attr == "pos" || attr == "*") || other is ValueFloat) update("handle")
	}

	override fun draw(g: SVGGElement) {
		super.draw(g) // TODO draggable ctrl points
		ltan1 = null
		lnorm1 = null
		ltan2 = null
		lnorm2 = null
	}

	override fun redraw(attr: String, g: SVGGElement) {
		super.redraw(attr, g)
		val pos = center()
		ltan1?.remove()
		lnorm1?.remove()
		val a1 = h1a?.get()
		val b1 = h1b?.get()
		if (a1 != null && b1 != null) {
			val prev = prevNode.center()
			val tan = prev + (pos - prev) * a1
			ltan1 = SVGLineElement(prev.x, prev.y, tan.x, tan.y) {
				classList += "lineref"
				g.insertBefore(this, g.firstChild)
			}
			val norm = tan + (pos - prev).rot90() * b1
			lnorm1 = SVGLineElement(tan.x, tan.y, norm.x, norm.y) {
				classList += "lineref"
				g.insertBefore(this, g.firstChild)
			}
		}
		val a2 = h2a?.get()
		val b2 = h2b?.get()
		ltan2?.remove()
		lnorm2?.remove()
		if (a2 != null && b2 != null) {
			val next = nextNode.center()
			val tan = pos + (next - pos) * a2
			ltan2 = SVGLineElement(pos.x, pos.y, tan.x, tan.y) {
				classList += "lineref"
				g.insertBefore(this, g.firstChild)
			}
			val norm = tan + (next - pos).rot90() * b2
			lnorm2 = SVGLineElement(tan.x, tan.y, norm.x, norm.y) {
				classList += "lineref"
				g.insertBefore(this, g.firstChild)
			}
		}
	}

	override fun calcHandles(): Tuple2<TXY, TXY> {
		val pos = center()
		val a1 = h1a?.get()
		val b1 = h1b?.get()
		val a2 = h2a?.get()
		val b2 = h2b?.get()
		val prev = prevNode.center()
		val next = nextNode.center()
		return Tuple2(if (a1 != null && b1 != null) norm2fixed(prev, pos, a1, b1) else pos,
				if (a2 != null && b2 != null) norm2fixed(pos, next, a2, b2) else pos)
	}

	override fun save(): dynamic = jsobject2<Flow1NodeJson> {
		it.type = NODE_FLOW1_TYPE
		it.name = name
		it.pos = pos.save()
		it.h1ab = if (h1a != null && h1b != null) Tuple2(h1a.save(), h1b.save()) else null
		it.h2ab = if (h2a != null && h2b != null) Tuple2(h2a.save(), h2b.save()) else null
	}

	companion object {
		const val NODE_FLOW1_TYPE = "flow1"
		val NODE_FLOW1_LOADER = object : PartLoader(
				Category.NODE,
				Flow1Node::class.simpleName!!,
				NODE_FLOW1_TYPE) {
			override fun loadStrict(ctx: Context, json: Flow1NodeJson, vararg args: Any?) = Flow1Node(
					ctx,
					json.name,
					ctx.loadPointOrNull(json.origin),
					ctx.loadPoint(json.pos),
					if (json.h1ab != null) ctx.loadFloat("prev_tangent", json.h1ab!![0]) else null,
					if (json.h1ab != null) ctx.loadFloat("prev_normal", json.h1ab!![1]) else null,
					if (json.h2ab != null) ctx.loadFloat("next_tangent", json.h2ab!![0]) else null,
					if (json.h2ab != null) ctx.loadFloat("next_normal", json.h2ab!![1]) else null
			)
		}.register()
	}
	interface Flow1NodeJson : PathNodeJson {
		var pos: Point.PointJson
		var h1ab: Tuple2<Number,Number>?
		var h2ab: Tuple2<Number,Number>?
	}

}
