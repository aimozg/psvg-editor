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
		defs: Array<CreateElementAttrs> = arrayOf()
) {
	val ctx: ModelContext
	val eModel: SVGElement
	val model: Model
	private val zoombox: SVGGElement
	private val svg: SVGSVGElement
	private var _zoomfact = 1
	var zoomfact: Int
		get() = _zoomfact
		set(value) {
			_zoomfact = value
			resizeView()
		}

	init {
		ctx = ModelContext(mode)
		model = model1.clone(ctx)
		div.innerHTML = ""
		val width = 100.0
		val height = 100.0
		val x0 = -(width / 2).toInt()
		val y0 = -(height / 2).toInt()
		zoombox = SVGItem("g") as SVGGElement
		svg = SVG(CreateSVGAttrs(
				width = width,
				height = height,
				`class` = "modelpane-$mode",
				items = arrayOf(
						if (defs.isEmpty()) null
						else CreateElementAttrs(
								tag = "defs",
								items = defs
						), CreateElementAttrs(
						tag = "rect",
						attrs = *arrayOf(
								"x" to "-50%",
								"y" to "-50%",
								"height" to "100%",
								"width" to "100%",
								"class" to "viewport"))
				)
		), arrayOf(x0, y0, width, height))
		svg.tabIndex = 0
		div.appendChild(svg)
		eModel = model.display()
		zoombox.appendChild(eModel)
		svg.appendChild(zoombox)
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