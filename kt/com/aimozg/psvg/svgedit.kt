package com.aimozg.psvg

import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGLength
import org.w3c.dom.svg.SVGSVGElement

class ModelPane(
		model1: Model,
		val mode: DisplayMode,
		val div: HTMLElement,
		defs: Array<SVGElement> = arrayOf()
) {
	val ctx: ModelContext = ModelContext(mode)
	val model: Model = model1.clone(ctx)
	val eModel: SVGElement = model.display()
	private val zoombox: SVGGElement = SVGGElement {
		appendChild(eModel)
	}
	private val svg: SVGSVGElement = SVGSVGElement {
		width.px = 100f
		height.px = 100f
		viewBox.set(-50.0, -50.0, 100.0, 100.0)
		classList += "modelpane-$mode"
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
	private var _zoomfact = 1
	var zoomfact: Int
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
		val brect = rect_cpy(this.zoombox.getBBox())
		rect_expand(brect, 50)
		rect_cpy(brect, this.svg.viewBox.baseVal)
		rect_scale(brect, this._zoomfact)
		this.svg.width.baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX, brect.width.toFloat())
		this.svg.height.baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX, brect.height.toFloat())
	}
}