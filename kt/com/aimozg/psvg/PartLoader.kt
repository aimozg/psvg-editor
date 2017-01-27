package com.aimozg.psvg

import com.aimozg.psvg.parts.Category
import com.aimozg.psvg.parts.Context
import com.aimozg.psvg.parts.JsTypename
import com.aimozg.psvg.parts.Part

abstract class PartLoader(
		val cat: Category,
		val name: String,
		val typename: String?,
		vararg val objtypes: JsTypename
) {
	abstract fun loadStrict(ctx: Context, json: dynamic, vararg args:Any?): Part
	open fun loadRelaxed(ctx: Context, json: dynamic, vararg args:Any?): Part? = loadStrict(ctx, json)
	fun register(): PartLoader {
		Context.registerLoader(this)
		return this
	}
}