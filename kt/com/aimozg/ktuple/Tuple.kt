@file:Suppress("unused", "NOTHING_TO_INLINE", "UnsafeCastFromDynamic")

package com.aimozg.ktuple

external interface Tuple {
	val length: Int
}

external interface MutableTuple : Tuple

@Suppress("UnsafeCastFromDynamic")
inline fun Tuple.asArray(): Array<Any?> {
	return asDynamic()
}

inline operator fun Tuple.get(index: Int): Any? = asArray()[index]
inline operator fun MutableTuple.set(index: Int, value: dynamic) {
	asArray()[index] = value
}

inline fun Tuple0(): Tuple = emptyArray<Any?>().asDynamic()

// [T1]

external interface Tuple1<T1> : Tuple {
	@JsName("concat")
	fun <T2> concat(t2: T2): Tuple2<T1, T2>
}

external interface MutableTuple1<T1> : Tuple1<T1>, MutableTuple {
	override fun <T2> concat(t2: T2): MutableTuple2<T1, T2>
}


inline fun <reified T1> Tuple1(i1: T1): Tuple1<T1> = arrayOf(i1).asDynamic()
inline fun <reified T1> MutableTuple1(i1: T1): MutableTuple1<T1> = arrayOf(i1).asDynamic()
inline operator fun <T1> Tuple1<T1>.component1(): T1 = get(0).asDynamic()
inline val <T1> Tuple1<T1>.i0: T1 get() = get(0).asDynamic()
inline var <T1> MutableTuple1<T1>.i0: T1
	get() = get(0).asDynamic()
	set(value) = set(0,value)
inline operator fun <T> Tuple1<T>.get(index:Int):T = asArray()[index].asDynamic()

// [T1, T2]

external interface Tuple2<T1, T2> : Tuple {
	@JsName("concat")
	fun <T3> concat(t3: T3): Tuple3<T1, T2, T3>
}

external interface MutableTuple2<T1, T2> : Tuple2<T1, T2>, MutableTuple {
	override fun <T3> concat(t3: T3): MutableTuple3<T1, T2, T3>
}

inline fun <reified T1, reified T2> Tuple2(i1: T1, i2: T2): Tuple2<T1, T2> = arrayOf(i1, i2).asDynamic()
inline fun <reified T1, reified T2> MutableTuple2(i1: T1, i2: T2): MutableTuple2<T1, T2> = arrayOf(i1, i2).asDynamic()
inline infix fun <reified T1, reified T2> T1.tup(i2: T2): Tuple2<T1, T2> = arrayOf(this, i2).asDynamic()
inline infix fun <reified T1, reified T2> T1.mtup(i2: T2): MutableTuple2<T1, T2> = arrayOf(this, i2).asDynamic()
inline operator fun <T1> Tuple2<T1, *>.component1(): T1 = get(0).asDynamic()
inline operator fun <T2> Tuple2<*, T2>.component2(): T2 = get(1).asDynamic()
inline val <T1> Tuple2<T1, *>.i0: T1  get() = get(0).asDynamic()
inline val <T2> Tuple2<*, T2>.i1: T2  get() = get(1).asDynamic()
inline var <T1> MutableTuple2<T1, *>.i0: T1
	get() = get(0).asDynamic()
	set(value) = set(0,value)
inline var <T2> MutableTuple2<*, T2>.i1: T2
	get() = get(1).asDynamic()
	set(value) = set(1,value)
inline operator fun <T> Tuple2<T,T>.get(index:Int):T = asArray()[index].asDynamic()

// [T1, T2, T3]

external interface Tuple3<T1, T2, T3> : Tuple {
	@JsName("concat")
	fun <T4> concat(t4: T4): Tuple4<T1, T2, T3, T4>
}

external interface MutableTuple3<T1, T2, T3> : Tuple3<T1, T2, T3>, MutableTuple {
	override fun <T4> concat(t4: T4): MutableTuple4<T1, T2, T3, T4>
}

inline fun <reified T1, reified T2, reified T3> Tuple3(i1: T1, i2: T2, i3: T3): Tuple3<T1, T2, T3> = arrayOf(i1, i2, i3).asDynamic()
inline fun <reified T1, reified T2, reified T3> MutableTuple3(i1: T1, i2: T2, i3: T3): MutableTuple3<T1, T2, T3> = arrayOf(i1, i2, i3).asDynamic()
inline operator fun <T1> Tuple3<T1, *, *>.component1(): T1 = get(0).asDynamic()
inline operator fun <T2> Tuple3<*, T2, *>.component2(): T2 = get(1).asDynamic()
inline operator fun <T3> Tuple3<*, *, T3>.component3(): T3 = get(2).asDynamic()
inline val <T1> Tuple3<T1, *, *>.i0: T1  get()= get(0).asDynamic()
inline val <T2> Tuple3<*, T2, *>.i1: T2  get()= get(1).asDynamic()
inline val <T3> Tuple3<*, *, T3>.i2: T3  get()= get(2).asDynamic()
inline var <T1> MutableTuple3<T1, *, *>.i0: T1
	get() = get(0).asDynamic()
	set(value) = set(0,value)
inline var <T2> MutableTuple3<*, T2, *>.i1: T2
	get() = get(1).asDynamic()
	set(value) = set(1,value)
inline var <T3> MutableTuple3<*, *, T3>.i2: T3
	get() = get(2).asDynamic()
	set(value) = set(2,value)
inline operator fun <T> Tuple3<T,T,T>.get(index:Int):T = asArray()[index].asDynamic()

