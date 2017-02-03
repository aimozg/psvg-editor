package com.aimozg.psvg.model.segment

import com.aimozg.ktuple.*
import com.aimozg.psvg.TXY
import com.aimozg.psvg.jsobject2
import com.aimozg.psvg.model.*
import com.aimozg.psvg.sliceFrom

/**
 * Created by aimozg on 29.01.2017.
 * Confidential
 */
class SegmentedPath(
		ctx: Context,
		name: String?,
		style: Style,
		val segments: List<Segment>) :
		AbstractPath(ctx, name, segments.map { it.asDependency(Attribute.ALL) }, style) {
	companion object {
		const private val TYPE = "S"
		val PATH_SEGMENTED_LOADER = object : PartLoader(Category.PATH, SegmentedPath::class.simpleName!!, TYPE) {
			override fun loadStrict(ctx: Context, json: SegmentedPathJson, vararg args: Any?) = SegmentedPath(ctx,
					json.name,
					ctx.loadStyle(json.style) ?: Style(ctx,""),
					json.segments?.map { ctx.loadSegment(it) } ?:
							Segment.loadFromStream(ctx, json.stream!!.asSequence()))
		}
	}

	interface SegmentedPathJson : AbstractPathJson {
		var segments: Array<Any?>?
		var stream: Array<Any?>?
	}

	fun start() = segments.firstOrNull()?.start() ?: TXY(0, 0)
	val closed: Boolean = segments.count { it is ZSegment } == 1

	override fun updated(other: ModelElement, attr: Attribute) {
		super.updated(other, attr)
		update(Attribute.ALL)
	}

	override fun save(): SegmentedPathJson = jsobject2 {
		it.type = TYPE
		it.name = name
		it.style = style.save()
		val segdata: List<Any?> = segments.map { it.save() }
		if (segdata.all { it is Array<*> } && segdata.isNotEmpty()) {
			@Suppress("UNCHECKED_CAST")
			segdata as List<Array<Any?>>
			val first = segments[0]
			var stream: List<Any?>? = null
			if (first is MoveTo && segdata.size > 1) {
				val closed = segments.last() is ZSegment
				val last = segments.size - if (closed) 1 else 0
				val sublist = segments.subList(1, last)
				val t = if (sublist.any { it is CubicTo }) "C"
				else if (sublist.all { it is LineTo }) "L"
				else null
				if (t != null) stream = (listOf("M$t" + (if (closed) "Z" else ""),first.pt.save()) +
						segdata.subList(1, last).flatMap { it.sliceFrom(1).asList() })
			}
			it.stream = (stream ?: segdata.flatMap { it.asIterable() }).toTypedArray()
		} else {
			it.segments = segdata.toTypedArray()
		}
	}

	override fun toSvgD(): String = segments.fold(mutableListOf<String>() tup TXY(0, 0)) { cmdPos, segment ->
		val (cmd, pos) = segment.toCmdAndPos(cmdPos.i1)
		cmdPos.i0.add(cmd)
		cmdPos.i0 tup pos
	}.i0.joinToString(" ")
}

