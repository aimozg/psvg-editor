package com.aimozg.psvg

import org.w3c.dom.DOMTokenList
import org.w3c.dom.Node
import org.w3c.dom.css.CSSStyleDeclaration

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

fun Node.appendTo(parent:Node) {
	parent.appendChild(this)
}