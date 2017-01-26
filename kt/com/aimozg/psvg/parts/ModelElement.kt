package com.aimozg.psvg.parts

import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGraphicsElement
import kotlin.dom.appendTo

abstract class ModelElement(ctx: ModelContext,
                            name: String?,
                            val ownOrigin: ModelPoint?,
                            items: List<ItemDeclaration?>) :
		Part(ctx, name, listOf(ownOrigin?.asPosDependency) + items) {
	val ownerElement: ModelElement? = climb<Part> { owner }.firstInstanceOf()
	val origin: TXY get() = (ownOrigin?.calculate() ?: TXY(0.0, 0.0)) + (ownerElement?.origin ?: ctx.origin)
	val graphic: SVGGraphicsElement? by lazy {
		draw()?.also { g ->
			g.setAttribute("data-partid", id.toString())
			ownOrigin?.graphic?.appendTo(g)
			redraw("*",g)
			translate(g)
		}
	}

	override fun update(attr: String) {
		super.update(attr)
		redraw(attr)
		translate(graphic)
	}

	private fun translate(g: SVGGraphicsElement?) {
		val oo = ownOrigin
		if (oo != null) g?.transform?.set(tftranslate(oo.calculate()))
	}

	protected abstract fun draw(): SVGGraphicsElement?
	protected abstract fun redraw(attr: String, graphic:SVGGraphicsElement? = this.graphic)
}