package com.aimozg.psvg.model.values

import com.aimozg.psvg.jsobject
import com.aimozg.psvg.model.*
import tinycolor.ColorFormats
import tinycolor.TinyColor
import tinycolor.tinycolor2

/**
 * Created by aimozg on 02.02.2017.
 * Confidential
 */
class FixedColor(ctx: Context,
                 name: String?,
                 value: TinyColor,
                 default: TinyColor? = null) : ValueColor(ctx, name) {
	companion object {
		private const val TYPE = "const"
		val FIXEDCOLOR_LOADER = object: PartLoader(Category.VALUECOLOR,FixedColor::class, TYPE,
				JsTypename.STRING) {
			@Suppress("UNCHECKED_CAST_TO_NATIVE_INTERFACE")
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = FixedColor(ctx,
					json.name as? String ?: args[0] as? String?,
					tinycolor2(json.value as ColorFormats.ColorFormat))

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): FixedColor? {
				if (json is String && json[0] != '@') return FixedColor(ctx,args[0] as String?, tinycolor2(""+json))
				return null
			}
		}
	}
	private var value: TinyColor = value.clone()
	val default: TinyColor? = default?.clone()
	override fun updated(other: ModelElement, attr: Attribute) {
	}

	override fun save(): dynamic {
		val v = value.getOriginalInput()
		if (v is String && v[0] != '@' && name == null) return v
		return jsobject {
			it.type = TYPE
			it.name = name
			it.value = v
		}
	}

	override fun get() = value

	fun set(value: TinyColor, suppressEvent:Boolean = false, suppressUpdate:Boolean = false) {
		if (!value.isValid()) return
		this.value = value.clone()
		if (!suppressUpdate) editor?.notify(value.toString())
		if (!suppressEvent) update(Attribute.VAL)
	}
}

