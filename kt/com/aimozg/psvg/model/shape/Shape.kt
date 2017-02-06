package com.aimozg.psvg.model.shape

import com.aimozg.psvg.model.Category
import com.aimozg.psvg.model.Context
import com.aimozg.psvg.model.Style
import com.aimozg.psvg.model.VisibleElement

/**
 * Created by aimozg on 07.02.2017.
 * Confidential
 */
abstract class Shape(ctx: Context,
                     name: String?,
                     val style: Style?,
                     items: List<ItemDeclaration?>) :
		VisibleElement(ctx, name, listOf(style?.asStyleDependency) + items) {
	override val category: Category
		get() = Category.SHAPE
}