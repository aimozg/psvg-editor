@file:JsModule("svg")
package com.aimozg.psvg.d

import org.w3c.dom.DOMRect
import org.w3c.dom.svg.SVGTransform

/**
 * Created by aimozg on 25.01.2017.
 * Confidential
 */
external fun rect_expand(rect: DOMRect, horiz: Number, vert: Number = definedExternally): DOMRect
external fun rect_scale(rect: DOMRect, horiz: Number, vert: Number = definedExternally): DOMRect
external fun rect_cpy(src: DOMRect, dst: DOMRect = definedExternally): DOMRect
external fun tfscale(scale:Number): SVGTransform
