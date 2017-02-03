@file:Suppress("unused", "UNUSED_PARAMETER")

package com.aimozg.psvg.d

import jquery.JQuery
import org.w3c.dom.Element
import org.w3c.dom.events.Event

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */

external interface JSTree {
	fun destroy()
	fun get_container(): JQuery

	fun get_node(obj: String): JSTreeNode
	fun get_node(obj: Element): JSTreeNode
	fun get_node(obj: String, as_dom: Boolean): JQuery
	fun get_node(obj: JQuery, as_dom: Boolean): JQuery
	// load_node
	fun open_node(obj: String) // callback, animation

	fun open_node(obj: Element) // callback, animation
	fun open_node(obj: JQuery) // callback, animation
	// close_node(node[,animation])
	fun toggle_node(obj: String)

	fun toggle_node(obj: Element)
	fun toggle_node(obj: JQuery)
	fun enable_node(obj: String)
	fun enable_node(obj: Element)
	fun enable_node(obj: JQuery)
	fun hide_node(obj: String)
	fun hide_node(obj: Element)
	fun hide_node(obj: JQuery)
	fun show_node(obj: String)
	fun show_node(obj: Element)
	fun show_node(obj: JQuery)
	fun select_node(obj: String, supress_event: Boolean = definedExternally, prevent_open: Boolean = definedExternally)
	fun select_node(obj: Element, supress_event: Boolean = definedExternally, prevent_open: Boolean = definedExternally)
	fun select_node(obj: JQuery, supress_event: Boolean = definedExternally, prevent_open: Boolean = definedExternally)
	fun deselect_node(obj: String) // suppress_event
	fun deselect_node(obj: Element) // suppress_event
	fun deselect_node(obj: JQuery) // suppress_event
	fun select_all(suppress_event: Boolean = definedExternally)
	fun deselect_all(suppress_event: Boolean = definedExternally)
	fun refresh_node(obj: String)
	fun refresh_node(obj: Element)
	fun refresh_node(obj: JQuery)
	fun create_node(par: String, node: String, pos: Number = definedExternally, callback: Function<Any?> = definedExternally): String
	fun create_node(par: Element, node: String, pos: Number = definedExternally, callback: Function<Any?> = definedExternally): String
	fun create_node(par: JQuery, node: String, pos: Number = definedExternally, callback: Function<Any?> = definedExternally): String
	fun create_node(par: String, node: JSTreeNodeInit, pos: Number = definedExternally, callback: Function<Any?> = definedExternally): String
	fun create_node(par: Element, node: JSTreeNodeInit, pos: Number = definedExternally, callback: Function<Any?> = definedExternally): String
	fun create_node(par: JQuery, node: JSTreeNodeInit, pos: Number = definedExternally, callback: Function<Any?> = definedExternally): String
	fun rename_node(obj: String, name: String): Boolean
	fun rename_node(obj: Element, name: String): Boolean
	fun rename_node(obj: JQuery, name: String): Boolean
	fun rename_node(obj: Array<String>, name: String): Boolean
	fun rename_node(obj: Array<Element>, name: String): Boolean
	fun rename_node(obj: Array<JQuery>, name: String): Boolean
	fun delete_node(obj: String, name: String): Boolean
	fun delete_node(obj: Element, name: String): Boolean
	fun delete_node(obj: JQuery, name: String): Boolean
	fun delete_node(obj: Array<String>, name: String): Boolean
	fun delete_node(obj: Array<Element>, name: String): Boolean
	fun delete_node(obj: Array<JQuery>, name: String): Boolean
	fun move_node(obj: String, par: String, pos: String = definedExternally, callback: Function<Any?> = definedExternally)
	fun move_node(obj: Element, par: Element, pos: String = definedExternally, callback: Function<Any?> = definedExternally)
	fun move_node(obj: JQuery, par: JQuery, pos: String = definedExternally, callback: Function<Any?> = definedExternally)
}

external interface JSTreeNode {
	val text: String
	val id: String
	val icon: String
	val parent: String
}

external interface JSTreeNodeInit {
	var text: String
	var id: String?
	var icon: String?
	var parent: String?
	var children: Array<JSTreeNodeInit>?
}

enum class TJSTreeOp {
	create_node,
	rename_node,
	delete_node,
	move_node,
	copy_node
}
/*typealias TJSTreeCheckCallback = (operation: String, node: JSTreeNode, node_parent: JSTreeNode, node_position: Number) -> Boolean*/
external interface JSTreeCoreOptions {
	var data: Array<JSTreeNodeInit>?
	var check_callback: Boolean
}

external interface JSTreeOptions{
	var core: JSTreeCoreOptions
}

external class JSTreeNodeEvent : Event {
	val node: JSTreeNode
}

external interface JSTreePlugin  {
	fun jstree(options: JSTreeOptions): JQuery
	fun jstree(): JSTree
	fun<T> on(type:String,handler:(e: Event, data:T)->Any?): JQuery
}
@Suppress("UNCHECKED_CAST_TO_NATIVE_INTERFACE", "CAST_NEVER_SUCCEEDS")
inline val JQuery.withPlugins: JSTreePlugin
	get() = this as JSTreePlugin