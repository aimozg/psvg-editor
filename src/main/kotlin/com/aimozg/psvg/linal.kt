package com.aimozg.psvg

import com.aimozg.ktuple.*
import org.w3c.dom.DOMPoint
import org.w3c.dom.svg.SVGSVGElement
import kotlin.browser.document
import kotlin.js.Math

/**
 * Created by aimozg on 26.01.2017.
 * Confidential
 */
private val svgsvg = document.createElementNS("http://www.w3.org/2000/svg", "svg") as SVGSVGElement

class TXY(val x: Double, val y: Double) {
	constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())
	constructor(xy: DOMPoint) : this(xy.x, xy.y)
	constructor(xy: Pair<Number, Number>) : this(xy.first.toDouble(), xy.second.toDouble())
	constructor(xy: Tuple2<Number, Number>) : this(xy.i0.toDouble(), xy.i1.toDouble())
	constructor(xy: Array<out Number>) : this(xy[0].toDouble(), xy[1].toDouble())

	override fun toString(): String = "$x,$y"
	@Suppress("NOTHING_TO_INLINE")
	inline operator fun component1() = x

	@Suppress("NOTHING_TO_INLINE")
	inline operator fun component2() = y
}

operator fun TXY.plus(other: TXY) = TXY(x + other.x, y + other.y)
fun TXY.plus(dx: Number, dy: Number) = TXY(x + dx.toDouble(), y + dy.toDouble())
operator fun TXY.minus(other: TXY) = TXY(x - other.x, y - other.y)
operator fun TXY.times(other: Number) = TXY(x * other.toDouble(), y * other.toDouble())
operator fun TXY.div(other: Number) = TXY(x / other.toDouble(), y / other.toDouble())
infix fun TXY.dot(other: TXY) = x * other.x + y * other.y

fun TXY.toDomPoint() = svgsvg.createSVGPoint().also { it.x = x; it.y = y }
val TXY.vnorm get() = x * x + y * y
val TXY.vlength get() = Math.sqrt(vnorm)
fun TXY.toUnit() = this / vlength
fun TXY.rot90() = TXY(y, -x)
fun TXY.rot180() = TXY(-y, -y)
fun TXY.rot270() = TXY(-y, x)
fun TXY.rotate(degrees: Number): TXY {
	if (degrees.toDouble() == 0.0) return this
	val sin = Math.sin(Math.PI * degrees.toDouble() / 180)
	val cos = Math.sqrt(1 - sin * sin)
	return TXY(
			+cos * x + sin * y,
			-sin * x + cos * y)
}

operator fun DOMPoint.minus(other: DOMPoint) = svgsvg.createSVGPoint().also {
	it.x = x - other.x
	it.y = y - other.y
}

fun vlinj(vararg kvs: Tuple2<Number, TXY>): TXY =
		TXY(kvs.map { (k, v) ->
			Tuple[k.toDouble() * v.x, k.toDouble() * v.y]
		}.fold(Tuple[0.0,0.0]) { v1, v2 ->
			Tuple[v1[0] + v2[0], v1[1] + v2[1]] })

fun vnormbisect(ab: TXY, ac: TXY, ablen: Double = ab.vlength, aclen: Double = ac.vlength): TXY {
	if (ablen == 0.0 || aclen == 0.0) return TXY(1, 0)
	return ac * (ablen / aclen) - ab
}

fun smoothHandles(b: TXY, a: TXY, c: TXY, abq: Number = 0.3, acq: Number = 0.3, rot: Number = 0.0): Tuple2<TXY, TXY> {
	val ab = b - a
	val ac = c - a
	val ablen = ab.vlength
	val aclen = ac.vlength
	val dir = vnormbisect(ab, ac, ablen, aclen).toUnit().rotate(rot)
	return Tuple[a - dir * (ablen * abq.toDouble()), a + dir * (aclen * acq.toDouble())]
}

fun fixed2norm(a: TXY, c: TXY, b: TXY): TXY {
	val v1 = b - a
	val v2 = v1.rot90()
	val tgt = c - a
	return solve2(v1, v2, tgt)
}

fun norm2fixed(a: TXY, b: TXY, alpha: Number, beta: Number): TXY {
	val v1 = b - a
	val v2 = v1.rot90()
	return vlinj(Tuple[1,a], Tuple[alpha,v1], Tuple[beta, v2])
}

/**
 * Проекция точки 'p' на линию 'ab'.
 */
fun ptproj(a: TXY, b: TXY, p: TXY): TXY {
	val ab = b - a
	val ab1 = ab.toUnit()
	val ap = p - a
	val n = ap dot ab1
	return a + ab1 * n
}

