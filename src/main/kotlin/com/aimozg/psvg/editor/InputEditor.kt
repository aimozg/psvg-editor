package com.aimozg.psvg.editor

import com.aimozg.psvg.*
import com.aimozg.psvg.model.EditorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event

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
	override val container: HTMLElement = html.div {
		it.classList.add("Value", vclassname)
		+label {
			it.htmlFor = vid
			it.textContent = vname?:""
		}
		+textInput {
			it.id = vid
			it.placeholder = vplaceholder
			it.value = if (vvalue == vdefval) "" else vtos(vvalue)
			it.addEventListener("change", handler)
			it.addEventListener("input", handler)
			input = it
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