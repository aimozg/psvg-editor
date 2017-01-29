package com.aimozg.psvg

import org.w3c.dom.svg.*
import kotlin.browser.document

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
const val SVGNS = "http://www.w3.org/2000/svg"

private val svgsvg = document.createElementNS(SVGNS, "svg") as SVGSVGElement

inline fun SVGCircleElement(cx0: Number? = null,
                            cy0: Number? = null,
                            r0: Number? = null,
                            init: SVGCircleElement.() -> Unit): SVGCircleElement = (document.createElementNS(SVGNS, "circle") as SVGCircleElement).apply {
	if (cx0 != null) cx.u = cx0.toFloat()
	if (cy0 != null) cy.u = cy0.toFloat()
	if (r0 != null) r.u = r0.toFloat()
	init()
}

inline fun SVGDefsElement(init: SVGDefsElement.() -> Unit): SVGDefsElement = (document.createElementNS(SVGNS, "defs") as SVGDefsElement).apply(init)
inline fun SVGGElement(init: SVGGElement.() -> Unit): SVGGElement = (document.createElementNS(SVGNS, "g") as SVGGElement).apply(init)
inline fun SVGLineElement(x1: Number? = null,
                          y1: Number? = null,
                          x2: Number? = null,
                          y2: Number? = null,
                          init: SVGLineElement.() -> Unit): SVGLineElement =
		(document.createElementNS(SVGNS, "line") as SVGLineElement).apply {
			if (x1 != null) this.x1.u = x1.toFloat()
			if (y1 != null) this.y1.u = y1.toFloat()
			if (x2 != null) this.x2.u = x2.toFloat()
			if (y2 != null) this.y2.u = y2.toFloat()
			init()
		}

inline fun SVGPathElement(d: String? = null,
                          init: SVGPathElement.() -> Unit): SVGPathElement =
		(document.createElementNS(SVGNS, "path") as SVGPathElement).apply{
			if (d != null) this.d = d
			init()
		}
inline fun SVGRectElement(x0: Number? = null,
                          y0: Number? = null,
                          width0: Number? = null,
                          height0: Number? = null,
                          init: SVGRectElement.() -> Unit): SVGRectElement =
		(document.createElementNS(SVGNS, "rect") as SVGRectElement).apply {
			if (x0 != null) x.u = x0.toFloat()
			if (y0 != null) y.u = y0.toFloat()
			if (width0 != null) width.u = width0.toFloat()
			if (height0 != null) height.u = height0.toFloat()
			init()
		}


inline fun SVGSVGElement(init: SVGSVGElement.() -> Unit): SVGSVGElement = (document.createElementNS(SVGNS, "svg") as SVGSVGElement).apply(init)
inline fun SVGUseElement(href0: String? = null,
                         init: SVGUseElement.() -> Unit): SVGUseElement = (document.createElementNS(SVGNS, "use") as SVGUseElement).apply {
	if (href0 != null) href set href0
	init()
}

var SVGPathElement.d: String
	get() = getAttribute("d") ?: ""
	set(value) = setAttribute("d", value)

fun SVGAnimatedRect.set(x: Double, y: Double, width: Double, height: Double) {
	baseVal.x = x
	baseVal.y = y
	baseVal.width = width
	baseVal.height = height
}

infix fun SVGAnimatedString.set(value: String) {
	baseVal = value
}

var SVGAnimatedLength.percent: Float
	get() {
		baseVal.convertToSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PERCENTAGE)
		return baseVal.valueInSpecifiedUnits
	}
	set(value) {
		baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PERCENTAGE, value)
	}
var SVGAnimatedLength.px: Float
	get() {
		baseVal.convertToSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX)
		return baseVal.valueInSpecifiedUnits
	}
	set(value) {
		baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX, value)
	}
var SVGAnimatedLength.u: Float
	get() {
		return baseVal.value
	}
	set(value) {
		baseVal.value = value
	}

fun<T:SVGElement> T.appendAll(vararg children: SVGElement?):T {
	children
			.filterNotNull()
			.forEach { appendChild(it) }
	return this
}
fun<T:SVGElement> T.appendAll(children: Iterable<SVGElement?>):T {
	children
			.filterNotNull()
			.forEach { appendChild(it) }
	return this
}

fun SVGAnimatedTransformList.set(tf: SVGTransform) = baseVal.initialize(tf)


data class DNode(val p: TXY, val h1: TXY, val h2: TXY)

fun tftranslate(dxy: TXY): SVGTransform = svgsvg.createSVGTransform().apply { setTranslate(dxy.x.toFloat(), dxy.y.toFloat()) }