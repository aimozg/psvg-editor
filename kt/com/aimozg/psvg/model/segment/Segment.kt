package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.Tuple2
import com.aimozg.ktuple.i0
import com.aimozg.ktuple.i1
import com.aimozg.psvg.SVGPathElement
import com.aimozg.psvg.TXY
import com.aimozg.psvg.d
import com.aimozg.psvg.model.*
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGPathElement
import kotlin.dom.appendTo

abstract class Segment(ctx: Context, name: String?, items: List<ItemDeclaration?>) :
		VisibleElement(ctx, name, null, items) {
	override final val category: Category get() = Category.SEGMENT

	override var owner: ModelElement? = null
		set(value) {
			super.owner = value
			path = value as? SegmentedPath?
		}
	var path: SegmentedPath? = null
		private set
	val index get() = path?.segments?.indexOf(this)
	val prev: Segment? get() {
		val segs = path?.segments ?: return null
		return segs.getOrNull(segs.indexOf(this) - 1)
	}
	val next: Segment? get() {
		val segs = path?.segments ?: return null
		return segs.getOrNull(segs.indexOf(this) + 1)
	}
	private var p: SVGPathElement? = null

	abstract fun toCmdAndPos(start: TXY): Tuple2<String, TXY>
	override fun draw(g: SVGGElement) {
		p = SVGPathElement { appendTo(g) }
		super.draw(g)
	}

	fun start(): TXY {
		return prev?.stop()?: TXY(0,0)
		/*return (path?.segments ?: emptyList<Segment>()).let {
			it.subList(0, it.indexOf(this))
		}.fold(TXY(0, 0)) { xy, s -> s.toCmdAndPos(xy).i1 }*/
	}
	fun stop(): TXY = toCmdAndPos(start()).i1

	override fun redraw(attr: String, g: SVGGElement) {
		val m1 = prev?.stop() ?: return
		p?.d = "M $m1 "+toCmdAndPos(m1).i0
	}

}

