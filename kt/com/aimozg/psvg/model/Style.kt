package com.aimozg.psvg.model

import com.aimozg.psvg.jsobject
import org.w3c.dom.css.CSSStyleDeclaration
import tinycolor.TinyColor

/**
 * Created by aimozg on 02.02.2017.
 * Confidential
 */
class Style(ctx: Context,
            name: String?,
            val stroke: ValueColor? = null,
            val fill: ValueColor? = null,
            val strokeWidth: ValueFloat? = null) :
		ModelElement(ctx,name,
				listOf(stroke?.asValDependency,
						fill?.asValDependency,
						strokeWidth?.asValDependency)){
	override val category: Category = Category.STYLE
	val asStyleDependency get() = asDependency("style")
	companion object {
		private const val TYPE = "style"
		val STYLE_LOADER = object : PartLoader(Category.STYLE,Style::class,TYPE,
				JsTypename.OBJECT, JsTypename.UNDEFINED) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) = Style(ctx,
					json.name,
					ctx.loadColor("stroke",json.data?.stroke),
					ctx.loadColor("fill",json.data?.fill),
					ctx.loadFloatOrNull("strokeWidth",json.data?.`stroke-width`))

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): Style? {
				if (json === null || json === undefined) return Style(ctx,null)
				return Style(ctx,null,
						ctx.loadColor("stroke",json.stroke),
						ctx.loadColor("fill",json.fill),
						ctx.loadFloatOrNull("strokeWidth",json.strokeWidth))
			}
		}
	}
	override fun updated(other: ModelElement, attr: String) {
		if (attr == "val" || attr == "*") update("style")
	}

	fun applyTo(style:CSSStyleDeclaration) {
		stroke?.get()?.styleAs("stroke",style)
		fill?.get()?.styleAs("fill",style)
		strokeWidth?.get()?.styleAs("stroke-width",style)
	}

	override fun save() = jsobject {
		it.stroke = stroke?.save()
		it.fill = fill?.save()
		it.strokeWidth = strokeWidth?.save()
	}
}
fun TinyColor.styleAs(name:String,style:CSSStyleDeclaration) {
	style.setProperty(name,toHexString())
}
fun Double.styleAs(name: String,style:CSSStyleDeclaration) {
	style.setProperty(name,toString())
}