package kotlinx.html.js

import kotlinx.html.*
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGGElement

interface SvgContent: Tag
interface SvgTag : CommonAttributeGroupFacade, SvgContent

open class G(initialAttributes : Map<String, String>, override val consumer : TagConsumer<*>) :
		HTMLTag("g", consumer, initialAttributes, "http://www.w3.org/2000/svg", false, false), SvgTag

fun SvgContent.g(classes : String? = null, block : G.() -> Unit = {}) : Unit = G(attributesMapOf("class", classes), consumer).visit(block)
fun TagConsumer<SVGElement>.g(classes : String? = null, block : G.() -> Unit = {}) : SVGGElement = G(attributesMapOf("class", classes), this).visitAndFinalize(this, block) as SVGGElement