class Vector3(val x: Double, val y: Double, val z: Double) {
	constructor(x: Number, y: Number, z: Number) : this(x.toDouble(), y.toDouble(), z.toDouble())
	constructor(xyz: DOMPoint) : this(xyz.x, xyz.y, xyz.z)
	constructor(xyz: Triple<Number, Number, Number>) : this(xyz.first.toDouble(), xyz.second.toDouble(), xyz.third.toDouble())
	constructor(xyz: Array<out Number>) : this(xyz[0].toDouble(), xyz[1].toDouble(), xyz[2].toDouble())
	constructor(xyz: Tuple3<Number, Number, Number>) : this(xyz.i0, xyz.i1, xyz.i2)

	override fun toString(): String = "$x,$y,$z"
}

operator fun Vector3.plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
operator fun Vector3.minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
operator fun Vector3.times(other: Number) = Vector3(x * other.toDouble(), y * other.toDouble(), z * other.toDouble())
operator fun Vector3.div(other: Number) = Vector3(x / other.toDouble(), y / other.toDouble(), z / other.toDouble())
@Suppress("NOTHING_TO_INLINE")
inline operator fun Vector3.get(index: Int) = when (index) {
	0 -> x
	1 -> y
	2 -> z
	else -> error("Vector3.get($index)")
}

fun solve2(v1: TXY, v2: TXY, vc: TXY) = solve2(v1.x, v2.x, vc.x, v1.y, v2.y, vc.y)
/**
 * a*v1x + b*v2x = cx
 * a*v1y + b*v2y = cy
 */
fun solve2(v1x: Number, v2x: Number, cx: Number, v1y: Number, v2y: Number, cy: Number) = solve2(Vector3(v1x, v2x, cx), Vector3(v1y, v2y, cy))

fun solve2(eq1: Vector3, eq2: Vector3): TXY {
	val m = arrayOf(eq1, eq2)
	//  a*m00 + b*m01 = m02
	//  a*m10 + b*m11 = m12
	if (m[0][0] == 0.0) {
		//        + b*m01 = m02
		//  a*m10 + b*m11 = m12
		val b = m[0][2] / m[0][1]
		//  a*m10         = m12 - b*m11
		val a = (m[1][2] - b * m[1][1]) / m[1][0]
		return TXY(a, b)
	}
	m[0] = m[0] * (1 / m[0][0])
	//  a     + b*m01 = m02
	//  a*m10 + b*m11 = m12
	if (m[1][0] != 0.0) {
		m[1] = m[1] - m[0] * m[1][0]
	}
	//  a     + b*m01 = m02
	//          b*m11 = m12
	m[1] = m[1] / m[1][1]
	//  a     + b*m01 = m02
	//          b     = m12
	m[0] = m[0] - m[1] * m[0][1]
	//  a             = m02
	//          b     = m12
	return TXY(m[0][2], m[1][2])
}

class LineFormula(val f: Double, val g: Double, val h: Double) {
	override fun toString() = "$f*x+$g*y=$h"

	constructor(f: Number, g: Number, h: Number) : this(f.toDouble(), g.toDouble(), h.toDouble())

	@Suppress("NOTHING_TO_INLINE")
	inline operator fun component1() = f

	@Suppress("NOTHING_TO_INLINE")
	inline operator fun component2() = g

	@Suppress("NOTHING_TO_INLINE")
	inline operator fun component3() = h

	companion object {
		/**
		 * @returns line from two points,
		 * possible forms: [1,g,h], [f,1,h], [1,0,h], [0,1,h]
		 */
		fun fromPoints(pt1: TXY, pt2: TXY): LineFormula {
			val (ax, ay) = pt1
			val (bx, by) = pt2
			// f*x + g*y = h
			val dx = bx - ax
			val dy = by - ay
			if (dx == 0.0) return LineFormula(1, 0, ax) // ax == bx  ->  x(y) = ax
			if (dy == 0.0) return LineFormula(0, 1, ay) // ay == by  ->  y(x) = ay
			// f*dx + g*dy = 0
			// ^ underedfined so we take either of
			//   dx + g*dy = 0  or
			// f*dx +   dy = 0
			if (Math.abs(dx) > Math.abs(dy)) {
				val g = -dx / dy
				//    x +  g*y = h
				return LineFormula(1, g, ax + g * ay)
			} else {
				val f = -dy / dx
				// f*x + y = h
				return LineFormula(f, 1, f * ax + ay)
			}
		}
	}
}

fun intersection(l1: LineFormula, l2: LineFormula): TXY {
	val (f1, g1, h1) = l1
	val (f2, g2, h2) = l2
	return solve2(f1, g1, h1, f2, g2, h2)
}

fun v22intersect(a1: TXY, a2: TXY, b1: TXY, b2: TXY) = intersection(LineFormula.fromPoints(a1, a2), LineFormula.fromPoints(b1, b2))
