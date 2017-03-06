package kotlinx.html.js

import kotlinx.html.*
import kotlinx.html.consumers.onFinalize
import kotlinx.html.dom.JSDOMBuilder
import org.w3c.dom.Document
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement
import kotlin.browser.document


@Suppress("NOTHING_TO_INLINE")
private inline fun SVGElement.setEvent(name: String, noinline callback : (Event) -> Unit) : Unit {
	asDynamic()[name] = callback
}
class JSSVGBuilder<out R : SVGElement>(val document : Document) : TagConsumer<R> {
	private val path = arrayListOf<SVGElement>()
	private var lastLeaved : SVGElement? = null

	override fun onTagStart(tag: Tag) {
		val element: SVGElement = when {
			tag.namespace != null -> document.createElementNS(tag.namespace!!, tag.tagName).asDynamic()
			else -> document.createElement(tag.tagName) as SVGElement
		}

		tag.attributesEntries.forEach {
			element.setAttribute(it.key, it.value)
		}

		if (path.isNotEmpty()) {
			path.last().appendChild(element)
		}

		path.add(element)
	}

	override fun onTagAttributeChange(tag: Tag, attribute: String, value: String?) {
		when {
			path.isEmpty() -> throw IllegalStateException("No current tag")
			path.last().tagName.toLowerCase() != tag.tagName.toLowerCase() -> throw IllegalStateException("Wrong current tag")
			else -> path.last().let { node ->
				if (value == null) {
					node.removeAttribute(attribute)
				} else {
					node.setAttribute(attribute, value)
				}
			}
		}
	}

	override fun onTagEvent(tag: Tag, event: String, value: (Event) -> Unit) {
		when {
			path.isEmpty() -> throw IllegalStateException("No current tag")
			path.last().tagName.toLowerCase() != tag.tagName.toLowerCase() -> throw IllegalStateException("Wrong current tag")
			else -> path.last().setEvent(event, value)
		}
	}

	override fun onTagEnd(tag: Tag) {
		if (path.isEmpty() || path.last().tagName.toLowerCase() != tag.tagName.toLowerCase()) {
			throw IllegalStateException("We haven't entered tag ${tag.tagName} but trying to leave")
		}

		lastLeaved = path.removeAt(path.lastIndex)
	}

	override fun onTagContent(content: CharSequence) {
		if (path.isEmpty()) {
			throw IllegalStateException("No current DOM node")
		}

		path.last().appendChild(document.createTextNode(content.toString()))
	}

	override fun onTagContentEntity(entity: Entities) {
		if (path.isEmpty()) {
			throw IllegalStateException("No current DOM node")
		}

		// stupid hack as browsers doesn't support createEntityReference
		val s = document.createElement("span") as SVGElement
		s.innerHTML = entity.text
		path.last().appendChild(s.childNodes.asList().filter { it.nodeType == Node.TEXT_NODE }.first())

		// other solution would be
//        pathLast().innerHTML += entity.text
	}

	override fun onTagContentUnsafe(block: Unsafe.() -> Unit) {
		with(DefaultUnsafe()) {
			block()

			path.last().innerHTML += toString()
		}
	}

	override fun finalize(): R = lastLeaved?.asR() ?: throw IllegalStateException("We can't finalize as there was no tags")

	@Suppress("UNCHECKED_CAST", "UnsafeCastFromDynamic")
	private fun SVGElement.asR(): R = this.asDynamic()

}


fun Document.createSvgTree() : TagConsumer<SVGElement> = JSSVGBuilder(this)
val TagConsumer<HTMLElement>.svg : TagConsumer<SVGElement> get() = JSSVGBuilder((this as? JSDOMBuilder<*>)?.document?: document)
val SVGSVGElement.create : TagConsumer<SVGElement>
	get() = JSSVGBuilder(ownerDocumentEx)

val SVGElement.append : TagConsumer<SVGElement>
	get() = ownerDocumentEx.createSvgTree().onFinalize { element, partial -> if (!partial) { this@append.appendChild(element) } }

private val SVGElement.ownerDocumentEx: Document
	get() = when {
		this is Document -> this
		else -> ownerDocument ?: throw IllegalStateException("SVGElement has no ownerDocument")
	}