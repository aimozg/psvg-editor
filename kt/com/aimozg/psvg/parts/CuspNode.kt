package com.aimozg.psvg.parts

import com.aimozg.psvg.PartLoader
import com.aimozg.psvg.TXY
import com.aimozg.psvg.appendAll
import com.aimozg.psvg.jsobject
import org.w3c.dom.svg.SVGGElement

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class CuspNode(ctx: Context,
               name: String?,
               ownOrigin: Point?,
               pos: Point,
               val h1: Point?,
               val h2: Point?) :
		CommonNode(ctx, name, ownOrigin, pos, listOf(h1?.asPosDependency, h2?.asPosDependency)) {
	override fun updated(other: Part, attr: String) {
		super.updated(other, attr)
		if (other == pos) update("pos")
		else update("handle")
	}

	override fun draw(g: SVGGElement) {
		super.draw(g)
		g.appendAll(h1?.graphic, h2?.graphic)
	}

	override fun calcHandles(): Pair<TXY, TXY> {
		val xy = pos.calculate()
		return (h1?.calculate() ?: xy) to (h2?.calculate() ?: xy)
	}

	override fun save(): dynamic = jsobject {
		it.type = NODE_CUSP_TYPE
		it.name = name
		it.pos = pos.save()
		it.handle1 = h1?.save()
		it.handle2 = h2?.save()
	}

	companion object {
		const val NODE_CUSP_TYPE = "cusp"
		val NODE_CUSP_LOADER = object : PartLoader(Category.NODE,
				"CuspNode", NODE_CUSP_TYPE) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = CuspNode(ctx,
					json.name,
					ctx.loadPointOrNull(json.origin),
					ctx.loadPoint(json.pos),
					ctx.loadPointOrNull(json.handle1),
					ctx.loadPointOrNull(json.handle2))
		}.register()
	}
}