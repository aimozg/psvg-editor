package com.aimozg.psvg.model

/**
 * Created by aimozg on 28.01.2017.
 * Confidential
 */
class PartLoadException(val cat: Category,
                        val json: dynamic,
                        cause: Throwable? = null) : Throwable(
		(cause?.message?.plus(" caused ")?:"")+
		" failure to load $cat from " + JSON.stringify(json), cause
)