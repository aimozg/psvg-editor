package com.aimozg.psvg.parts

import com.aimozg.psvg.PartLoader
import com.aimozg.psvg.jsobject

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
class Parameter(
		ctx: Context,
		name:String,
		val def:ValueFloat,
		val min:ValueFloat,
		val max:ValueFloat) : Part(ctx,name,
		listOf(def.asValDependency, min.asValDependency, max.asValDependency)

){
	override val category: Category = Category.PARAM

	override fun save() = jsobject {
		it.name = name
		it.def = def.save()
		it.min = min.save()
		it.max = max.save()
	}

	override fun updated(other: Part, attr: String) {
		update("meta")
	}
	companion object {
		val PARAM_LOADER = object: PartLoader(Category.PARAM,"Param",null,JsTypename.OBJECT){
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?): Part {
				return Parameter(ctx,json.name,
						ctx.loadFloat("default",json.def,0.5),
						ctx.loadFloat("min",json.min,0),
						ctx.loadFloat("max",json.max,1))
			}
		}.register()
	}
}