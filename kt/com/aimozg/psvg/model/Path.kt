package com.aimozg.psvg.model

import com.aimozg.ktuple.*
import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGPathElement
import kotlin.dom.appendTo

class Path(ctx: Context,
           name: String?,
           val closed: Boolean,
           val style: dynamic,
           ownOrigin: Point?,
           val nodes: List<PathNode>) :
		VisibleElement(ctx, name, ownOrigin, nodes.map { it.asDependency }) {
	private var p: SVGPathElement? = null
	override val category = Category.PATH

	override fun draw(g: SVGGElement) {
		p = SVGPathElement { appendTo(g) }
		g.appendAll(nodes.map { it.graphic })
	}

	override fun display() = SVGPathElement {
		graphic
		p = this
		redraw("*",graphic)
		d = toSvgD()
		translate(this)
		val styledef = this@Path.style
		for (k in Object.keys(styledef)) {
			style.setProperty(k, styledef[k])
		}
	}

	override fun redraw(attr: String, g: SVGGElement) {
		p?.d = toSvgD()
	}

	override fun updated(other: ModelElement, attr: String) {
		super.updated(other, attr)
		if (other is PathNode && (attr == "handle" || attr == "pos" || attr == "*")) update("*")
	}

	fun toSvgD(): String {
		val pts = nodes.map { it.toDNode() }
		if (pts.isEmpty()) return ""
		val M = "M ${pts[0].p}"
		if (pts.size == 1) return M
		val rslt = pts.mapIndexed { i, p ->
			val next = pts[(i + 1) % pts.size]
			val cp1 = p.h2
			val cp2 = next.h1
			val pt3 = next.p
			"C $cp1 $cp2 $pt3"
		}
		if (closed) return M + " " + rslt.joinToString(" ")
		return M + " " + rslt.subList(0, rslt.size - 1).joinToString(" ")
	}

	override fun save() =
			arrayOf(name, closed, style ?: jsobject {}, ownOrigin?.save()) + nodes.map { it.save() }

	companion object {
		val PATH_LOADER = object : PartLoader(
				Category.PATH,
				Path::class.simpleName!!,
				null,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?): Path {
				if (json is Array<Any?>) {
					val array:Array<Any?> = json
					val a: Tuple4<String?, Boolean, dynamic, dynamic> = json
					return Path(ctx,a.i0,a.i1,a.i2,
							ctx.loadPointOrNull(a.i3),
							(4..array.size-1).map { ctx.loadNode(array[it]) })
				}
				val json1: PathJson = json
				return Path(ctx,
						json1.name,
						json1.closed,
						json1.style ?: jsobject {},
						ctx.loadPointOrNull(json1.origin),
						json1.nodes.map { ctx.loadNode(it) })
			}
		}.register()
	}
}
interface PathJson : VisualElementJson {
	var closed: Boolean
	var style: dynamic
	var nodes: Array<PathNodeJson>
}