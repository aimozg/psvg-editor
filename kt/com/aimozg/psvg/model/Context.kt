package com.aimozg.psvg.model

import com.aimozg.psvg.Object
import com.aimozg.psvg.entries
import com.aimozg.psvg.model.node.ModelNode
import com.aimozg.psvg.model.point.Point
import com.aimozg.psvg.model.segment.Handle
import com.aimozg.psvg.model.segment.Segment
import com.aimozg.psvg.sliceFrom
import kotlin.browser.window
import kotlin.collections.set

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
class Context {
	private val _parts = hashMapOf<Int, ModelElement>()
	private var id = 0
	private val postloadQueue = ArrayList<() -> Any?>()
	var onUpdate: ((elAttrs: Collection<Pair<ModelElement, ModelElement.Attribute>>) -> Unit)? = null
	var onRemoved: ((els: Collection<ModelElement>) -> Unit)? = null
	private val removing = mutableSetOf<ModelElement>()
	val parts: Map<Int, ModelElement> = _parts

	fun clear() {
		_parts.clear()
		id = 0
		postloadQueue.clear()
		onUpdate = null
	}

	internal fun nextId(): Int = id++
	internal fun register(modelElement: ModelElement) {
		_parts[modelElement.id] = modelElement
	}

	//private val wave: LinkedHashSet<Pair<ModelElement, String>> = linkedSetOf()
	private val wave: LinkedHashSet<ModelElement.Dependency> = linkedSetOf()
	private var cause: ModelElement? = null

	fun updated(element: ModelElement, attr: ModelElement.Attribute) {
		//console.log("updated ${cause.toString()} -> $element.$attr")
		if (wave.isNotEmpty()) {
			wave.add(ModelElement.Dependency(cause ?: element, element, attr))
			return
		}
		val rslt = hashSetOf<Pair<ModelElement, ModelElement.Attribute>>()
		wave.add(ModelElement.Dependency(element, element, attr))
		while (wave.isNotEmpty()) {
			val first = wave.first()
			val (isrc, i, iattr) = first
			rslt.add(i to iattr)
			(if (iattr == ModelElement.Attribute.ALL) i.dependants else
				i.dependants.filter {
					it.attr eq iattr
				}).forEach {
				val old = cause
				cause = isrc
				it.target.updated(i, iattr)
				cause = old
			}
			wave.remove(first)
		}
		onUpdate?.invoke(rslt)
	}

	fun findPart(name: String, cat: Category) = _parts.values.find { it.name == name && it.category == cat}
	fun findPart(name: String) = _parts.values.find { it.name == name }
	fun findPoint(name: String) = findPart(name,Category.POINT) as Point?

	private fun loadPart(cat: Category, json: dynamic, vararg args: Any?): ModelElement {
		val type = JsTypename.of(json)
		try {
			val loaders = loaders[cat] ?: LoaderLib()
			if (type == JsTypename.OBJECT && json !== null) {
				val loader = loaders.bytypefield[json["type"]]
				if (loader != null) return loader.loadStrict(this, json, *args)
			}
			val jtloaders = loaders.byjstype[type]
			(jtloaders ?: emptyList<PartLoader>())
					.asSequence()
					.mapNotNull { it.loadRelaxed(this, json, *args) }
					.firstOrNull { return it }
		} catch (e: PartLoadException) {
			throw e
		} catch (e: dynamic) {
			console.error(e.stack)
			val e2 = PartLoadException(cat, json, e)
			console.error(e2.asDynamic().stack)
			throw e2
		}
		throw PartLoadException(cat, json, "Not found")
	}

	fun loadAnyPart(name: String?, json: dynamic): ModelElement {
		val array: Array<Any?> = json
		val type = Category.valueOf((array[0] as String).toUpperCase())
		if (array.size > 2) {
			return loadPart(
					type,
					arrayOf(name) + array.sliceFrom(1),
					name)
		} else return loadPart(type, array[1])
	}

	fun loadPoint(json: dynamic): Point? =
			if (json == null) null
			else loadPart(Category.POINT, json) as Point

	fun loadNode(json: dynamic): ModelNode = loadPart(Category.NODE, json) as ModelNode
	fun loadPath(json: dynamic): AbstractPath = loadPart(Category.PATH, json) as AbstractPath
	fun loadSegment(json: dynamic): Segment = loadPart(Category.SEGMENT, json) as Segment
	fun loadHandle(json: dynamic, atStart: Boolean): Handle? =
			if (json == null) null
			else loadPart(Category.HANDLE, json, atStart) as Handle

	fun loadFloatOrNull(name: String,
	                    json: dynamic,
	                    def: Number? = null,
	                    min: Number = Double.NEGATIVE_INFINITY,
	                    max: Number = Double.POSITIVE_INFINITY): ValueFloat? =
			if (json == null) null
			else loadPart(Category.VALUEFLOAT, json, name, def, min, max) as ValueFloat?

	fun loadFloat(name: String,
	              json: dynamic,
	              def: Number? = null,
	              min: Number = Double.NEGATIVE_INFINITY,
	              max: Number = Double.POSITIVE_INFINITY): ValueFloat =
			loadPart(Category.VALUEFLOAT, json, name, def, min, max) as ValueFloat

	fun loadColor(name: String,
	              json: dynamic): ValueColor? =
			if (json == null) null
			else loadPart(Category.VALUECOLOR, json, name) as ValueColor

	fun loadStyle(json: dynamic): Style? =
			loadPart(Category.STYLE, json) as Style

	fun loadModel(json: dynamic): Model {
		val model = Model(this,
				json.name ?: "unnamed",
				(json.items as Object?)?.entries()?.map { loadAnyPart(it[0] as String, it[1]) } ?: emptyList()
		)
		for (function in postloadQueue) function()
		postloadQueue.clear()
		return model
	}

	fun queuePostload(code: () -> Any?) {
		postloadQueue.add(code)
	}

	internal class LoaderLib {
		val all = ArrayList<PartLoader>()
		val byjstype = HashMap<JsTypename, ArrayList<PartLoader>>()
		val bytypefield = HashMap<String?, PartLoader>()
		fun put(loader: PartLoader) {
			all += loader
			val tn = loader.typename
			if (tn != null) bytypefield[tn] = loader
			for (jsTypename in loader.objtypes) {
				byjstype.getOrPut(jsTypename, { ArrayList() }).add(loader)
			}
		}
	}

	companion object {
		fun registerLoader(loader: PartLoader) {
			loaders.getOrPut(loader.cat, { LoaderLib() }).put(loader)
		}

		fun loadersFor(category: Category): List<PartLoader> = loaders[category]?.all ?: emptyList<PartLoader>()
		private val loaders = HashMap<Category, LoaderLib>()
	}

	fun removed(element: ModelElement) {
		_parts.remove(element.id)
		if (removing.isEmpty()) {
			removing.add(element)
			window.setTimeout({
				onRemoved?.invoke(removing.toList())
				removing.clear()
			})
		} else {
			removing.add(element)
		}
	}
}
