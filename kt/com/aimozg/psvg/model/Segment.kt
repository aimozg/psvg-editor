package com.aimozg.psvg.model

import com.aimozg.ktuple.*
import com.aimozg.psvg.TXY
import org.w3c.dom.svg.SVGGElement

abstract class Segment(ctx: Context, name: String?, items: List<ItemDeclaration?>) :
		VisibleElement(ctx, name, null, items) {
	override final val category: Category get() = Category.SEGMENT

	abstract fun toCmdAndPos(start: TXY): Tuple2<String, TXY>
	override fun draw(g: SVGGElement) {
	}

	override fun redraw(attr: String, g: SVGGElement) {
	}

}

class MoveTo(ctx: Context,
             name: String?,
             val pt: Point) : Segment(ctx, name, listOf(pt.asPosDependency)) {
	companion object {
		private const val TYPE = "M"
		val SEGMENT_M_LOADER = object:PartLoader(Category.SEGMENT,MoveTo::class, TYPE,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = MoveTo(ctx,json.name,ctx.loadPoint(json.pt))
			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): MoveTo? {
				if (json is Array<dynamic> && json[0] == TYPE) {
					when((json as Array<*>).size) {
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
	override fun save() = if (name == null) TYPE tup pt.save() else Tuple3(TYPE,name,pt.save())

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
		val SEGMENT_L_LOADER = object:PartLoader(Category.SEGMENT,LineTo::class, TYPE,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = LineTo(ctx,json.name,ctx.loadPoint(json.pt))
			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): LineTo? {
				if (json is Array<dynamic> && json[0] == TYPE) {
					when((json as Array<*>).size) {
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
		return if (name == null) TYPE tup pt.save() else Tuple3(TYPE,name,pt.save())
	}

	override fun toCmdAndPos(start: TXY): Tuple2<String, TXY> {
		val pos = pt.calculate()
		return "$TYPE ${pos.x},${pos.y}" tup pos
	}
}
