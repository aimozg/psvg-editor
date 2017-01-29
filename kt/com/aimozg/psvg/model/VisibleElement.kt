package com.aimozg.psvg.model

import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGGraphicsElement
import kotlin.dom.appendTo

abstract class VisibleElement(ctx: Context,
                              name: String?,
                              val ownOrigin: Point?,
                              items: List<ItemDeclaration?>) :
		ModelElement(ctx, name, listOf(ownOrigin?.asPosDependency) + items) {
	val visibleOwner: VisibleElement? = climb<ModelElement> { owner }.firstInstanceOf()
	val origin: TXY get() = (ownOrigin?.calculate() ?: visibleOwner?.origin ?: ctx.origin)
	val graphic: SVGGElement by lazy {
		SVGGElement {
			draw(this)
			classList.add("elem",category.name.toLowerCase())
			setAttribute("data-partid", this@VisibleElement.id.toString())
			ownOrigin?.graphic?.appendTo(this)
			redraw("*", this)
			translate(this)
		}
	}

	override fun updated(other: ModelElement, attr: String) {
		if (other == ownOrigin) {
			translate(graphic)
			update("*")
		}
	}

	override fun update(attr: String) {
		super.update(attr)
		redraw(attr,graphic)
		translate(graphic)
	}

	protected fun translate(g: SVGGraphicsElement?) {
		val oo = ownOrigin
		if (oo != null) g?.transform?.set(tftranslate(oo.calculate()))
	}

	open fun display():SVGGraphicsElement? = null // TODO translate on origin movement
	protected abstract fun draw(g: SVGGElement)
	protected abstract fun redraw(attr: String, g: SVGGElement)
	interface VisualElementJson : ModelElementJson {
		var origin: Point.PointJson?
	}
}
