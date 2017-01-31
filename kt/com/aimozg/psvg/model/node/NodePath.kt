package com.aimozg.psvg.model.node

import com.aimozg.ktuple.*
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.model.*
import com.aimozg.psvg.model.point.Point

class NodePath(ctx: Context,
               name: String?,
               val closed: Boolean,
               style: dynamic,
               ownOrigin: Point?,
               val nodes: List<ModelNode>) :
		AbstractPath(ctx, name, ownOrigin, nodes.map { it.asDependency }, style) {

	override fun updated(other: ModelElement, attr: String) {
		super.updated(other, attr)
		if (other is ModelNode && (attr == "handle" || attr == "pos" || attr == "*")) update("*")
	}

	override fun toSvgD(): String {
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
			arrayOf(TYPE, name, closed, style ?: jsobject {}, ownOrigin?.save()) + nodes.map { it.save() }

	companion object {
		private const val TYPE = "N"
		val PATH_LOADER = object : PartLoader(Category.PATH, NodePath::class, TYPE,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: PathJson, vararg args: Any?): NodePath {
				return NodePath(ctx,
						json.name,
						json.closed,
						json.style ?: jsobject {},
						ctx.loadPoint(json.origin),
						json.nodes.map { ctx.loadNode(it) })
			}

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): NodePath? {
				if (json is Array<Any?>) {
					val array:Array<Any?> = json
					val a: Tuple5<String, String?, Boolean, dynamic, dynamic> = json
					if (a.i0 != TYPE) return null
					return NodePath(ctx,a.i1,a.i2,a.i3,
							ctx.loadPoint(a.i4),
							(5..array.size-1).map { ctx.loadNode(array[it]) })
				}
				return null
			}
		}
	}
	interface PathJson : AbstractPathJson {
		var closed: Boolean
		var nodes: Array<ModelNode.ModelNodeJson>
	}
}
