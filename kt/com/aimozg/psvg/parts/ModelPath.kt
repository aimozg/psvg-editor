package com.aimozg.psvg.parts

import com.aimozg.psvg.*
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGPathElement

class ModelPath(ctx: ModelContext,
                name: String?,
                ownOrigin: ModelPoint?,
                val nodes:List<ModelNode>,
                val style:dynamic,
                val closed:Boolean):
		ModelElement(ctx,name,ownOrigin,nodes.map { it.asDependency }){
	private var p: SVGPathElement? = null
	override val category = PartCategory.PATH

	override fun draw() = SVGGElement {
		classList.add("elem","path")
		p = SVGPathElement { }
		appendChild(p!!)
		for (node in nodes) appendAll(node.graphic)
	}

	override fun redraw(attr: String, graphic: SVGGraphicsElement?) {
		p?.d = toSvgD()
	}

	override fun updated(other: Part, attr: String) {
		if (other is ModelNode && (attr == "handle" || attr == "pos" || attr == "*")) update("*")
	}
	fun toSvgD():String {
		val pts = nodes.map { it.toDNode() }
		if (pts.isEmpty()) return ""
		val M = "M ${pts[0].p}"
		if (pts.size == 1) return M
		val rslt = pts.mapIndexed { i, p ->
			val next = pts[(i+1)%pts.size]
			val cp1 = p.h2
			val cp2 = next.h1
			val pt3 = next.p
			"C $cp1 $cp2 $pt3"
		}
		if (closed) return M+" "+rslt.joinToString(" ")
		return M+" "+rslt.subList(0,rslt.size-1).joinToString(" ")
	}

	override fun save() = jsobject {
		it.name = name
		it.closed = closed
		it.style = style?: jsobject{}
		it.nodes = nodes.map { it.save() }.toTypedArray()
		it.origin = ownOrigin?.save()
	}
	companion object {
		val PATH_LOADER = object: ModelLoader(PartCategory.PATH,"Path",null,JsTypename.OBJECT) {
			override fun loadStrict(ctx: ModelContext,json:dynamic,vararg args:Any?) =
					ModelPath(ctx,
							json.name,
							ctx.loadPointOrNull(json.origin),
							(json.nodes as Array<dynamic>).map { ctx.loadNode(it) },
							json.style?: jsobject {},
							json.closed)
		}.register()
	}
}
