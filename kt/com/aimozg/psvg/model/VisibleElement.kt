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
			redraw(Attribute.ALL, this)
		}?: SVGGElement {  } else SVGGElement {
			draw(this)
			classList.add("elem",category.name.toLowerCase())
			setAttribute("data-partid", this@VisibleElement.id.toString())
			redraw(Attribute.ALL, this)
		}
	}

	override fun update(attr: Attribute) {
		super.update(attr)
		redraw(attr,graphic)
	}

	override fun remove() {
		graphic.remove()
		super.remove()
	}

	fun export(): SVGGraphicsElement? {
		displayMode = true
		return graphic
	}
	protected open fun display():SVGGraphicsElement? = null
	override fun updated(other: ModelElement, attr: Attribute) {}
	protected open fun draw(g: SVGGraphicsElement) {
		for (child in children) (child as? VisibleElement)?.graphic?.appendTo(g)
	}
	protected fun redraw(attr: Attribute, g: SVGGraphicsElement) {
		if (removed) return
		doRedraw(attr,g)
	}
	protected abstract fun doRedraw(attr: Attribute, g: SVGGraphicsElement)
	interface VisualElementJson : ModelElementJson {
	}
}
