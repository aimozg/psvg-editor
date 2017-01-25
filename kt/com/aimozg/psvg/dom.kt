package com.aimozg.psvg

import org.w3c.dom.DOMTokenList
import org.w3c.dom.svg.*
import kotlin.browser.document

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */

operator fun DOMTokenList.plusAssign(value:String) = add(value)


const val SVGNS = "http://www.w3.org/2000/svg"

private val svgsvg = document.createElementNS(SVGNS,"svg")

inline fun SVGDefsElement(init: SVGDefsElement.() -> Unit): SVGDefsElement = (document.createElementNS(SVGNS, "defs") as SVGDefsElement).apply(init)
inline fun SVGGElement(init: SVGGElement.() -> Unit): SVGGElement = (document.createElementNS(SVGNS, "g") as SVGGElement).apply(init)
inline fun SVGRectElement(init: SVGRectElement.() -> Unit): SVGRectElement = (document.createElementNS(SVGNS, "rect") as SVGRectElement).apply(init)
inline fun SVGSVGElement(init: SVGSVGElement.() -> Unit): SVGSVGElement = (document.createElementNS(SVGNS, "svg") as SVGSVGElement).apply(init)
inline fun SVGUseElement(init: SVGUseElement.() -> Unit): SVGUseElement = (document.createElementNS(SVGNS, "use") as SVGUseElement).apply(init)

fun SVGAnimatedRect.set(x:Double,y:Double,width:Double,height:Double) {
	baseVal.x = x
	baseVal.y = y
	baseVal.width = width
	baseVal.height = height
}
var SVGAnimatedLength.px: Float
	get() {
		baseVal.convertToSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX)
		return baseVal.valueInSpecifiedUnits
	}
	set(value) {
		baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX, value)
	}
var SVGAnimatedLength.percent: Float
	get() {
		baseVal.convertToSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PERCENTAGE)
		return baseVal.valueInSpecifiedUnits
	}
	set(value) {
		baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PERCENTAGE, value)
	}

fun SVGElement.appendAll(vararg children: SVGElement?) {
	for (child in children) if (child != null) appendChild(child)
}