@file:JsModule("svgedit-api")

package com.aimozg.psvg.model

import com.aimozg.ktuple.Tuple2
import com.aimozg.ktuple.i0
import com.aimozg.ktuple.i1
import com.aimozg.ktuple.tup
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
	VALUEFLOAT,
	SEGMENT
}
typealias PartDependency = Tuple2<String, ModelElement>
sealed class ItemDeclaration {
	class Instant(val modelElement: ModelElement,
	              val dependency: String? = null) : ItemDeclaration()

	class Deferred(val dep: (ModelElement) -> ItemDeclaration) : ItemDeclaration()
}

abstract class ModelElement(
		val ctx: Context,
		val name: String?,
		items: List<ItemDeclaration?>
) {
	val id: Int = ctx.nextId()
	open var owner: ModelElement? = null
		protected set
	private val _children = ArrayList<ModelElement>()
	val children: List<ModelElement> get() = _children
	protected val dependants = ArrayList<PartDependency>()
	abstract val category: Category

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
				val item = icd.modelElement
				if (item.owner == null) {
					item.owner = this
					_children.add(item)
				}
				val dep = icd.dependency
				if (dep != null) {
					item.dependants.add(dep tup this)
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
			if (attr == "*" || dep.i0 == "*" || dep.i0 == attr) dep.i1.updated(this, attr)
			ctx.updated(this, attr)
		}
	}

	protected abstract fun updated(other: ModelElement, attr: String)

	val classname = this::class.simpleName?:"ModelElement<?>"

	abstract fun save(): dynamic
}

val ModelElement.asDependency get() = ItemDeclaration.Instant(this, "*")
fun ModelElement.asDependency(dependency: String?) = ItemDeclaration.Instant(this,dependency)

abstract class Value<out T>(ctx: Context,
                            name: String?,
                            declarations:List<ItemDeclaration> =emptyList()) : ModelElement(ctx, name, declarations) {
	val asValDependency get() = ItemDeclaration.Instant(this,"val")
	abstract fun get(): T
	abstract fun editorElement(): HTMLElement
}
interface ModelElementJson {
	var type: String
	var name: String?
}

