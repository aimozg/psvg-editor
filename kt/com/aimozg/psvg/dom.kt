package com.aimozg.psvg

import org.w3c.dom.DOMTokenList
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.css.CSSStyleDeclaration
import kotlin.browser.document

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */

operator fun DOMTokenList.plusAssign(value: String) = add(value)

var CSSStyleDeclaration.any: String
	get() = getPropertyValue("any")
	set(value) = setProperty("any", value)

inline fun HTMLDivElement(init: HTMLDivElement.() -> Unit): HTMLDivElement = (document.createElement("div") as HTMLDivElement).apply(init)

fun HTMLElement.appendAll(vararg children: Element?) {
	for (child in children) if (child != null) appendChild(child)
}

