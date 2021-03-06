package com.aimozg.psvg.model.values

import com.aimozg.psvg.model.*

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
class FixedFloat(
		ctx: Context,
		name:String?,
		private var value:Double,
		val def:Double?,
		val min:Double,
		val max:Double
) : ValueFloat(ctx,name) {
	init {
		if (def?:min<min || def?:max>max || min>max) error("Illegal bounds for $name: def=$def min=$min max=$max")
	}

	override fun updated(other: ModelElement, attr: Attribute) {}

	override fun save(): dynamic {
		return get()
	}

	override fun get(): Double = value

	fun set(value:Number, suppressEvent:Boolean = false, suppressUpdate:Boolean = false) {
		if (!validate(value)) return
		this.value = value.toDouble()
		if (!suppressUpdate) editor?.notify(value)
		if (!suppressEvent) update(Attribute.VAL)
	}
	fun validate(x:Number):Boolean = x.toDouble().let { it.isFinite() && it>=min && it<=max }

	companion object {
		private const val TYPE = "const"
		val FIXEDFLOAT_LOADER = object: PartLoader(
				Category.VALUEFLOAT,
				FixedFloat::class.simpleName!!,
				TYPE,
				JsTypename.NUMBER, JsTypename.STRING, JsTypename.UNDEFINED, JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?): FixedFloat = loadRelaxed(ctx,json,*args) ?: error("Cannot load VALUEFLOAT ${JsTypename.of(json)} ${JSON.stringify(json)}")
			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): FixedFloat? {
				val name = args[0] as String?
				val def = args[1] as Number?
				val min = args[2] as Number
				val max = args[3] as Number
				val x:Number
				if (def != null && def !=undefined && (json == null || json == undefined)) {
					x = def
				} else if (json is Number) {
					x = json
				} else if (json is String) {
					x = (""+json).toDouble()
					if (!x.isFinite()) error("Cannot load VALUEFLOAT ${JsTypename.of(json)} ${JSON.stringify(json)}")
				} else error("Cannot load VALUEFLOAT ${JsTypename.of(json)} ${JSON.stringify(json)}")
				return FixedFloat(ctx,name,x.toDouble(),def?.toDouble(),min.toDouble(),max.toDouble())
			}
		}
	}
}

