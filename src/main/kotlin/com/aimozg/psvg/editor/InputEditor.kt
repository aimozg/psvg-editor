package com.aimozg.psvg.editor

import com.aimozg.psvg.model.EditorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

class InputEditor(
		override val container: HTMLElement,
		val input: HTMLInputElement): EditorElement {
	override fun notify(value: Any?) {
		input.value = value?.toString()?:""
	}

}