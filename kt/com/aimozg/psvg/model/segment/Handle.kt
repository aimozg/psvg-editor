package com.aimozg.psvg.model.segment

import com.aimozg.psvg.TXY
import com.aimozg.psvg.model.Context
import com.aimozg.psvg.model.ItemDeclaration
import com.aimozg.psvg.model.VisibleElement
import com.aimozg.psvg.model.asDependency

abstract class Handle(ctx: Context,
                      name: String?,
                      items: List<ItemDeclaration?>) :
		VisibleElement(ctx,name,null,items) {
	val asPosDependency get() = asDependency("pos")
	abstract fun calculate(segment:CubicTo,start:TXY,stop:TXY,cpIndex:Int): TXY
}