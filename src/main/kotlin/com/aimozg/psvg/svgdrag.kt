package com.aimozg.psvg

import org.w3c.dom.CustomEvent
import org.w3c.dom.CustomEventInit
import org.w3c.dom.DOMPoint
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGSVGElement
import kotlin.browser.document

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
private val svgsvg = document.createElementNS("http://www.w3.org/2000/svg", "svg") as SVGSVGElement
typealias SVGPoint = DOMPoint
enum class SvgDragEventType {
	SDRAG,
	SDRAGSTART,
	SDRAGSTOP
}

class SvgDragEventDetails(val movement: SVGPoint, val start: SVGPoint)

fun SvgDragEvent(type: SvgDragEventType,
                 movement: SVGPoint,
                 start: SVGPoint): CustomEvent = CustomEvent(type.name.toLowerCase(), CustomEventInit(
		                 detail = SvgDragEventDetails(movement,start),
		                 bubbles = true,
		                 cancelable = true
                 ))


fun unproject(svg: SVGGraphicsElement, x: Number, y: Number): SVGPoint {
	val pt = svgsvg.createSVGPoint()
	pt.x = x.toDouble()
	pt.y = y.toDouble()
	return pt.matrixTransform(svg.getScreenCTM()!!.inverse())
}

class SvgDraggable(val el: SVGGraphicsElement) {
	val start = svgsvg.createSVGPoint()
	var movement = svgsvg.createSVGPoint()
	var dragging = false
	var onmove: ((e: Event) -> Unit)? = null
	fun onmup(event: Event) {
		if ((event as MouseEvent).button.toInt() != 0) return
		stopDragging()
	}

	fun stopDragging() {
		if (dragging) {
			dragging = false
			el.dispatchEvent(SvgDragEvent(SvgDragEventType.SDRAGSTOP, movement, start))
		}
		el.ownerSVGElement?.removeEventListener("mousemove", onmove, false)
		el.ownerDocument?.body?.removeEventListener("mouseup", this::onmup, false)
	}

	fun onmdown(e: Event) {
		e as MouseEvent
		if (e.button != 0.toShort()) return
		val svg = el.ownerSVGElement!!
		el.parentNode?.appendChild(el)
		val mouseStart = unproject(svg, e.clientX, e.clientY)
		val matrix0 = el.transform.baseVal.consolidate()!!.matrix.translate(0.0, 0.0)
		start.x = matrix0.e
		start.y = matrix0.f
		movement.x = 0.0
		movement.y = 0.0
		val startEvent = SvgDragEvent(SvgDragEventType.SDRAGSTART, movement, start)
		el.dispatchEvent(startEvent)
		if (startEvent.defaultPrevented) return
		dragging = true
		el.ownerDocument!!.body!!.addEventListener("mouseup", this::onmup, false)
		onmove = { e ->
			e as MouseEvent
			if (e.buttons.toInt().and(1) == 0) {
				stopDragging()
			} else {
				val current = unproject(svg, e.clientX, e.clientY)
				movement = current - mouseStart
				val unprojectmx = svg.getScreenCTM()!!.inverse().multiply(el.getScreenCTM()!!)
				unprojectmx.e = 0.0
				unprojectmx.f = 0.0
				movement = movement.matrixTransform(unprojectmx)
				val dragEvent = SvgDragEvent(SvgDragEventType.SDRAG, movement, start)
				el.dispatchEvent(dragEvent)
				if (!dragEvent.defaultPrevented) {
					el.transform.baseVal.initialize(el.transform.baseVal.createSVGTransformFromMatrix(matrix0.translate(movement.x, movement.y)))
				}
			}
		}
		svg.addEventListener("mousemove", onmove, false)
	}

	init {
		el.addEventListener("mousedown", this::onmdown, false)
	}
}

fun SVGGraphicsElement.makeDraggable() {
	SvgDraggable(this)
}

fun SVGGraphicsElement.onsdragstart(handler: (Event, SvgDragEventDetails) -> Unit) {
	addEventListener(SvgDragEventType.SDRAGSTART.name.toLowerCase(), { handler(it,(it as CustomEvent).detail as SvgDragEventDetails) })
}

fun SVGGraphicsElement.onsdragstop(handler: (Event, SvgDragEventDetails) -> Unit) {
	addEventListener(SvgDragEventType.SDRAGSTOP.name.toLowerCase(), { handler(it,(it as CustomEvent).detail as SvgDragEventDetails) })
}

fun SVGGraphicsElement.onsdrag(handler: (Event, SvgDragEventDetails) -> Unit) {
	addEventListener(SvgDragEventType.SDRAG.name.toLowerCase(), { handler(it,(it as CustomEvent).detail as SvgDragEventDetails) })
}
