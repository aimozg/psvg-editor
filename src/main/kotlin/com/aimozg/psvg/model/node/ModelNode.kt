package com.aimozg.psvg.model.node

import com.aimozg.psvg.DNode
import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.Category
import com.aimozg.psvg.model.Context
import com.aimozg.psvg.model.VisibleElement

abstract class ModelNode(ctx: Context,
                         name: String?,
                         items: List<ItemDeclaration?>) :
		VisibleElement(ctx, name, items) {
	override val category: Category = Category.NODE
	val path get() = owner as NodePath
	val index get() = path.nodes.indexOf(this)
	val first get() = !path.closed && index == 0
	val last get() = !path.closed && index == path.nodes.size - 1
	val prevNode get() = path.nodes[(index + path.nodes.size - 1) % path.nodes.size]
	val nextNode get() = path.nodes[(index + 1) % path.nodes.size]
	val asPosDependency get() = ItemDeclaration.Instant(this, Attribute.POS)

	abstract fun center(): TXY
	abstract fun toDNode(): DNode
	interface ModelNodeJson : VisualElementJson {

	}
}
