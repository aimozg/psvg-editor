package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.Tuple
import com.aimozg.ktuple.Tuple2
import com.aimozg.ktuple.get
import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject
import com.aimozg.psvg.model.*

/**
 * Created by aimozg on 06.02.2017.
 * Confidential
 */
class CopySegment(ctx: Context,
                  name: String?,
                  val ref: String) :
		Segment(ctx, name, listOf(ItemDeclaration.Deferred{
			(ctx.findPart(ref,Category.SEGMENT) as? Segment)?.asDependency(Attribute.ALL)
		})) {
	companion object {
		private const val TYPE = "COPY"
		val SEGMENT_COPY_LOADER = object: PartLoader(Category.SEGMENT,CopySegment::class,TYPE,
				JsTypename.STRING) {
			override fun loadStrict(ctx: Context, json: dynamic, vararg args: Any?) =
					CopySegment(ctx,json.name,json.ref as String)

			override fun loadRelaxed(ctx: Context, json: dynamic, vararg args: Any?): CopySegment? {
				if (json is String) {
					val s:String = json
					if (s[0] == '@') return CopySegment(ctx,null,s.substring(1))
				}
				return null
			}
		}
	}

	val obj get() = ctx.findPart(ref,Category.SEGMENT) as? Segment

	override fun save(): dynamic = if (name == null) "@$ref" else jsobject {
		it.type = TYPE
		it.name = name
		it.ref = ref
	}

	override fun toCmdAndPos(start: TXY): Tuple2<String, TXY> = obj?.toCmdAndPos(start)?: Tuple["",start]

	override fun stop(): TXY = obj?.stop()?:start()

	override fun updated(other: ModelElement, attr: Attribute) {
		super.updated(other, attr)
		if (other == obj) update(attr)
	}
}