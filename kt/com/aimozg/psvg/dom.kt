package com.aimozg.psvg

import org.w3c.dom.*
import org.w3c.dom.css.CSSStyleDeclaration
import kotlin.browser.document

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */

operator fun DOMTokenList.plusAssign(value: String) = add(value)

operator fun DOMTokenList.minusAssign(value: String) = remove(value)

var CSSStyleDeclaration.any: String
	get() = getPropertyValue("any")
	set(value) = setProperty("any", value)

operator fun CSSStyleDeclaration.set(prop: String, value: String) {
	setProperty(prop, value)
}

operator fun CSSStyleDeclaration.get(prop: String) = getPropertyValue(prop)

inline fun HTMLDivElement(init: HTMLDivElement.() -> Unit): HTMLDivElement = (document.createElement("div") as HTMLDivElement).apply(init)
inline fun HTMLInputElement(type: String, init: HTMLInputElement.() -> Unit): HTMLInputElement = (document.createElement("input") as HTMLInputElement).apply {
	this.type = type
	init(this)
}

inline fun HTMLLabelElement(init: HTMLLabelElement.() -> Unit): HTMLLabelElement = (document.createElement("label") as HTMLLabelElement).apply(init)

fun HTMLElement.appendAll(vararg children: Element?) {
	children.filterNotNull()
			.forEach { appendChild(it) }
}

