package com.aimozg.psvg

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
external open class Object

inline fun jsobject(init: dynamic.() -> Unit): dynamic {
	return (Object()).apply(init)
}
inline fun<T> jsobject2(init: T.() -> Unit): T {
	return (Object().asDynamic() as T).apply(init)
}
