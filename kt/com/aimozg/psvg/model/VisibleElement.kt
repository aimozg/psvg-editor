package com.aimozg.psvg.model

import com.aimozg.psvg.SVGGElement
import com.aimozg.psvg.climb
import com.aimozg.psvg.firstInstanceOf
import org.w3c.dom.svg.SVGGraphicsElement
import kotlin.dom.appendTo

abstract class VisibleElement(ctx: Context,
                              name: String?,
                              items: List<ItemDeclaration?>) :
		ModelElement(ctx, name, items) {
	val visibleOwner: VisibleElement? = climb<ModelElement> { owner }.firstInstanceOf()
	protected var displayMode = false
		private set
	val graphic: SVGGraphicsElement by lazy {
		if (displayMode) display()?.apply {
			redraw("*", this)
		}?: SVGGElement {  } else SVGGElement {
			draw(this)
			classList.add("elem",category.name.toLowerCase())
			setAttribute("data-partid", this@VisibleElement.id.toString())
			redraw("*", this)
		}
	}

	override fun update(attr: String) {
		super.update(attr)
		redraw(attr,graphic)
	}

	fun export(): SVGGraphicsElement? {
		displayMode = true
		return graphic
	}
	protected open fun display():SVGGraphicsElement? = null
	override fun updated(other: ModelElement, attr: String) {}
	protected open fun draw(g: SVGGraphicsElement) {
		for (child in children) (child as? VisibleElement)?.graphic?.appendTo(g)
	}
	protected abstract fun redraw(attr: String, g: SVGGraphicsElement)
	interface VisualElementJson : ModelElementJson {
	}
}
