package com.aimozg.psvg.model

import com.aimozg.ktuple.*
import com.aimozg.psvg.SVGPathElement
import com.aimozg.psvg.TXY
import com.aimozg.psvg.d
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
	}

	fun start(): TXY {
		return prev?.stop()?:TXY(0,0)
		/*return (path?.segments ?: emptyList<Segment>()).let {
			it.subList(0, it.indexOf(this))
		}.fold(TXY(0, 0)) { xy, s -> s.toCmdAndPos(xy).i1 }*/
	}
	fun stop(): TXY = toCmdAndPos(start()).i1

	override fun redraw(attr: String, g: SVGGElement) {
		val m1 = prev?.stop() ?: return
		p?.d = "M ${m1.x},${m1.y} "+toCmdAndPos(m1).i0
	}

}

class MoveTo(ctx: Context,
             name: String?,
             val pt: Point) : Segment(ctx, name, listOf(pt.asPosDependency)) {
	companion object {
		private const val TYPE = "M"
		val SEGMENT_M_LOADER = object : PartLoader(Category.SEGMENT, MoveTo::class, TYPE,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = MoveTo(ctx, json.name, ctx.loadPoint(json.pt))
			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): MoveTo? {
				if (json is Array<dynamic> && json[0] == TYPE) {
					when ((json as Array<*>).size) {
						2 -> {
							val a1: Tuple2<String, dynamic> = json
							return MoveTo(ctx, null, ctx.loadPoint(a1.i1))
						}
						3 -> {
							val a2: Tuple3<String, String?, dynamic> = json
							return MoveTo(ctx, a2.i1, ctx.loadPoint(a2.i2))
						}
					}
				}
				return null
			}
		}.register()
	}

	override fun save() = if (name == null) TYPE tup pt.save() else Tuple3(TYPE, name, pt.save())

	override fun toCmdAndPos(start: TXY): Tuple2<String, TXY> {
		val pos = pt.calculate()
		return "$TYPE ${pos.x},${pos.y}" tup pos
	}
}

class LineTo(ctx: Context,
             name: String?,
             val pt: Point) : Segment(ctx, name, listOf(pt.asPosDependency)) {
	companion object {
		private const val TYPE = "L"
		val SEGMENT_L_LOADER = object : PartLoader(Category.SEGMENT, LineTo::class, TYPE,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = LineTo(ctx, json.name, ctx.loadPoint(json.pt))
			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): LineTo? {
				if (json is Array<dynamic> && json[0] == TYPE) {
					when ((json as Array<*>).size) {
						2 -> {
							val a1: Tuple2<String, dynamic> = json
							return LineTo(ctx, null, ctx.loadPoint(a1.i1))
						}
						3 -> {
							val a2: Tuple3<String, String?, dynamic> = json
							return LineTo(ctx, a2.i1, ctx.loadPoint(a2.i2))
						}
					}
				}
				return null
			}
		}.register()
	}

	override fun save(): Tuple {
		return if (name == null) TYPE tup pt.save() else Tuple3(TYPE, name, pt.save())
	}

	override fun toCmdAndPos(start: TXY): Tuple2<String, TXY> {
		val pos = pt.calculate()
		return "$TYPE ${pos.x},${pos.y}" tup pos
	}
}
