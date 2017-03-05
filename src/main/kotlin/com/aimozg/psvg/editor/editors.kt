package com.aimozg.psvg.editor

import com.aimozg.psvg.*
import com.aimozg.psvg.model.EditorElement
import com.aimozg.psvg.model.ModelElement
import com.aimozg.psvg.model.values.FixedColor
import com.aimozg.psvg.model.values.FixedFloat
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import tinycolor.tinycolor2

fun editorFor(e:ModelElement):EditorElement? = when(e) {
	is FixedColor -> colorEditor(e)
	is FixedFloat -> floatEditor(e)
	else -> null
}
fun colorEditor(color: FixedColor): EditorElement {
	val handler = {e: Event ->
		val input = e.target as HTMLInputElement
		val s = input.value.trim()
		val value = if (s == "" && color.default != null) color.default.clone() else tinycolor2(s)
		if (value.isValid()) {
			color.set(value, suppressUpdate = true)
			input.classList -= "-error"
		} else {
			input.classList += "-error"
		}
	}
	val vval = color.get()
	val vid = "valuefloat_${color.id}"
	val vname = color.name
	val input = HTMLInputElement("text") {
		placeholder = color.default?.toString() ?: ""
		id = vid
		value = if (vval.toHex() == color.default?.toString()) "" else vval.toString()
		addEventListener("change", handler)
		addEventListener("input", handler)
	}
	val div = HTMLDivElement {
		classList.add("Value", color.classname)
		appendAll(HTMLLabelElement {
			htmlFor = vid
			textContent = vname
		}, input)
	}
	return InputEditor(div, input)
}
fun floatEditor(float: FixedFloat): EditorElement {
	val handler = {e: Event ->
		val input = e.target as HTMLInputElement
		val s = input.value.trim()
		val value = if (s == "" && float.def != null) float.def else s.toDoubleOrNull()
		if (value != null && float.validate(value)) {
			float.set(value, suppressUpdate = true)
			input.classList -= "-error"
		} else {
			input.classList += "-error"
		}
	}
	val vval = float.get()
	val vid = "valuefloat_${float.id}"
	val vname = float.name
	val input = HTMLInputElement("text") {
		placeholder = float.def?.toString() ?: ""
		id = vid
		value = if (vval == float.def) "" else vval.toString()
		addEventListener("change", handler)
		addEventListener("input", handler)
	}
	val div = HTMLDivElement {
		classList.add("Value", float.classname)
		appendAll(HTMLLabelElement {
			htmlFor = vid
			textContent = vname
		}, input)
	}
	return InputEditor(div, input)
}