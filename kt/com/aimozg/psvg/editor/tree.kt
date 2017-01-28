package com.aimozg.psvg.editor

import com.aimozg.psvg.wrap

fun com.aimozg.psvg.model.ModelElement.treeNodeFull(): com.aimozg.psvg.d.JSTreeNodeInit = treeNodeSelf().apply {
	children = this@treeNodeFull.children.filter { it !is com.aimozg.psvg.model.Value<*> }.map { it.treeNodeFull() }.toTypedArray()
}

fun com.aimozg.psvg.model.ModelElement.treeNodeText(): String = (name?.wrap("\"") ?: "#$id") + " (" + classname + ")"
fun com.aimozg.psvg.model.ModelElement.treeNodeId(): String = "ModelPart_$id"
fun com.aimozg.psvg.model.ModelElement.treeNodeSelf(): com.aimozg.psvg.d.JSTreeNodeInit = com.aimozg.psvg.jsobject2 {
	id = treeNodeId()
	text = treeNodeText()
}