package com.aimozg.psvg.model.point

import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.model.*

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class PointRef(
		ctx: Context,
		name:String?,
		val ref:String
): Point(ctx,name,listOf(ItemDeclaration.Deferred { (it as PointRef).obj.asPosDependency })) {

	override fun calculate(): TXY = obj.calculate()

	val obj: Point by lazy {
		ctx.findPoint(ref) ?: throw NullPointerException("Cannot dereference Point.@$ref")
	}

	override fun updated(other: ModelElement, attr: String) {
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
		val POINT_REF_LOADER = object: PartLoader(Category.POINT,PointRef::class, POINT_REF_TYPE,
				JsTypename.STRING, JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?): ModelElement {
				return PointRef(ctx,json.name,json.ref)
			}

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): ModelElement? {
				val a:Any? = json
				if (a is String) return PointRef(ctx,null,a.substring(1))
				val type: Any? = json.type
				if (type is String && type[0] == '@') return PointRef(ctx,json["type"],type.substring(1))
				return null
			}
		}
	}
}
