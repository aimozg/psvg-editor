package com.aimozg.psvg

import com.aimozg.psvg.parts.JsTypename
import com.aimozg.psvg.parts.ModelContext
import com.aimozg.psvg.parts.Part
import com.aimozg.psvg.parts.PartCategory

abstract class ModelLoader(
		val cat: PartCategory,
		val name: String,
		val typename: String?,
		vararg val objtypes: JsTypename
) {
	abstract fun loadStrict(ctx: ModelContext, json: dynamic, vararg args:Any?): Part
	open fun loadRelaxed(ctx: ModelContext, json: dynamic, vararg args:Any?): Part? = loadStrict(ctx, json)
	fun register():ModelLoader {
		ModelContext.registerLoader(this)
		return this
	}
}