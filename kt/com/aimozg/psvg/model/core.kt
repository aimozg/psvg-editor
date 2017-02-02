@file:JsModule("svgedit-api")

package com.aimozg.psvg.model

import com.aimozg.ktuple.Tuple2
import com.aimozg.ktuple.tup
import com.aimozg.psvg.wrap
import org.w3c.dom.HTMLElement
import tinycolor.TinyColor

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
	MODEL,
	PARAM,
	GROUP,

	POINT,
	HANDLE,
	SEGMENT,
	PATH,
	NODE,

	VALUEFLOAT,
	VALUECOLOR
}
typealias PartDependency = Tuple2<String, ModelElement>
sealed class ItemDeclaration {
	class Instant(val modelElement: ModelElement,
	              val dependency: String? = null) : ItemDeclaration()

	class Deferred(val dep: (ModelElement) -> ItemDeclaration?) : ItemDeclaration()
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
	private val _dependants = ArrayList<PartDependency>()
	val dependants: List<PartDependency> get() = _dependants
	abstract val category: Category

	init {
		ctx.register(this)
		for (icd in items) {
			depend(icd)
		}
	}

	override fun toString(): String = (name?.wrap("\"") ?: "#$id") + " (" + classname + ")"

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
					item._dependants.add(dep tup this)
				}
			}
			is ItemDeclaration.Deferred -> {
				val other = icd.dep

				ctx.queuePostload { depend(other(this)) }
			}
		}
	}
	companion object {
		private var indent = 0
	}
	open fun update(attr: String = "*") {
		/*for (dep in _dependants) {
			if (attr == "*" || dep.i0 == "*" || dep.i0 == attr) {
				//console.log(js("Array")(indent).join(" "),"->",dep.i1.toString(),dep.i0)
				//dep.i1.updated(this, attr)
			}
		}*/
		//indent--
		ctx.updated(this, attr)
	}

	internal abstract fun updated(other: ModelElement, attr: String)

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

abstract class ValueFloat(ctx: Context, name: String?, declarations: List<ItemDeclaration> = emptyList()) : Value<Double>(ctx, name, declarations) {
	override val category: Category = Category.VALUEFLOAT
}
abstract class ValueColor(ctx: Context, name: String?, declarations: List<ItemDeclaration> = emptyList()) : Value<TinyColor>(ctx, name, declarations) {
	override val category: Category = Category.VALUECOLOR
}