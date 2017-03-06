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
	override val container: HTMLElement = HTMLDivElement {
		classList.add("Value", vclassname)
		appendAll(HTMLLabelElement {
			htmlFor = vid
			textContent = vname?:""
		}, HTMLInputElement("text") {
			id = vid
			placeholder = vplaceholder
			value = if (vvalue == vdefval) "" else vtos(vvalue)
			addEventListener("change", handler)
			addEventListener("input", handler)
			input = this
		})
	}

	override fun notify(value: Any?) {
		@Suppress("UNCHECKED_CAST")
		input.value = vtos(value as T)
	}

	protected open fun vtos(v:T): String = v.toString()
	protected abstract fun convert(s:String):Pair<Boolean,T?>
	protected abstract fun update(value:T)
}