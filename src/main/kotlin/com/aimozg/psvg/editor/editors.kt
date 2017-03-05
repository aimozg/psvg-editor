package com.aimozg.psvg.editor

import com.aimozg.psvg.model.EditorElement
import com.aimozg.psvg.model.ModelElement
import com.aimozg.psvg.model.values.FixedColor
import com.aimozg.psvg.model.values.FixedFloat
import tinycolor.TinyColor
import tinycolor.tinycolor2

fun editorFor(e: ModelElement): EditorElement? = when (e) {
	is FixedColor -> ColorEditor(e)
	is FixedFloat -> FloatEditor(e)
	else -> null
}

class ColorEditor(val color: FixedColor) :
		InputEditor<TinyColor>(vid = "valuefloat_${color.id}",
		                       vname = color.name ?: "",
		                       vclassname = color.classname,
		                       vvalue = color.get(),
		                       vdefval = color.default
		) {
	override fun convert(s: String): Pair<Boolean, TinyColor> {
		val c = if (s == "" && color.default != null) color.default.clone() else tinycolor2(s)
		return c.isValid() to c
	}

	override fun update(value: TinyColor) {
		color.set(value, suppressUpdate = true)
	}
}

class FloatEditor(val float: FixedFloat) :
		InputEditor<Double>(
				vid = "valuefloat_${float.id}",
		        vname = float.name,
		        vclassname = float.classname,
		        vvalue = float.get(),
		        vdefval = float.def
		) {

	override fun convert(s: String): Pair<Boolean, Double?> {
		val v = if (s == "" && float.def != null) float.def else s.toDoubleOrNull()
		return (v!=null) to v
	}

	override fun update(value: Double) {
		float.set(value,suppressUpdate = true)
	}
}