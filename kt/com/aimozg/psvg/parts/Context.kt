package com.aimozg.psvg.parts

import com.aimozg.psvg.PartLoader
import com.aimozg.psvg.TXY

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
class Context() {
	private val _parts = hashMapOf<Int, Part>()
	private var id = 0
	private val postloadQueue = ArrayList<() -> Any?>()

	var onUpdate: (part: Part, attr: String) -> Unit = { _, _ -> }
	val origin: TXY = TXY(0, 0)
	val parts: Map<Int, Part> = _parts

	internal fun nextId(): Int = id++
	internal fun register(part: Part) {
		_parts[part.id] = part
	}

	fun updated(part: Part, attr: String) {
		onUpdate(part, attr)
	}

	private fun loadPart(cat: Category, json: dynamic, vararg args: Any?): Part {
		val loaders = loaders[cat] ?: LoaderLib()
		val type = JsTypename.of(json)
		if (type == JsTypename.OBJECT) {
			val loader = loaders.bytypefield[json["type"]]
			if (loader != null) return loader.loadStrict(this, json, *args)
		}
		val jtloaders = loaders.byjstype[type]
		for (loader in jtloaders ?: emptyList<PartLoader>()) {
			val ele = loader.loadRelaxed(this, json, *args)
			if (ele != null) return ele
		}
		error("No loader for $cat $type " + JSON.stringify(json))
	}

	fun findPoint(name: String) = parts.values.find { it is Point && it.name == name } as Point?
	fun loadPoint(json: dynamic): Point = loadPart(Category.POINT, json) as Point
	fun loadPointOrNull(json: dynamic): Point? = if (json == null) null else loadPart(Category.POINT, json) as Point
	fun loadNode(json: dynamic): PathNode = loadPart(Category.NODE, json) as PathNode
	fun loadPath(json: dynamic): Path = loadPart(Category.PATH, json) as Path
	fun loadParam(json: dynamic): Parameter = loadPart(Category.PARAM, json) as Parameter
	fun loadFloat(name: String, json: dynamic, def: Number? = null, min: Number = Double.NEGATIVE_INFINITY, max: Number = Double.POSITIVE_INFINITY): ValueFloat = loadPart(Category.VALUEFLOAT, json, name, def, min, max) as ValueFloat
	fun loadModel(json: dynamic): Model {
		val model = Model(this,
				json.name ?: "unnamed",
				null,
				(json.paths as Array<dynamic>?)?.map { loadPath(it) } ?: emptyList(),
				(json.params as Array<dynamic>?)?.map { loadParam(it) } ?: emptyList())
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
}
