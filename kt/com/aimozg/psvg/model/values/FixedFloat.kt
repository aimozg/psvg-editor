package com.aimozg.psvg.model.values

import com.aimozg.psvg.*
import com.aimozg.psvg.model.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event

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
	private var input: HTMLInputElement? = null

	override fun updated(other: ModelElement, attr: String) {}

	override fun save(): dynamic {
		return get()
	}

	override fun get(): Double = value

	override fun editorElement(): HTMLElement {
		val handler = {e: Event ->
			val input = e.target as HTMLInputElement
			val s = input.value.trim()
			val value = if (s == "" && def != null) def else s.toDouble()
			if (validate(value)) {
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
				placeholder = def?.toString()?:""
				id = vid
				value = if (vval == def) "" else vval.toString()
				addEventListener("change",handler)
				addEventListener("input",handler)
			})
		}
	}

	fun set(value:Number, suppressEvent:Boolean = false, suppressUpdate:Boolean = false) {
		if (!validate(value)) return
		this.value = value.toDouble()
		if (!suppressUpdate) input?.value = value.toString()
		if (!suppressEvent) update("val")
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
					x = (json as String).toDouble()
					if (!x.isFinite()) error("Cannot load VALUEFLOAT ${JsTypename.of(json)} ${JSON.stringify(json)}")
				} else error("Cannot load VALUEFLOAT ${JsTypename.of(json)} ${JSON.stringify(json)}")
				return FixedFloat(ctx,name,x.toDouble(),def?.toDouble(),min.toDouble(),max.toDouble())
			}
		}
	}
}