// [T1, T2, T3, T4]

external interface Tuple4<T1, T2, T3, T4> : Tuple {
	@JsName("concat")
	fun <T5> concat(t5: T5): Tuple5<T1, T2, T3, T4, T5>
}

external interface MutableTuple4<T1, T2, T3, T4> : Tuple4<T1, T2, T3, T4>, MutableTuple {
	override fun <T5> concat(t5: T5): MutableTuple5<T1, T2, T3, T4, T5>
}

inline fun <reified T1, reified T2, reified T3, reified T4> Tuple4(i1: T1, i2: T2, i3: T3, i4: T4): Tuple4<T1, T2, T3, T4> = arrayOf(i1, i2, i3, i4).asDynamic()
inline fun <reified T1, reified T2, reified T3, reified T4> MutableTuple4(i1: T1, i2: T2, i3: T3, i4: T4): MutableTuple4<T1, T2, T3, T4> = arrayOf(i1, i2, i3, i4).asDynamic()
inline operator fun <T1> Tuple4<T1, *, *, *>.component1(): T1 = get(0).asDynamic()
inline operator fun <T2> Tuple4<*, T2, *, *>.component2(): T2 = get(1).asDynamic()
inline operator fun <T3> Tuple4<*, *, T3, *>.component3(): T3 = get(2).asDynamic()
inline operator fun <T4> Tuple4<*, *, *, T4>.component4(): T4 = get(3).asDynamic()
inline val <T1> Tuple4<T1, *, *, *>.i0: T1 get() = get(0).asDynamic()
inline val <T2> Tuple4<*, T2, *, *>.i1: T2 get() = get(1).asDynamic()
inline val <T3> Tuple4<*, *, T3, *>.i2: T3 get() = get(2).asDynamic()
inline val <T4> Tuple4<*, *, *, T4>.i3: T4 get() = get(3).asDynamic()
inline var <T1> MutableTuple4<T1, *, *, *>.i0: T1
	get() = get(0).asDynamic()
	set(value) = set(0,value)
inline var <T2> MutableTuple4<*, T2, *, *>.i1: T2
	get() = get(1).asDynamic()
	set(value) = set(1,value)
inline var <T3> MutableTuple4<*, *, T3, *>.i2: T3
	get() = get(2).asDynamic()
	set(value) = set(2,value)
inline var <T4> MutableTuple4<*, *, *, T4>.i3: T4
	get() = get(3).asDynamic()
	set(value) = set(3,value)
inline operator fun <T> Tuple4<T,T,T,T>.get(index:Int):T = asArray()[index].asDynamic()

// [T1, T2, T3, T4, T5]

external interface Tuple5<T1, T2, T3, T4, T5> : Tuple
external interface MutableTuple5<T1, T2, T3, T4, T5> : Tuple5<T1, T2, T3, T4, T5>, MutableTuple

inline fun <reified T1, reified T2, reified T3, reified T4, reified T5> Tuple5(i1: T1, i2: T2, i3: T3, i4: T4, i5: T5): Tuple5<T1, T2, T3, T4, T5> = arrayOf(i1, i2, i3, i4, i5).asDynamic()
inline fun <reified T1, reified T2, reified T3, reified T4, reified T5> MutableTuple5(i1: T1, i2: T2, i3: T3, i4: T4, i5: T5): MutableTuple5<T1, T2, T3, T4, T5> = arrayOf(i1, i2, i3, i4, i5).asDynamic()
inline operator fun <T1> Tuple5<T1, *, *, *, *>.component1(): T1 = get(0).asDynamic()
inline operator fun <T2> Tuple5<*, T2, *, *, *>.component2(): T2 = get(1).asDynamic()
inline operator fun <T3> Tuple5<*, *, T3, *, *>.component3(): T3 = get(2).asDynamic()
inline operator fun <T4> Tuple5<*, *, *, T4, *>.component4(): T4 = get(3).asDynamic()
inline operator fun <T5> Tuple5<*, *, *, *, T5>.component5(): T5 = get(4).asDynamic()
inline val <T1> Tuple5<T1, *, *, *, *>.i0: T1 get() = get(0).asDynamic()
inline val <T2> Tuple5<*, T2, *, *, *>.i1: T2 get() = get(1).asDynamic()
inline val <T3> Tuple5<*, *, T3, *, *>.i2: T3 get() = get(2).asDynamic()
inline val <T4> Tuple5<*, *, *, T4, *>.i3: T4 get() = get(3).asDynamic()
inline val <T5> Tuple5<*, *, *, *, T5>.i4: T5 get() = get(4).asDynamic()
inline var <T1> MutableTuple5<T1, *, *, *, *>.i0: T1
	get() = get(0).asDynamic()
	set(value) = set(0,value)
inline var <T2> MutableTuple5<*, T2, *, *, *>.i1: T2
	get() = get(1).asDynamic()
	set(value) = set(1,value)
inline var <T3> MutableTuple5<*, *, T3, *, *>.i2: T3
	get() = get(2).asDynamic()
	set(value) = set(2,value)
inline var <T4> MutableTuple5<*, *, *, T4, *>.i3: T4
	get() = get(3).asDynamic()
	set(value) = set(3,value)
inline var <T5> MutableTuple5<*, *, *, *, T5>.i4: T5
	get() = get(4).asDynamic()
	set(value) = set(4,value)
inline operator fun <T> Tuple5<T,T,T,T,T>.get(index:Int):T = asArray()[index].asDynamic()

