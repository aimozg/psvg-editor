package com.aimozg.psvg

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
external open class Object {
	companion object {
		fun keys(o: Object):Array<String>
		fun entries(o: Object):Array<Array<Any?>>
	}
}
fun Object.keys() = Object.keys(this)
fun Object.entries() = Object.entries(this)

inline fun jsobject(init: (jso:dynamic) -> Unit): dynamic {
	return (Object()).also(init)
}
inline fun<T> jsobject2(init: (T) -> Unit): T {
	return (Object().asDynamic() as T).also(init)
}

fun String.prepend(other:String):String = other+this
fun String.wrap(prefix:String,suffix:String=prefix):String = prefix+this+suffix

fun <T : Any> T.climb(step: T.() -> T?) = generateSequence({ this }, step)
inline fun <TYPE, reified SUBTYPE : TYPE> Sequence<TYPE>.firstInstanceOf() = firstOrNull { it is SUBTYPE } as SUBTYPE?

fun<T> Array<T>.sliceFrom(index:Int) = sliceArray(index..size-1)

fun<T:Any> MutableIterable<T>.removeFirstOrNull():T? {
	val i = iterator()
	if (!i.hasNext()) return null
	val ii = i.next()
	i.remove()
	return ii
}
fun<T:Any> MutableIterable<T>.removeFirst():T {
	val i = iterator()
	val ii = i.next()
	i.remove()
	return ii
}