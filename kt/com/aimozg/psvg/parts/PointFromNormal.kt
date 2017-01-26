package com.aimozg.psvg.parts

import com.aimozg.psvg.ModelLoader
import com.aimozg.psvg.appendAll
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.norm2fixed
import org.w3c.dom.svg.SVGGElement

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class PointFromNormal(ctx:ModelContext,
                      name:String?,
                      val pt0: ModelPoint,
                      val pt1: ModelPoint,
                      val alpha:ValueFloat,
                      val beta:ValueFloat):
ModelPoint(ctx,name,listOf(pt0.asPosDependency,pt1.asPosDependency,alpha.asValDependency,beta.asValDependency)){
	override fun draw(): SVGGElement {
		return super.draw().appendAll(pt0.graphic,pt1.graphic)
	}

	override fun updated(other: Part, attr: String) {
		update("pos")
	}

	override fun calculate() = norm2fixed(pt0.calculate(),pt1.calculate(),alpha.get(),beta.get())

	override fun save() = jsobject {
		it.type = POINT_FROM_NORMAL_TYPE
		it.name = name
		it.pt0 = pt0.save()
		it.pt1 = pt1.save()
		it.alpha = alpha.save()
		it.beta = beta.save()
	}
	companion object {
		const val POINT_FROM_NORMAL_TYPE = "N"
		val POINT_FROM_NORMAL_LOADER = object:ModelLoader(PartCategory.POINT,"PointFromNormal", POINT_FROM_NORMAL_TYPE) {
			override fun loadStrict(ctx: ModelContext, json: dynamic, vararg args: Any?) = PointFromNormal(ctx,json.name,
					ctx.loadPoint(json.pt0),
					ctx.loadPoint(json.pt1),
					ctx.loadFloat("tangent",json.alpha,0),
					ctx.loadFloat("normal",json.beta,0))
		}.register()
	}
}