package com.aimozg.psvg.model

import com.aimozg.psvg.Object
import com.aimozg.psvg.TXY
import com.aimozg.psvg.entries
import com.aimozg.psvg.model.segment.Handle
import com.aimozg.psvg.model.segment.Segment
import com.aimozg.psvg.sliceFrom
import kotlin.collections.set

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
class Context {
	private val _parts = hashMapOf<Int, ModelElement>()
	private var id = 0
	private val postloadQueue = ArrayList<() -> Any?>()

	var onUpdate: (modelElement: ModelElement, attr: String) -> Unit = { _, _ -> }
	val origin: TXY = TXY(0, 0)
	val parts: Map<Int, ModelElement> = _parts

	internal fun nextId(): Int = id++
	internal fun register(modelElement: ModelElement) {
		_parts[modelElement.id] = modelElement
	}

	fun updated(modelElement: ModelElement, attr: String) {
		onUpdate(modelElement, attr)
	}

	fun findPoint(name: String) = parts.values.find { it is Point && it.name == name } as Point?

	private fun loadPart(cat: Category, json: dynamic, vararg args: Any?): ModelElement {
		val type = JsTypename.of(json)
		try {
			val loaders = loaders[cat] ?: LoaderLib()
			if (type == JsTypename.OBJECT) {
				val loader = loaders.bytypefield[json["type"]]
				if (loader != null) return loader.loadStrict(this, json, *args)
			}
			val jtloaders = loaders.byjstype[type]
			(jtloaders ?: emptyList<PartLoader>())
					.asSequence()
					.mapNotNull { it.loadRelaxed(this, json, *args) }
					.firstOrNull { return it }
		} catch (e:PartLoadException){
			throw e
		} catch (e:dynamic){
			val e2 = PartLoadException(cat,json,e)
			console.error(e2.asDynamic().stack)
			throw e2
		}
		error("No loader for $cat $type " + JSON.stringify(json))
	}

	fun loadAnyPart(name:String,json:dynamic): ModelElement {
		val array:Array<Any?> = json
		val type = Category.valueOf((array[0] as String).toUpperCase())
		if (array.size>2) {
			return loadPart(
					type,
					arrayOf(name) + array.sliceFrom(1),
					name)
		} else return loadPart(type,array[1])
	}

	fun loadPoint(json: dynamic): Point = loadPointOrNull(json)!!
	fun loadPointOrNull(json: dynamic): Point? = if (json == null) null else loadPart(Category.POINT, json) as Point
	fun loadNode(json: dynamic): ModelNode = loadPart(Category.NODE, json) as ModelNode
	fun loadPath(json: dynamic): AbstractPath = loadPart(Category.PATH, json) as AbstractPath
	fun loadSegment(json: dynamic): Segment = loadPart(Category.SEGMENT, json) as Segment
	fun loadHandle(json: dynamic): Handle = loadPart(Category.HANDLE, json) as Handle
	fun loadParam(json: dynamic): Parameter = loadPart(Category.PARAM, json) as Parameter
	fun loadFloat(name: String,
	              json: dynamic,
	              def: Number? = null,
	              min: Number = Double.NEGATIVE_INFINITY,
	              max: Number = Double.POSITIVE_INFINITY): ValueFloat =
			loadPart(Category.VALUEFLOAT, json, name, def, min, max) as ValueFloat
	fun loadModel(json: dynamic): Model {
		val model = Model(this,
				json.name ?: "unnamed",
				null,
				(json.store as Object?)?.entries()?.map { loadAnyPart(it[0] as String,it[1]) } ?: emptyList(),
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
