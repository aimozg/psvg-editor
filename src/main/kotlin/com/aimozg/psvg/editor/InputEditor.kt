package com.aimozg.psvg.editor

import com.aimozg.psvg.minusAssign
import com.aimozg.psvg.model.EditorElement
import com.aimozg.psvg.plusAssign
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.input
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onInputFunction
import kotlinx.html.label
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import kotlin.browser.document

abstract class InputEditor<T:Any>(
		vid: String,
		vname: String?,
		vclassname:String,
		vvalue: T,
		val vdefval: T? = null,
		vplaceholder: String = vdefval?.toString()?:""
		) : EditorElement {
	private val handler = { e: Event ->
		val input = e.target as HTMLInputElement
		val (valid,value) = convert(input.value.trim())
		if (valid && value != null) {
			update(value)
			input.classList -= "-error"
		} else {
			input.classList += "-error"
		}
	}
	private lateinit var input: HTMLInputElement
	override val container: HTMLElement = document.create.div("Value $vclassname").apply {
		append.label {
			for_ = vid
			+(vname?:"")
		}
		input = append.input(InputType.text) {
			id = vid
			placeholder = vplaceholder
			value = if (vvalue == vdefval) "" else vtos(vvalue)
			onChangeFunction = handler
			onInputFunction = handler
		}
	}

	override fun notify(value: Any?) {
		@Suppress("UNCHECKED_CAST")
		input.value = vtos(value as T)
	}

	protected open fun vtos(v:T): String = v.toString()
	protected abstract fun convert(s:String):Pair<Boolean,T?>
	protected abstract fun update(value:T)
}