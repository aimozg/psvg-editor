@file:JsModule("svg")
package com.aimozg.psvg.d

import org.w3c.dom.DOMRect
import org.w3c.dom.svg.SVGTransform

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
typealias SVGRect = DOMRect
external fun rect_expand(rect: SVGRect, horiz: Number, vert: Number = noImpl): SVGRect
external fun rect_scale(rect: SVGRect, horiz: Number, vert: Number = noImpl): SVGRect
external fun rect_cpy(src: SVGRect, dst: SVGRect = noImpl): SVGRect
external fun tfscale(scale:Number): SVGTransform
