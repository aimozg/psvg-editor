package com.aimozg.psvg.model.node

import com.aimozg.ktuple.*
import com.aimozg.psvg.model.*

@Deprecated("Use PathV2")
class NodePath(ctx: Context,
               name: String?,
               val closed: Boolean,
               style: Style,
               val nodes: List<ModelNode>) :
		AbstractPath(ctx, name, nodes.map { it.asDependency(Attribute.ALL) }, style) {

	override fun updated(other: ModelElement, attr: Attribute) {
		super.updated(other, attr)
		if (other is ModelNode && (attr eq Attribute.HANDLE || attr eq Attribute.POS)) update(Attribute.ALL)
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
			arrayOf(TYPE, name, closed, style.save()) + nodes.map { it.save() }

	companion object {
		private const val TYPE = "N"
		val PATH_LOADER = object : PartLoader(Category.PATH, NodePath::class, TYPE,
				JsTypename.OBJECT) {
			override fun loadStrict(ctx: Context, json: PathJson, vararg args: Any?): NodePath {
				return NodePath(ctx,
						json.name,
						json.closed,
						ctx.loadStyle(json.style) ?: Style(ctx,""),
						json.nodes.map { ctx.loadNode(it) })
			}

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): NodePath? {
				if (json is Array<Any?>) {
					val array:Array<Any?> = json
					val a: Tuple5<String, String?, Boolean, dynamic, dynamic> = json
					if (a.i0 != TYPE) return null
					return NodePath(ctx, a.i1, a.i2, a.i3,
							(4..array.size - 1).map { ctx.loadNode(array[it]) })
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
