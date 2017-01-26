package com.aimozg.psvg.parts

import com.aimozg.psvg.DNode
import com.aimozg.psvg.TXY

abstract class ModelNode(ctx: ModelContext,
                         name: String?,
                         ownOrigin: ModelPoint?,
                         items: List<ItemDeclaration?>) :
		ModelElement(ctx, name, ownOrigin, items) {
	override val category: PartCategory = PartCategory.NODE
	val path get() = owner as ModelPath
	val index get() = path.nodes.indexOf(this)
	val first get() = path.closed && index == 0
	val last get() = path.closed && index == path.nodes.size - 1
	val prevNode get() = path.nodes[(index + path.nodes.size - 1) % path.nodes.size]
	val nextNode get() = path.nodes[(index + 1) % path.nodes.size]
	val asPosDependency get() = ItemDeclaration.Instant(this, "pos")

	abstract fun center(): TXY
	abstract fun toDNode(): DNode
}