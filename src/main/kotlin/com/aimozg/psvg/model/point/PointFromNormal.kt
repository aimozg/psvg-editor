package com.aimozg.psvg.model.point

import com.aimozg.psvg.jsobject2
import com.aimozg.psvg.model.*
import com.aimozg.psvg.norm2fixed

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class PointFromNormal(ctx: Context,
                      name:String?,
                      val pt0: Point,
                      val pt1: Point,
                      val alpha: ValueFloat,
                      val beta: ValueFloat):
Point(ctx,name,listOf(pt0.asPosDependency,pt1.asPosDependency,alpha.asValDependency,beta.asValDependency)){
	override fun updated(other: ModelElement, attr: Attribute) {
		super.updated(other, attr)
		update(Attribute.POS)
	}

	override fun calculate() = norm2fixed(pt0.calculate(),pt1.calculate(),alpha.get(),beta.get())

	override fun save(): PointFromNormalJson = jsobject2 {
		it.type = POINT_FROM_NORMAL_TYPE
		it.name = name
		it.pt0 = pt0.save()
		it.pt1 = pt1.save()
		it.alpha = alpha.save()
		it.beta = beta.save()
	}
	companion object {
		const val POINT_FROM_NORMAL_TYPE = "N"
		val POINT_FROM_NORMAL_LOADER = object: PartLoader(Category.POINT,PointFromNormal::class, POINT_FROM_NORMAL_TYPE) {
			override fun loadStrict(ctx: Context, json: PointFromNormalJson, vararg args: Any?) = PointFromNormal(ctx,json.name,
					ctx.loadPoint(json.pt0)!!,
					ctx.loadPoint(json.pt1)!!,
					ctx.loadFloat("tangent",json.alpha,0),
					ctx.loadFloat("normal",json.beta,0))
		}
	}
	interface PointFromNormalJson : PointJson {
		var pt0: PointJson
		var pt1: PointJson
		var alpha: Any?
		var beta: Any?
	}
}