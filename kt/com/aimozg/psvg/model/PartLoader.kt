package com.aimozg.psvg.model

abstract class PartLoader(
		val cat: Category,
		val name: String,
		val typename: String?,
		vararg val objtypes: JsTypename
) {
	abstract fun loadStrict(ctx: Context, json: dynamic, vararg args:Any?): ModelElement
	open fun loadRelaxed(ctx: Context, json: dynamic, vararg args:Any?): ModelElement? = loadStrict(ctx, json)
	fun register(): PartLoader {
		Context.registerLoader(this)
		return this
	}
}