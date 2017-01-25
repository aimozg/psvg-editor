@file:JsModule("dom")

package com.aimozg.psvg

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

//	fun escape(s:String):String
//	fun byId(id:String):HTMLElement?
//	fun querySelector(el: Document, selector:String):Array<HTMLElement>
//	fun querySelector(el: HTMLElement, selector: String):Array<HTMLElement>
//	fun querySelector(el: Nothing?, selector: String):Array<HTMLElement>
//export function children(el: HTMLElement|undefined): HTMLElement[]
//export function show(el: HTMLElement|HTMLElement[]|undefined)
//export function hide(el: HTMLElement|HTMLElement[]|null)
external fun clone(el: HTMLElement, ext: ((old: HTMLElement, copy: HTMLElement) -> Unit)?): HTMLElement

//export function traverse(root: Element, deep: (el: Element) => boolean)

external fun traverseAll(root: Node, deep: (n: Node) -> Boolean)

external fun <T : Node> clear(e: T, nodeType: Int?): T

//export function neww(tag: string, clazz?: string): HTMLElement

external fun nodesToElements(nl: NodeList): Array<HTMLElement>

//export function nodesToList(nl: NodeList): Node[]
//export function tokensToList(nl: DOMTokenList): string[]
//export function attrsToList(nl: NamedNodeMap): Attr[]

external interface CreateElementAttrsLite {
	var tag: String?
	var parent: Element?
	var items: Array<out CreateElementAttrs?>?
	var text: String?
	var style: dynamic
	var callback: ((el: Element, attrs: CreateElementAttrsLite) -> Unit)?
	var oninput: ((e: MouseEvent) -> Unit)?
	var onchange: ((e: Event) -> Unit)?
}

external interface CreateElementAttrs : CreateElementAttrsLite {

}

fun CreateElementAttrs(
		tag: String,
		parent: Element? = null,
		items: Array<out CreateElementAttrs?>? = null,
		text: String? = null,
		style: dynamic = null,
		callback: ((el: Element, attrs: CreateElementAttrsLite) -> Unit)? = null,
		oninput: ((e: MouseEvent) -> Unit)? = null,
		onchange: ((e: Event) -> Unit)? = null,
		vararg attrs: Pair<String, Any?>
):CreateElementAttrs = jsobject2 {
	this.tag = tag
	this.parent = parent
	this.items = items
	this.text = text
	this.style = style
	this.callback = callback
	this.oninput = oninput
	this.onchange = onchange
	for (attr in attrs) {
		this.asDynamic()[attr.first] = attr.second
	}
}

external fun createElement(attrs: CreateElementAttrs): HTMLElement
external fun createElement(tag: String, attrs: CreateElementAttrsLite?): HTMLElement

external fun SVGItem(attrs: CreateElementAttrs): SVGElement
external fun SVGItem(tag: String, attrs: CreateElementAttrsLite? = noImpl): SVGElement

external interface CreateSVGAttrs {
	var items: Array<CreateElementAttrs?>
	var width: Number
	var height: Number
	var `class`: String
}

fun CreateSVGAttrs(items: Array<CreateElementAttrs?>,
                   width: Number,
                   height: Number,
                   `class`: String):CreateSVGAttrs = jsobject2 {
	this.items = items
	this.width = width
	this.height = height
	this.`class` = `class`
}

external fun SVG(attrs: CreateSVGAttrs, viewBox: Array<Number>): SVGSVGElement
