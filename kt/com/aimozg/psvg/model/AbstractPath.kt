package com.aimozg.psvg.model

import com.aimozg.psvg.Object
import com.aimozg.psvg.SVGPathElement
import com.aimozg.psvg.d
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGPathElement
import kotlin.dom.appendTo

abstract class AbstractPath(ctx: Context,
                            name: String?,
                            items: List<ItemDeclaration>,
                            val style: dynamic) :
		VisibleElement(ctx, name, items) {
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
		for (k in Object.keys(styledef)) {
			style.setProperty(k, styledef[k])
		}
	}

	override fun redraw(attr: String, g: SVGGraphicsElement) {
		p?.d = toSvgD()
	}

	interface AbstractPathJson : VisualElementJson {
		var style: dynamic
	}
}