@file:JsModule("svgedit-api")

package com.aimozg.psvg.parts

import com.aimozg.psvg.d.JSTreeNodeInit
import com.aimozg.psvg.jsobject2
import com.aimozg.psvg.wrap
import org.w3c.dom.HTMLElement

enum class JsTypename {
	OBJECT,
	FUNCTION,
	UNDEFINED,
	STRING,
	NUMBER,
	BOOLEAN,
	SYMBOL;

	val value = name.toLowerCase()
	companion object {
		fun of(obj:Any?):JsTypename = valueOf(jsTypeOf(obj).toUpperCase())
	}
}

enum class DisplayMode {
	EDIT,
	VIEW
}

enum class Category {
	POINT,
	NODE,
	PATH,
	MODEL,
	PARAM,
	VALUEFLOAT
}
typealias PartDependency = Pair<String, Part>
sealed class ItemDeclaration {
	class Instant(val part: Part,
	              val dependency: String? = null) : ItemDeclaration()

	class Deferred(val dep: (Part) -> ItemDeclaration) : ItemDeclaration()
}

abstract class Part(
		val ctx: Context,
		val name: String?,
		items: List<ItemDeclaration?>
) {
	val id: Int = ctx.nextId()
	var owner: Part? = null
		private set
	private val _children = ArrayList<Part>()
	val children: List<Part> get() = _children
	protected val dependants = ArrayList<PartDependency>()
	abstract val category: Category
	val asDependency get() = ItemDeclaration.Instant(this, "*")

	init {
		ctx.register(this)
		for (icd in items) {
			depend(icd)
		}
	}

	private fun depend(icd: ItemDeclaration?) {
		if (icd == null) return
		when (icd) {
			is ItemDeclaration.Instant -> {
				val item = icd.part
				if (item.owner == null) {
					item.owner = this
					_children.add(item)
				}
				val dep = icd.dependency
				if (dep != null) {
					item.dependants.add(dep to this)
				}
			}
			is ItemDeclaration.Deferred -> {
				val other = icd.dep
				ctx.queuePostload { depend(other(this)) }
			}
		}
	}

	open fun update(attr: String = "*") {
		for (dep in dependants) {
			if (attr == "*" || dep.first == "*" || dep.first == attr) dep.second.updated(this, attr)
			ctx.updated(this, attr)
		}
	}

	protected abstract fun updated(other: Part, attr: String)

	fun treeNodeSelf(): JSTreeNodeInit = jsobject2 {
		id = treeNodeId()
		text = treeNodeText()
	}

	fun treeNodeFull(): JSTreeNodeInit = treeNodeSelf().apply {
		children = _children.filter { it !is Value<*> }.map(Part::treeNodeFull).toTypedArray()
	}

	fun treeNodeId(): String = "ModelPart_$id"
	val classname = this::class.simpleName?:"Part<?>"

	fun treeNodeText(): String = (name?.wrap("\"") ?: "#$id") + " (" + classname + ")"

	abstract fun save(): dynamic
}

abstract class Value<T>(ctx: Context,
                        name: String?,
                        declarations:List<ItemDeclaration> =emptyList()) : Part(ctx, name, declarations) {
	val asValDependency get() = ItemDeclaration.Instant(this,"val")
	abstract fun get(): T
	abstract fun editorElement(): HTMLElement
}

