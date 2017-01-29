package com.aimozg.psvg.model

import com.aimozg.psvg.DNode
import com.aimozg.psvg.TXY

abstract class ModelNode(ctx: Context,
                         name: String?,
                         ownOrigin: Point?,
                         items: List<ItemDeclaration?>) :
		VisibleElement(ctx, name, ownOrigin, items) {
	override val category: Category = Category.NODE
	val path get() = owner as NodePath
	val index get() = path.nodes.indexOf(this)
	val first get() = !path.closed && index == 0
	val last get() = !path.closed && index == path.nodes.size - 1
	val prevNode get() = path.nodes[(index + path.nodes.size - 1) % path.nodes.size]
	val nextNode get() = path.nodes[(index + 1) % path.nodes.size]
	val asPosDependency get() = ItemDeclaration.Instant(this, "pos")

	abstract fun center(): TXY
	abstract fun toDNode(): DNode
	interface ModelNodeJson : VisualElementJson {

	}
}
