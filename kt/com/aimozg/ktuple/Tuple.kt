@file:Suppress("unused", "NOTHING_TO_INLINE", "UnsafeCastFromDynamic")

package com.aimozg.ktuple

external interface Tuple {
	val length: Int
	companion object
}

@Suppress("UnsafeCastFromDynamic")
inline fun Tuple.asArray(): Array<out Any?> {
	return asDynamic()
}

inline operator fun Tuple.get(index: Int): Any? = asArray()[index]

inline fun Tuple0(): Tuple = emptyArray<Any?>().asDynamic()

// [T0]

external interface Tuple1<out T0> : Tuple {
	@JsName("concat")
	fun <T1> concat(t2: T1): Tuple2<T0, T1>
}

inline fun <T0> Tuple1(i0: T0): Tuple1<T0> = arrayOf(i0).asDynamic()
inline operator fun<T0> Tuple.Companion.get(i0:T0): Tuple1<T0> = Tuple1(i0)

inline operator fun <T0> Tuple1<T0>.component1(): T0 = get(0).asDynamic()
inline val <T0> Tuple1<T0>.i0: T0 get() = get(0).asDynamic()
inline operator fun <T> Tuple1<T>.get(index:Int):T = asArray()[index].asDynamic()

// [T0, T1]

external interface Tuple2<out T0, out T1> : Tuple {
	@JsName("concat")
	fun <T2> concat(t3: T2): Tuple3<T0, T1, T2>
}

inline fun <T0, T1> Tuple2(i0: T0, i1: T1): Tuple2<T0, T1> = arrayOf(i0, i1).asDynamic()
inline infix fun <T0, T1> T0.tup(i2: T1): Tuple2<T0, T1> = arrayOf(this, i2).asDynamic()
inline operator fun<T0,T1> Tuple.Companion.get(i0:T0,i1:T1): Tuple2<T0,T1> = Tuple2(i0,i1)

inline operator fun <T0> Tuple2<T0, *>.component1(): T0 = get(0).asDynamic()
inline operator fun <T1> Tuple2<*, T1>.component2(): T1 = get(1).asDynamic()
inline val <T0> Tuple2<T0, *>.i0: T0  get() = get(0).asDynamic()
inline val <T1> Tuple2<*, T1>.i1: T1  get() = get(1).asDynamic()
inline operator fun <T> Tuple2<T,T>.get(index:Int):T = asArray()[index].asDynamic()

// [T0, T1, T2]

external interface Tuple3<out T0,out T1,out T2> : Tuple {
	@JsName("concat")
	fun <T3> concat(t4: T3): Tuple4<T0, T1, T2, T3>
}

inline fun <T0, T1, T2> Tuple3(i0: T0, i1: T1, i2: T2): Tuple3<T0, T1, T2> = arrayOf(i0, i1, i2).asDynamic()
inline operator fun<T0,T1,T2> Tuple.Companion.get(i0:T0,i1:T1,i2:T2): Tuple3<T0,T1,T2> = Tuple3(i0,i1,i2)

inline operator fun <T0> Tuple3<T0, *, *>.component1(): T0 = get(0).asDynamic()
inline operator fun <T1> Tuple3<*, T1, *>.component2(): T1 = get(1).asDynamic()
inline operator fun <T2> Tuple3<*, *, T2>.component3(): T2 = get(2).asDynamic()
inline val <T0> Tuple3<T0, *, *>.i0: T0  get()= get(0).asDynamic()
inline val <T1> Tuple3<*, T1, *>.i1: T1  get()= get(1).asDynamic()
inline val <T2> Tuple3<*, *, T2>.i2: T2  get()= get(2).asDynamic()
inline operator fun <T> Tuple3<T,T,T>.get(index:Int):T = asArray()[index].asDynamic()

// [T0, T1, T2, T3]

external interface Tuple4<out T0,out T1,out T2,out T3> : Tuple {
	@JsName("concat")
	fun <T4> concat(t5: T4): Tuple5<T0, T1, T2, T3, T4>
}

