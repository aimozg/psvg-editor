package com.aimozg.psvg.model

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
		fun of(obj:Any?): JsTypename = valueOf(jsTypeOf(obj).toUpperCase())
	}
}

enum class DisplayMode {
	EDIT,
	VIEW
}

enum class Category {
	MODEL,
	GROUP,

	POINT,
	HANDLE,
	SEGMENT,
	PATH,
	NODE,
	SHAPE,

	VALUEFLOAT,
	VALUECOLOR,
	STYLE
}

abstract class ModelElement(
		val ctx: Context,
		val name: String?,
		items: List<ItemDeclaration?>
) {
	data class DependencyDef(val attr: Attribute, val target: ModelElement)
	data class Dependency(val from: ModelElement, val to: ModelElement, val attr: Attribute)

	enum class Attribute {
		ALL,
		VAL,
		POS,
		HANDLE,
		STYLE;

		infix fun eq(other: Attribute) = this == ALL || other == ALL || this == other
	}
	sealed class ItemDeclaration {
		class Instant(val modelElement: ModelElement,
		              val dependency: Attribute? = null) : ItemDeclaration()

		class Deferred(val dep: (ModelElement) -> ItemDeclaration?) : ItemDeclaration()
	}

	val id: Int = ctx.nextId()
	open var owner: ModelElement? = null
		protected set
	private val _children = ArrayList<ModelElement>()
	val children: List<ModelElement> get() = _children
	private val _dependants = ArrayList<DependencyDef>()
	val dependants: List<DependencyDef> get() = _dependants
	abstract val category: Category
	var removed = false; private set

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
					item._dependants.add(DependencyDef(dep,this))
				}
			}
			is ItemDeclaration.Deferred -> {
				val other = icd.dep

				ctx.queuePostload { depend(other(this)) }
			}
		}
	}
	open fun update(attr: Attribute = Attribute.ALL) {
		ctx.updated(this, attr)
	}
	open fun remove() {
		removed = true

		val children = _children.toTypedArray()
		children.forEach { it.removed = true }
		children.forEach { it.remove() }
		_children.clear()

		dependants.toTypedArray().forEach { it.target.removed(this) }
		_dependants.clear()

		owner?.removed(this)
		ctx.removed(this)
	}
	open fun removed(dependency: ModelElement) {
		_children.remove(dependency)
		update(Attribute.ALL)
	}


	internal abstract fun updated(other: ModelElement, attr: Attribute)

	val classname = this::class.simpleName?:"ModelElement<?>"
	open var editor: EditorElement? = null

	abstract fun save(): dynamic
	fun asDependency(dependency: Attribute?) = ItemDeclaration.Instant(this, dependency)
}

abstract class Value<out T>(ctx: Context,
                            name: String?,
                            declarations:List<ItemDeclaration> =emptyList()) : ModelElement(ctx, name, declarations) {
	val asValDependency get() = ItemDeclaration.Instant(this, Attribute.VAL)
	abstract fun get(): T
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
interface EditorElement {
	val container:HTMLElement
	fun notify(value:Any?)
}