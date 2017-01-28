package com.aimozg.psvg.editor

import com.aimozg.psvg.d.JSTreeNodeInit
import com.aimozg.psvg.jsobject2
import com.aimozg.psvg.model.ModelElement
import com.aimozg.psvg.model.Value
import com.aimozg.psvg.wrap

fun ModelElement.treeNodeFull(): JSTreeNodeInit = treeNodeSelf().apply {
	children = this@treeNodeFull.children.filter { it !is Value<*> }.map { it.treeNodeFull() }.toTypedArray()
}

fun ModelElement.treeNodeText(): String = (name?.wrap("\"") ?: "#$id") + " (" + classname + ")"
fun ModelElement.treeNodeId(): String = "ModelPart_$id"
fun ModelElement.treeNodeSelf(): JSTreeNodeInit = jsobject2 {
	it.id = treeNodeId()
	it.text = treeNodeText()
}