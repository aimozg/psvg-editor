package com.aimozg.psvg.model.values

import com.aimozg.psvg.*
import com.aimozg.psvg.model.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
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
		val FIXEDCOLOR_LOADER = object: PartLoader(Category.VALUECOLOR,FixedColor::class,TYPE,
				JsTypename.STRING) {
			@Suppress("UNCHECKED_CAST_TO_NATIVE_INTERFACE")
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = FixedColor(ctx,
					json.name as? String ?: args[0] as? String?,
					tinycolor2(json.value as ColorFormats.ColorFormat))

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): FixedColor? {
				if (json is String && (json as String)[0] != '@') return FixedColor(ctx,args[0] as String?,tinycolor2(""+json))
				return null
			}
		}
	}
	private var value: TinyColor = value.clone()
	val default:TinyColor? = default?.clone()
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

	private var input: HTMLInputElement? = null

	fun set(value:TinyColor, suppressEvent:Boolean = false, suppressUpdate:Boolean = false) {
		if (!value.isValid()) return
		this.value = value.clone()
		if (!suppressUpdate) input?.value = value.toString()
		if (!suppressEvent) update(Attribute.VAL)
	}

	override fun editorElement(): HTMLElement {
		val handler = {e: Event ->
			val input = e.target as HTMLInputElement
			val s = input.value.trim()
			val value = if (s == "" && default != null) default.clone() else tinycolor2(s)
			if (value.isValid()) {
				set(value,suppressUpdate = true)
				input.classList -= "-error"
			} else {
				input.classList += "-error"
			}
		}
		val vval = get()
		val vid = "valuefloat_${this.id}"
		val vname = name
		return HTMLDivElement {
			classList.add("Value",classname)
			appendAll(HTMLLabelElement {
				htmlFor = vid
				textContent = vname
			}, HTMLInputElement("text") {
				input = this
				placeholder = default?.toString()?:""
				id = vid
				value = if (vval.toHex() == default?.toString()) "" else vval.toString()
				addEventListener("change",handler)
				addEventListener("input",handler)
			})
		}
	}

}