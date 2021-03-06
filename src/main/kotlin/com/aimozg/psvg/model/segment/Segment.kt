package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.Tuple2
import com.aimozg.ktuple.i0
import com.aimozg.psvg.SVGPathElement
import com.aimozg.psvg.TXY
import com.aimozg.psvg.appendTo
import com.aimozg.psvg.d
import com.aimozg.psvg.model.Category
import com.aimozg.psvg.model.Context
import com.aimozg.psvg.model.ModelElement
import com.aimozg.psvg.model.VisibleElement
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGPathElement

abstract class Segment(ctx: Context,
                       name: String?,
                       items: List<ItemDeclaration?>,
                       val visible: Boolean = true) :
		VisibleElement(ctx, name, items + listOf(
				ItemDeclaration.Deferred { (it as Segment).prevInList?.asStopDependency }
		)) {
	companion object {
		fun loadOneFromStream(ctx: Context, i: Iterator<Any?>, tc: Char, closed: Boolean): Segment = when (tc) {
			'L' -> {
				if (i.hasNext()) CubicTo(ctx,null,null,null,ctx.loadPoint(i.next()))
				else if (closed) ZSegment(ctx, null)
				else error("Unexpected end of compact stream")
			}
			'C' -> CubicTo(ctx, null,
					ctx.loadHandle(i.next(), true),
					ctx.loadHandle(i.next(), false),
					if (i.hasNext()) ctx.loadPoint(i.next())
					else if (closed) null
					else error("Unexpected end of compact stream")
			)
			else -> error("Unknown segment $tc in compact stream")
		}

		fun loadFromStream(ctx: Context, stream: Sequence<Any?>): List<Segment> {
			val i = stream.iterator()
			val rslt = ArrayList<Segment>()
			while (i.hasNext()) {
				val next = i.next()
				when (next) {
					"M" -> rslt.add(MoveTo(ctx, null,
							ctx.loadPoint(i.next())!!))
					"L" -> rslt.add(CubicTo(ctx, null,null,null,
							ctx.loadPoint(i.next())!!))
					"C" -> rslt.add(CubicTo(ctx, null,
							ctx.loadHandle(i.next(), true),
							ctx.loadHandle(i.next(), false),
							ctx.loadPoint(i.next())))
					"Z" -> rslt.add(ZSegment(ctx, null))
					"ML", "MLZ", "MC", "MCZ" -> {
						rslt.add(MoveTo(ctx, null,
								ctx.loadPoint(i.next())!!))
						val type = next as String
						val tc = type[1]
						val closed = type.endsWith("Z")
						while (i.hasNext()) rslt.add(loadOneFromStream(ctx, i, tc, closed))
						if (closed && tc == 'L') rslt.add(ZSegment(ctx, null))
					}
					else -> rslt.add(ctx.loadSegment(next))
				}
			}
			return rslt
		}
	}

	override final val category: Category get() = Category.SEGMENT
	val asStopDependency get() = asDependency(Attribute.POS)
	val asStartDependency get() = prevInList?.asStopDependency

	override var owner: ModelElement?
		get() = super.owner
		set(value) {
			super.owner = value
			path = value as? SegmentedPath?
		}
	var path: SegmentedPath? = null
		private set
	val segsOfPath get() = path?.segments
	val index get() = path?.segments?.indexOf(this)
	val prevInList: Segment?
		get() {
			val segments = segsOfPath
			return segments?.getOrNull(segments.indexOf(this) - 1)
		}
	val nextInList: Segment?
		get() {
			val segments = segsOfPath
			return segments?.getOrNull(segments.indexOf(this) + 1)
		}
	val prevInLoop: Segment?
		get() {
			val segments = segsOfPath
			return segments?.getOrNull((segments.indexOf(this) + segments.size - 1) % segments.size)
		}
	val nextInLoop: Segment?
		get() {
			val segments = segsOfPath
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
	override fun draw(g: SVGGraphicsElement) {
		p = SVGPathElement { appendTo(g) }
		super.draw(g)
	}

	open fun start() = prevInList?.stop() ?: TXY(0, 0)
	abstract fun stop(): TXY// toCmdAndPos(start()).i1

	override fun doRedraw(attr: Attribute, g: SVGGraphicsElement) {
		val m1 = start()
		p?.d = "M $m1 " + toCmdAndPos(m1).i0
	}

}

