package com.aimozg.psvg.parts

import com.aimozg.psvg.ModelLoader
import com.aimozg.psvg.TXY

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
class ModelContext(val mode: DisplayMode) {
	private val _parts = hashMapOf<Int,Part>()
	private var id = 0
	private val postloadQueue = ArrayList<()->Any?>()

	var onUpdate: (part: Part,attr:String) -> Unit = {_,_->}
	val origin: TXY = TXY(0,0)
	val parts: Map<Int,Part> = _parts

	internal fun nextId():Int = id++
	internal fun register(part: Part) {
		_parts[part.id] = part
	}

	fun updated(part:Part,attr:String) {
		onUpdate(part,attr)
	}
	fun doPostload() {
		for (function in postloadQueue) {
			function()
		}
		postloadQueue.clear()
	}
	private fun loadPart(cat:PartCategory,json: dynamic,vararg args:Any?):Part {
		val loaders = loaders[cat] ?: LoaderLib()
		val type = JsTypename.of(json)
		if (type == JsTypename.OBJECT) {
			val loader = loaders.bytypefield[json["type"]]
			if (loader!=null) return loader.loadStrict(this,json,*args)
		}
		val jtloaders = loaders.byjstype[type]
		for (loader in jtloaders?: emptyList<ModelLoader>()) {
			val ele = loader.loadRelaxed(this,json,*args)
			if (ele != null) return ele
		}
		error("No loader for $cat $type "+ JSON.stringify(json))
	}
	fun findPoint(name:String) = parts.values.find { it is ModelPoint && it.name == name } as ModelPoint?
	fun loadPoint(json:dynamic): ModelPoint = loadPart(PartCategory.POINT,json) as ModelPoint
	fun loadPointOrNull(json:dynamic): ModelPoint? = if (json == null) null else loadPart(PartCategory.POINT,json) as ModelPoint
	fun loadNode(json:dynamic): ModelNode = loadPart(PartCategory.NODE,json) as ModelNode
	fun loadPath(json:dynamic):ModelPath = loadPart(PartCategory.PATH,json) as ModelPath
	fun loadParam(json:dynamic):ModelParam = loadPart(PartCategory.PARAM,json) as ModelParam
	fun loadFloat(name:String,json:dynamic,def:Number?=null,min:Number=Double.NEGATIVE_INFINITY,max:Number=Double.POSITIVE_INFINITY):ValueFloat = loadPart(PartCategory.VALUEFLOAT,json,name,def,min,max) as ValueFloat
	fun queuePostload(code:()->Any?) {
		postloadQueue.add(code)
	}
	internal class LoaderLib {
		val all = ArrayList<ModelLoader>()
		val byjstype = HashMap<JsTypename,ArrayList<ModelLoader>>()
		val bytypefield = HashMap<String?, ModelLoader>()
		fun put(loader: ModelLoader) {
			all += loader
			val tn = loader.typename
			if (tn != null) bytypefield[tn] = loader
			for (jsTypename in loader.objtypes) {
				byjstype.getOrPut(jsTypename,{ArrayList()}).add(loader)
			}
		}
	}
	companion object {
		fun registerLoader(loader: ModelLoader) {
			loaders.getOrPut(loader.cat,{LoaderLib()}).put(loader)
		}
		fun loadersFor(category:PartCategory):List<ModelLoader> = loaders[category]?.all?: emptyList<ModelLoader>()
		private val loaders = HashMap<PartCategory,LoaderLib>()
	}
}
