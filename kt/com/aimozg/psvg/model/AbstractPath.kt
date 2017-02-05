package com.aimozg.psvg.model

import com.aimozg.psvg.SVGPathElement
import com.aimozg.psvg.d
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGPathElement
import kotlin.dom.appendTo

abstract class AbstractPath(ctx: Context,
                            name: String?,
                            items: List<ItemDeclaration>,
                            val style: Style) :
		VisibleElement(ctx, name, listOf(style.asStyleDependency)+items) {
	protected var p: SVGPathElement? = null
	override val category = Category.PATH
	abstract fun toSvgD(): String
	override fun draw(g: SVGGraphicsElement) {
		p = SVGPathElement { appendTo(g) }
		super.draw(g)
	}

	override fun display() = SVGPathElement {
		p = this
		d = toSvgD()
		val styledef = this@AbstractPath.style
		styledef.applyTo(style)
	}

	override fun doRedraw(attr: Attribute, g: SVGGraphicsElement) {
		val p = p
		if (p!=null) {
			if (attr eq Attribute.POS || attr eq Attribute.HANDLE) p.d = toSvgD()
			if (displayMode && attr eq Attribute.STYLE) style.applyTo(p.style)
		}
	}

	interface AbstractPathJson : VisualElementJson {
		var style: dynamic
	}
}