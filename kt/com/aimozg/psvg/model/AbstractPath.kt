package com.aimozg.psvg.model

import com.aimozg.psvg.Object
import com.aimozg.psvg.SVGPathElement
import com.aimozg.psvg.d
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGPathElement
import kotlin.dom.appendTo

abstract class AbstractPath(ctx: Context,
                            name:String?,
                            ownOrigin: Point?,
                            items:List<ItemDeclaration>, val style: dynamic) :
		VisibleElement(ctx,name,ownOrigin,items) {
	protected var p: SVGPathElement? = null
	override val category = Category.PATH
	abstract fun toSvgD(): String
	override fun draw(g: SVGGElement) {
		p = SVGPathElement { appendTo(g) }
	}

	override fun display() = SVGPathElement {
		graphic
		p = this
		redraw("*", graphic)
		d = toSvgD()
		translate(this)
		val styledef = this@AbstractPath.style
		for (k in Object.keys(styledef)) {
			style.setProperty(k, styledef[k])
		}
	}

	override fun redraw(attr: String, g: SVGGElement) {
		p?.d = toSvgD()
	}

	interface AbstractPathJson : VisualElementJson {
		var style: dynamic
	}
}