inline fun <T0, T1, T2, T3> Tuple4(i0: T0, i1: T1, i2: T2, i3: T3): Tuple4<T0, T1, T2, T3> = arrayOf(i0, i1, i2, i3).asDynamic()
inline operator fun<T0,T1,T2,T3> Tuple.Companion.get(i0:T0,i1:T1,i2:T2,i3:T3): Tuple4<T0,T1,T2,T3> = Tuple4(i0,i1,i2,i3)

inline operator fun <T0> Tuple4<T0, *, *, *>.component1(): T0 = get(0).asDynamic()
inline operator fun <T1> Tuple4<*, T1, *, *>.component2(): T1 = get(1).asDynamic()
inline operator fun <T2> Tuple4<*, *, T2, *>.component3(): T2 = get(2).asDynamic()
inline operator fun <T3> Tuple4<*, *, *, T3>.component4(): T3 = get(3).asDynamic()
inline val <T0> Tuple4<T0, *, *, *>.i0: T0 get() = get(0).asDynamic()
inline val <T1> Tuple4<*, T1, *, *>.i1: T1 get() = get(1).asDynamic()
inline val <T2> Tuple4<*, *, T2, *>.i2: T2 get() = get(2).asDynamic()
inline val <T3> Tuple4<*, *, *, T3>.i3: T3 get() = get(3).asDynamic()
inline operator fun <T> Tuple4<T,T,T,T>.get(index:Int):T = asArray()[index].asDynamic()

// [T0, T1, T2, T3, T4]

external interface Tuple5<out T0,out T1,out T2,out T3,out T4> : Tuple

inline fun <T0, T1, T2, T3, T4> Tuple5(i0: T0, i1: T1, i2: T2, i3: T3, i4: T4): Tuple5<T0, T1, T2, T3, T4> =
		arrayOf(i0, i1, i2, i3, i4).asDynamic()
inline operator fun<T0,T1,T2,T3,T4> Tuple.Companion.get(i0:T0,i1:T1,i2:T2,i3:T3,i4:T4): Tuple5<T0,T1,T2,T3,T4> = Tuple5(i0,i1,i2,i3,i4)

inline operator fun <T0> Tuple5<T0, *, *, *, *>.component1(): T0 = get(0).asDynamic()
inline operator fun <T1> Tuple5<*, T1, *, *, *>.component2(): T1 = get(1).asDynamic()
inline operator fun <T2> Tuple5<*, *, T2, *, *>.component3(): T2 = get(2).asDynamic()
inline operator fun <T3> Tuple5<*, *, *, T3, *>.component4(): T3 = get(3).asDynamic()
inline operator fun <T4> Tuple5<*, *, *, *, T4>.component5(): T4 = get(4).asDynamic()
inline val <T0> Tuple5<T0, *, *, *, *>.i0: T0 get() = get(0).asDynamic()
inline val <T1> Tuple5<*, T1, *, *, *>.i1: T1 get() = get(1).asDynamic()
inline val <T2> Tuple5<*, *, T2, *, *>.i2: T2 get() = get(2).asDynamic()
inline val <T3> Tuple5<*, *, *, T3, *>.i3: T3 get() = get(3).asDynamic()
inline val <T4> Tuple5<*, *, *, *, T4>.i4: T4 get() = get(4).asDynamic()
inline operator fun <T> Tuple5<T,T,T,T,T>.get(index:Int):T = asArray()[index].asDynamic()

