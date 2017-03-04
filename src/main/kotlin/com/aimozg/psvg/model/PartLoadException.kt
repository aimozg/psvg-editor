package com.aimozg.psvg.model

/**
 * Created by aimozg on 28.01.2017.
 * Confidential
 */
class PartLoadException(val cat: Category,
                        val json: dynamic,
                        cause: dynamic = null) : Throwable(
		when (cause) {
			is String -> cause
			null, undefined -> "Error"
			else -> cause.message ?: "Error"
		} + " when loading $cat from " + try {
			JSON.stringify(json)
		} catch (e: dynamic) {
			console.error("Offending object:", json)
			"[$e] $json"
		}, cause
)