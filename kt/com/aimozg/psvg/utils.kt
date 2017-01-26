package com.aimozg.psvg

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
external open class Object

inline fun jsobject(init: (jso:dynamic) -> Unit): dynamic {
	return (Object()).apply {init(this)}
}
inline fun<T> jsobject2(init: T.() -> Unit): T {
	return (Object().asDynamic() as T).apply(init)
}

external fun parseFloat(s: String): Double

fun String.toInt() = parseInt(this)
fun String.toDouble() = parseFloat(this)
fun String.prepend(other:String):String = other+this
fun String.wrap(prefix:String,suffix:String=prefix):String = prefix+this+suffix

fun <T : Any> T.climb(step: T.() -> T?) = generateSequence({ this }, step)
inline fun <TYPE, reified SUBTYPE : TYPE> Sequence<TYPE>.firstInstanceOf() = firstOrNull { it is SUBTYPE } as SUBTYPE?


