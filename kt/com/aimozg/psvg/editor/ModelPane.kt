package com.aimozg.psvg.editor

import com.aimozg.psvg.*
import com.aimozg.psvg.d.rect_cpy
import com.aimozg.psvg.d.rect_expand
import com.aimozg.psvg.d.rect_scale
import com.aimozg.psvg.parts.DisplayMode
import com.aimozg.psvg.parts.Model
import com.aimozg.psvg.parts.ModelContext
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGLength
import org.w3c.dom.svg.SVGSVGElement

class ModelPane(
		model1: Model,
		val mode: DisplayMode,
		val div: HTMLElement,
		vararg defs: SVGElement
) {
	val ctx: ModelContext = ModelContext(mode)
	val model: Model = model1.clone(ctx)
	val eModel: SVGElement = model.graphic!!
	private val zoombox: SVGGElement = SVGGElement {
		appendChild(eModel)
	}
	private val svg: SVGSVGElement = SVGSVGElement {
		width.px = 100f
		height.px = 100f
		viewBox.set(-50.0, -50.0, 100.0, 100.0)
		classList += "modelpane-"+mode.name.toLowerCase()
		tabIndex = 0
		appendAll(
				if (defs.isEmpty()) null else SVGDefsElement { appendAll(*defs) },
				SVGRectElement {
					x.percent = -50f
					y.percent = -50f
					height.percent = 100f
					width.percent = 100f
					classList += "viewport"
				},
				zoombox
		)
	}
	private var _zoomfact = 1.0
	var zoomfact: Double
		get() = _zoomfact
		set(value) {
			_zoomfact = value
			resizeView()
		}

	init {
		div.innerHTML = ""
		div.appendChild(svg)
		resizeView()
	}

	private fun resizeView() {
		val brect = rect_cpy(zoombox.getBBox())
		rect_expand(brect, 50)
		rect_cpy(brect, svg.viewBox.baseVal)
		rect_scale(brect, _zoomfact)
		svg.width.baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX, brect.width.toFloat())
		svg.height.baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX, brect.height.toFloat())
	}
}

