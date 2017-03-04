package com.aimozg.psvg.model.values

import com.aimozg.psvg.HTMLDivElement
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.model.*
import org.w3c.dom.HTMLElement

/**
 * Created by aimozg on 09.02.2017.
 * Confidential.
 */
class ColorRef(ctx: Context,
               name: String?,
               val ref: String) : ValueColor(ctx, name, listOf(ItemDeclaration.Deferred { (it as ColorRef).obj.asValDependency })) {
	companion object {
		private const val COLOR_REF_TYPE = "@"
		val COLOR_REF_LOADER = object : PartLoader(Category.VALUECOLOR, ColorRef::class, COLOR_REF_TYPE,
				JsTypename.STRING) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = ColorRef(ctx, json.name, json.ref)

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): ModelElement? {
				val a: Any? = json
				if (a is String) return ColorRef(ctx, null, a.substring(1))
				val type: Any? = json.type
				if (type is String && type[0] == '@') return ColorRef(ctx, args[0] as? String?, type.substring(1))
				return null
			}
		}
	}

	override fun save(): dynamic = if (name == null) "@$ref" else jsobject {
		it.name = name
		it.type = "@$ref"
	}

	override fun updated(other: ModelElement, attr: Attribute) {
		update(Attribute.VAL)
	}

	val obj: ValueColor by lazy {
		ctx.findPart(ref,Category.VALUECOLOR) as ValueColor? ?: throw NullPointerException("Cannot dereference Color.@$ref")
	}

	override fun get() = obj.get()

	override fun editorElement(): HTMLElement = HTMLDivElement {
	}
}