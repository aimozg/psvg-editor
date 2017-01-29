package com.aimozg.psvg.model

import kotlin.reflect.KClass

abstract class PartLoader(
		val cat: Category,
		val name: String,
		val typename: String?,
		vararg val objtypes: JsTypename
) {
	constructor(cat: Category, clazz: KClass<*>, typename: String?, vararg objtypes: JsTypename):
			this(cat,clazz.simpleName!!,typename,*objtypes)
	abstract fun loadStrict(ctx: Context, json: dynamic, vararg args:Any?): ModelElement
	open fun loadRelaxed(ctx: Context, json: dynamic, vararg args:Any?): ModelElement? = loadStrict(ctx, json)
	fun register(): PartLoader {
		Context.registerLoader(this)
		return this
	}
}