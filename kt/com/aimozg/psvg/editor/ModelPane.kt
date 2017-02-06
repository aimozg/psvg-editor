package com.aimozg.psvg.editor

import com.aimozg.psvg.*
import com.aimozg.psvg.d.rect_cpy
import com.aimozg.psvg.d.rect_expand
import com.aimozg.psvg.d.rect_scale
import com.aimozg.psvg.model.Context
import com.aimozg.psvg.model.DisplayMode
import com.aimozg.psvg.model.Model
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGLength
import org.w3c.dom.svg.SVGSVGElement
import kotlin.dom.appendTo

class ModelPane(
		model1: Model,
		val mode: DisplayMode,
		val div: HTMLElement,
		vararg defs: SVGElement
) {
	var ctx: Context = Context()
		private set
	private val zoombox: SVGGElement = SVGGElement {
	}
	var eModel: SVGElement = SVGGElement { appendTo(zoombox) }
		private set
	var model: Model = model1.clone(ctx)
		set(value) {
			ctx = Context()
			field = value.clone(ctx)
			eModel.remove()
			eModel = when(mode) {
				DisplayMode.EDIT -> model.graphic
				DisplayMode.VIEW -> model.export()!!
			}
			zoombox.appendChild(eModel)
			resizeView()
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
		var us:String = ""
		addEventListener("mouseenter",{
			ownerDocument?.body?.style?.let {
				us = it.getPropertyValue("user-select")
				it.setProperty("user-select","none")
			}
		})
		addEventListener("mouseleave",{
			ownerDocument?.body?.style?.setProperty("user-select",us)
		})
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

