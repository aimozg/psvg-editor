@file:Suppress("unused")

package com.aimozg.psvg

import org.w3c.dom.*
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.dom.svg.SVGElement
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

fun<T:Node> T.appendTo(parent:Node):T {
	parent.appendChild(this)
	return this
}

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class HtmlTagMarker

@HtmlTagMarker
interface DomBuilder

abstract class NodeBuilder<out T:Node>(val element:T) : DomBuilder {
	@Suppress("NOTHING_TO_INLINE")
	inline operator fun<T:Node?> T.unaryPlus():T {
		if (this!=null) element.appendChild(this)
		return this
	}
}
abstract class HtmlElementBuilder<out T:HTMLElement>(element:T) : NodeBuilder<T>(element)
inline fun<T:HTMLElement> T.build(init: HtmlElementBuilder<T>.()->Unit):T {
	(object : HtmlElementBuilder<T>(this){}).init()
	return this
}
abstract class SvgElementBuilder<out T:SVGElement>(element:T) : NodeBuilder<T>(element)
inline fun<T:SVGElement> T.build(init: SvgElementBuilder<T>.()->Unit):T {
	(object : SvgElementBuilder<T>(this){}).init()
	return this
}

inline fun<reified T:HTMLElement> createAndInit(tag:String,vararg attrs:String?):T = document.createElement(tag).apply {
	var i = 0
	val n = attrs.size
	while(i+1<n) {
		val k = attrs[i]
		val v = attrs[i+1]
		if (k!=null && v!=null) setAttribute(k,v)
		i+=2
	}
} as T
class DIV(vararg attrs:String?): HtmlElementBuilder<HTMLDivElement>(createAndInit("div",*attrs))
class INPUT(vararg attrs:String?): HtmlElementBuilder<HTMLInputElement>(createAndInit("input",*attrs))
class LABEL(vararg attrs:String?): HtmlElementBuilder<HTMLLabelElement>(createAndInit("label",*attrs))

inline fun DomBuilder.div(init:DIV.(HTMLDivElement)->Unit):HTMLDivElement = DIV().apply{init(element)}.element
inline fun DomBuilder.label(init:LABEL.(HTMLLabelElement)->Unit):HTMLLabelElement = LABEL().apply{init(element)}.element
inline fun DomBuilder.input(type:String, init:INPUT.(HTMLInputElement)->Unit):HTMLInputElement = INPUT("type",type).apply{init(element)}.element
inline fun DomBuilder.textInput(init:INPUT.(HTMLInputElement)->Unit):HTMLInputElement = INPUT("type","text").apply{init(element)}.element

object html: DomBuilder


