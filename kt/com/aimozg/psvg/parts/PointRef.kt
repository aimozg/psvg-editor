package com.aimozg.psvg.parts

import com.aimozg.psvg.PartLoader
import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class PointRef(
		ctx: Context,
		name:String?,
		val ref:String
): Point(ctx,name,listOf(ItemDeclaration.Deferred{(it as PointRef).obj().asPosDependency})) {

	override fun calculate(): TXY = obj().calculate()

	fun obj(): Point = ctx.findPoint(ref)!!

	override fun updated(other: Part, attr: String) {
		super.updated(other, attr)
		if (attr == "pos" || attr == "*") update("pos")
	}

	override fun save(): dynamic {
		if (name == null) return "@$ref"
		return jsobject{
			it.name = name
			it.type = "@$ref"
		}
	}
	companion object {
		const val POINT_REF_TYPE = "@"
		val POINT_REF_LOADER = object: PartLoader(Category.POINT,"PointRef", POINT_REF_TYPE,JsTypename.STRING,JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?): Part {
				return PointRef(ctx,json.name,json.ref)
			}

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): Part? {
				val a:Any? = json
				if (a is String) return PointRef(ctx,null,a.substring(1))
				val type: Any? = json.type
				if (type is String && type[0] == '@') return PointRef(ctx,json["type"],type.substring(1))
				return null
			}
		}.register()
	}
}
