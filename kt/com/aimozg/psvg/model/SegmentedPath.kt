package com.aimozg.psvg.model

import com.aimozg.ktuple.*
import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject2

/**
 * Created by aimozg on 29.01.2017.
 * Confidential
 */
class SegmentedPath(
		ctx: Context,
		name: String?,
		ownOrigin: Point?,
		style: dynamic,
		val segments: Iterable<Segment>) :
		AbstractPath(ctx, name, ownOrigin, segments.map { it.asDependency }, style) {
	companion object {
		const private val TYPE = "S"
		val PATH_SEGMENTED_LOADER = object : PartLoader(Category.PATH, SegmentedPath::class.simpleName!!, TYPE) {
			override fun loadStrict(ctx: Context, json: SegmentedPathJson, vararg args: Any?) =
					SegmentedPath(ctx,
							json.name,
							ctx.loadPointOrNull(json.origin),
							json.style,
							json.segments.map { ctx.loadSegment(it) })
		}.register()
	}
	interface SegmentedPathJson : AbstractPathJson {
		var segments: Array<dynamic>
	}

	override fun save(): SegmentedPathJson = jsobject2 {
		it.type = TYPE
		it.name = name
		it.origin = ownOrigin?.save()
		it.style = style
		it.segments = segments.map { it.save() }.toTypedArray()
	}

	override fun toSvgD(): String = segments.fold(mutableListOf<String>() mtup TXY(0,0)) { cmdPos, segment ->
		val (cmd,pos) = segment.toCmdAndPos(cmdPos.i1)
		cmdPos.i0.add(cmd)
		cmdPos.i1 = pos
		cmdPos
	}.i0.joinToString(" ")
}