/*external interface MutableTuple : Tuple
inline operator fun MutableTuple.set(index: Int, value: dynamic) {
	asArray()[index] = value
}
external interface MutableTuple1<T0> : Tuple1<T0>, MutableTuple {
	override fun <T1> concat(t2: T1): MutableTuple2<T0, T1>
}
inline fun <T0> MutableTuple1(i1: T0): MutableTuple1<T0> = arrayOf(i1).asDynamic()
inline var <T0> MutableTuple1<T0>.i0: T0
	get() = get(0).asDynamic()
	set(value:T0) = set(0,value)
inline operator fun<T0> MutableTuple1<T0>.set(index: Int, value: T0) {
	asArray()[index] = value
}
external interface MutableTuple2<T0, T1> : Tuple2<T0, T1>, MutableTuple {
	override fun <T2> concat(t3: T2): MutableTuple3<T0, T1, T2>
}
inline fun <T0, T1> MutableTuple2(i1: T0, i2: T1): MutableTuple2<T0, T1> = arrayOf(i1, i2).asDynamic()
inline var <T0> MutableTuple2<T0, *>.i0: T0
	get() = get(0).asDynamic()
	set(value) = set(0,value)
inline var <T1> MutableTuple2<*, T1>.i1: T1
	get() = get(1).asDynamic()
	set(value) = set(1,value)
inline infix fun <T0, T1> T0.mtup(i2: T1): MutableTuple2<T0, T1> = arrayOf(this, i2).asDynamic()
external interface MutableTuple3<T0, T1, T2> : Tuple3<T0, T1, T2>, MutableTuple {
	override fun <T3> concat(t4: T3): MutableTuple4<T0, T1, T2, T3>
}
inline fun <T0, T1, T2> MutableTuple3(i1: T0, i2: T1, i3: T2): MutableTuple3<T0, T1, T2> = arrayOf(i1, i2, i3).asDynamic()
inline var <T0> MutableTuple3<T0, *, *>.i0: T0
	get() = get(0).asDynamic()
	set(value) = set(0,value)
inline var <T1> MutableTuple3<*, T1, *>.i1: T1
	get() = get(1).asDynamic()
	set(value) = set(1,value)
inline var <T2> MutableTuple3<*, *, T2>.i2: T2
	get() = get(2).asDynamic()
	set(value) = set(2,value)
external interface MutableTuple4<T0, T1, T2, T3> : Tuple4<T0, T1, T2, T3>, MutableTuple {
	override fun <T4> concat(t5: T4): MutableTuple5<T0, T1, T2, T3, T4>
}
inline fun <T0, T1, T2, T3> MutableTuple4(i1: T0, i2: T1, i3: T2, i4: T3): MutableTuple4<T0, T1, T2, T3> = arrayOf(i1, i2, i3, i4).asDynamic()
inline var <T0> MutableTuple4<T0, *, *, *>.i0: T0
	get() = get(0).asDynamic()
	set(value) = set(0,value)
inline var <T1> MutableTuple4<*, T1, *, *>.i1: T1
	get() = get(1).asDynamic()
	set(value) = set(1,value)
inline var <T2> MutableTuple4<*, *, T2, *>.i2: T2
	get() = get(2).asDynamic()
	set(value) = set(2,value)
inline var <T3> MutableTuple4<*, *, *, T3>.i3: T3
	get() = get(3).asDynamic()
	set(value) = set(3,value)
external interface MutableTuple5<T0, T1, T2, T3, T4> : Tuple5<T0, T1, T2, T3, T4>, MutableTuple
inline fun <T0, T1, T2, T3, T4> MutableTuple5(i1: T0, i2: T1, i3: T2, i4: T3, i5: T4): MutableTuple5<T0, T1, T2, T3, T4> = arrayOf(i1, i2, i3, i4, i5).asDynamic()
inline var <T0> MutableTuple5<T0, *, *, *, *>.i0: T0
	get() = get(0).asDynamic()
	set(value) = set(0,value)
inline var <T1> MutableTuple5<*, T1, *, *, *>.i1: T1
	get() = get(1).asDynamic()
	set(value) = set(1,value)
inline var <T2> MutableTuple5<*, *, T2, *, *>.i2: T2
	get() = get(2).asDynamic()
	set(value) = set(2,value)
inline var <T3> MutableTuple5<*, *, *, T3, *>.i3: T3
	get() = get(3).asDynamic()
	set(value) = set(3,value)
inline var <T4> MutableTuple5<*, *, *, *, T4>.i4: T4
	get() = get(4).asDynamic()
	set(value) = set(4,value)*/


