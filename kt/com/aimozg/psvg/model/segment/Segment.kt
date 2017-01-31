package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.Tuple2
import com.aimozg.ktuple.i0
import com.aimozg.psvg.SVGPathElement
import com.aimozg.psvg.TXY
import com.aimozg.psvg.d
import com.aimozg.psvg.model.*
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGPathElement
import kotlin.dom.appendTo

abstract class Segment(ctx: Context,
                       name: String?,
                       items: List<ItemDeclaration?>,
                       val visible: Boolean = true) :
		VisibleElement(ctx, name, null, items + listOf(
				ItemDeclaration.Deferred { (it as Segment).prevInList?.asStopDependency }
		)) {
	override final val category: Category get() = Category.SEGMENT
	val asStopDependency get() = asDependency("pos")
	val asStartDependency get() = prevInList?.asStopDependency

	override var owner: ModelElement?
		get() = super.owner
		set(value) {
			super.owner = value
			path = value as? SegmentedPath?
		}
	var path: SegmentedPath? = null
		private set
	protected val segments get() = path?.segments
	val index get() = path?.segments?.indexOf(this)
	val prevInList: Segment?
		get() {
			val segments = segments
			return segments?.getOrNull(segments.indexOf(this) - 1)
		}
	val nextInList: Segment?
		get() {
			val segments = segments
			return segments?.getOrNull(segments.indexOf(this) + 1)
		}
	val prevInLoop: Segment?
		get() {
			val segments = segments
			return segments?.getOrNull((segments.indexOf(this) + segments.size - 1) % segments.size)
		}
	val nextInLoop: Segment?
		get() {
			val segments = segments
			return segments?.getOrNull((segments.indexOf(this) + 1) % segments.size)
		}
	val prevInPath: Segment?
		get() {
			var prev = prevInLoop
			while (prev != null && prev != this && !prev.visible) prev = prev.prevInLoop
			return if (prev == this) null else prev
		}
	val nextInPath: Segment?
		get() {
			var next = nextInLoop
			while (next != null && next != this && !next.visible) next = next.nextInLoop
			return if (next == this) null else next
		}
	private var p: SVGPathElement? = null

	abstract fun toCmdAndPos(start: TXY): Tuple2<String, TXY>
	override fun draw(g: SVGGElement) {
		p = SVGPathElement { appendTo(g) }
		super.draw(g)
	}

	fun start() = prevInList?.stop() ?: TXY(0, 0)
	abstract fun stop(): TXY// toCmdAndPos(start()).i1

	override fun redraw(attr: String, g: SVGGElement) {
		val m1 = start()
		p?.d = "M $m1 " + toCmdAndPos(m1).i0
	}

}

