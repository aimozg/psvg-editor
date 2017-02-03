@file:Suppress("unused")

package com.aimozg.psvg.d

import org.w3c.dom.Element

/**
 * Created by aimozg on 03.02.2017.
 * Copy of jquery/common.kt of Kotlin js stdlib as of 1.1 b38
 */
external class JQuery() {
	fun addClass(className: String): JQuery
	fun addClass(f: Element.(Int, String) -> String): JQuery

	fun attr(attrName: String): String
	fun attr(attrName: String, value: String): JQuery

	fun html(): String
	fun html(s: String): JQuery
	fun html(f: Element.(Int, String) -> String): JQuery


	fun hasClass(className: String): Boolean
	fun removeClass(className: String): JQuery
	fun height(): Number
	fun width(): Number

	fun click(): JQuery

	fun mousedown(handler: Element.(MouseEvent) -> Unit): JQuery
	fun mouseup(handler: Element.(MouseEvent) -> Unit): JQuery
	fun mousemove(handler: Element.(MouseEvent) -> Unit): JQuery

	fun dblclick(handler: Element.(MouseClickEvent) -> Unit): JQuery
	fun click(handler: Element.(MouseClickEvent) -> Unit): JQuery

	fun load(handler: Element.() -> Unit): JQuery
	fun change(handler: Element.() -> Unit): JQuery

	fun append(str: String): JQuery
	fun ready(handler: () -> Unit): JQuery
	fun text(text: String): JQuery
	fun slideUp(): JQuery
	fun hover(handlerInOut: Element.() -> Unit): JQuery
	fun hover(handlerIn: Element.() -> Unit, handlerOut: Element.() -> Unit): JQuery
	fun next(): JQuery
	fun parent(): JQuery
	fun `val`(): String?
}

open external class MouseEvent() {
	val pageX: Double
	val pageY: Double
	fun preventDefault()
	fun isDefaultPrevented(): Boolean
}

external class MouseClickEvent() : MouseEvent {
	val which: Int
}

@JsName("$")
external fun jq(selector: String): JQuery
@JsName("$")
external fun jq(selector: String, context: Element): JQuery
@JsName("$")
external fun jq(callback: () -> Unit): JQuery
@JsName("$")
external fun jq(obj: JQuery): JQuery
@JsName("$")
external fun jq(el: Element): JQuery
@JsName("$")
external fun jq(): JQuery
