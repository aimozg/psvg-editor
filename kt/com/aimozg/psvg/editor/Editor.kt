package com.aimozg.psvg.editor

import com.aimozg.psvg.*
import com.aimozg.psvg.d.JSTree
import com.aimozg.psvg.d.JSTreeNodeEvent
import com.aimozg.psvg.d.tfscale
import com.aimozg.psvg.d.withPlugins
import com.aimozg.psvg.model.*
import com.aimozg.psvg.model.point.FixedPoint
import jquery.jq
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.WheelEvent
import org.w3c.dom.svg.SVGGraphicsElement
import kotlin.browser.document

class Editor(
		private val canvasDiv: HTMLElement,
		private val treeDiv: HTMLElement,
		private val previewDivs: Array<HTMLElement>,
		private val objviewDiv: HTMLElement) {
	private var tree: JSTree? = null
	private lateinit var editPane: ModelPane
	private var previews = ArrayList<ModelPane>()
	private var scaledown = ArrayList<SVGGraphicsElement>()

	val model: Model get() = editPane.model

	var zoom: Double
		get() = editPane.zoomfact
		set(value) {
			editPane.zoomfact = value
			for (obj in scaledown) obj.transform.set(tfscale(1.0 / value))
		}

	private var selection: ModelElement? = null

	init {
		document.addEventListener("wheel", { ev ->
			ev as WheelEvent
			if (ev.ctrlKey) {
				val panes = listOf(editPane) + previews
				var t = ev.target
				while (t != null && t is Element) {
					for (p in panes.filter { it.div == t }) {
						ev.preventDefault()
						val steps = if (ev.deltaY < 0) 1.0 else -1.0
						if (p == editPane) zoom *= Math.pow(1.1, steps)
						else p.zoomfact *= Math.pow(1.1, steps)
					}
					t = t.parentElement
				}
			}
		})
		val model = Model(Context(),"unnaned", emptyList())
		editPane = ModelPane(model, DisplayMode.EDIT, canvasDiv,
				SVGPathElement("M -5 0 0 -5 5 0 0 5 z") {
					id = "svgpt_diamond_sm"
					style.any = "inherit"
					scaledown.add(this)
				}, SVGPathElement("M -10 0 0 -10 10 0 0 10 z") {
			id = "svgpt_diamond"
			style.any = "inherit"
			scaledown.add(this)
		}, SVGCircleElement(0, 0, 5) {
			id = "svgpt_circle_sm"
			style.any = "inherit"
			scaledown.add(this)
		}, SVGCircleElement(0, 0, 10) {
			id = "svgpt_circle"
			style.any = "inherit"
			scaledown.add(this)
		}, SVGRectElement(-3, -3, 6, 6) {
			id = "svgpt_box_sm"
			style.any = "inherit"
			scaledown.add(this)
		}, SVGRectElement(-5, -5, 10, 10) {
			id = "svgpt_box"
			style.any = "inherit"
			scaledown.add(this)
		}, SVGUseElement("#svgpt_box") { id = "svg_FixedPoint" },
				SVGUseElement("#svgpt_box_sm") { id = "svg_PointAtProjection" },
				SVGUseElement("#svgpt_box_sm") { id = "svg_PointAtIntersection" },
				SVGUseElement("#svgpt_box_sm") { id = "svg_PointFromNormal" },
				SVGUseElement("#svgpt_diamond") { id = "svg_CuspNode" },
				SVGUseElement("#svgpt_circle") { id = "svg_Flow1Node" },
				SVGUseElement("#svgpt_circle") { id = "svg_SmoothNode" },
				SVGUseElement("#svgpt_box") { id = "svg_SymmetricNode" })
		for (pd in previewDivs) {
			previews.add(ModelPane(model, DisplayMode.VIEW, pd))
		}
	}

	private fun recreateView(model: Model) {
		editPane.model = model

		for (p in previews) p.model = model

		tree?.destroy()
		tree = jq(treeDiv).withPlugins.jstree(jsobject2 {
			it.core = jsobject2 {
				it.data = editPane.model.treeNodeFull().children
				it.check_callback = true
			}
		}).withPlugins.jstree().apply {
			get_container().withPlugins.on("select_node.jstree", { _, data: JSTreeNodeEvent ->
				val id = data.node.id.split("_")
				if (id[0] == "ModelPart") select(editPane.ctx.parts[id[1].toInt()])
			}).withPlugins.on("click dblclick", { _, _: Any? ->
				// console.log(e, this.tree.jstree().get_node(e.target))

			})
			Unit
		}
		editPane.ctx.onUpdate = { rslt ->
			console.log("${rslt.size} elements updated")
			for ((obj, _) in rslt) {
				val id = obj.id
				if (obj is ValueFloat || obj is FixedPoint) {
					for (m in previews) {
						val p = m.ctx.parts[id]
						//console.log(obj,id,p);
						/*if (p is FixedPoint && obj is FixedPoint) {
							p.set(obj.x.get(), obj.y.get())
						} else*/ if (p is ValueFloat && obj is ValueFloat) {
							p.set(obj.get())
						}
						// if (p) p.update();
					}
				}
				tree?.rename_node(obj.treeNodeId(), obj.treeNodeText())
			}
		}
		editPane.eModel.addEventListener("click", { e: Event ->
			var element = e.target as Element?
			while (element != null) {
				val id = element.getAttribute("data-partid")?.toInt()
				if (id != null) {
					val part = editPane.model.ctx.parts[id]
					if (part != null) {
						select(part)
						return@addEventListener
					}
				}
				element = element.parentElement
			}
		})
		select(null)
	}

	fun select(modelElement: ModelElement?) {
		if (selection == modelElement) return
		val model = editPane.model
		var s = selection
		while (s != null && s is VisibleElement && s != model) {
			s.graphic.classList.remove("-selected", "-primary")
			s = s.owner
		}
		tree?.deselect_all(true)
		selection = modelElement
		if (modelElement != null && modelElement is VisibleElement && modelElement != model) {
			tree?.select_node(modelElement.treeNodeId(), true)
			modelElement.graphic.classList.add("-selected", "-primary")
			var p2: ModelElement? = modelElement
			while (p2 != null && p2 is VisibleElement && p2 != model) {
				val g = p2.graphic
				g.classList.add("-selected")
				g.parentElement?.appendChild(g)
				p2 = p2.owner
			}
		}
		objviewDiv.innerHTML = ""
		if (modelElement != null) {
			objviewDiv.appendAll(
					HTMLDivElement {
						className = "partValues"
						for (v in modelElement.children) {
							if (v is Value<*>) appendChild(v.editorElement())
						}
					}, HTMLDivElement {
				className = "partReplace"
				for (v in Context.loadersFor(modelElement.category)) {
					appendChild(HTMLDivElement {
						textContent = v.name
					})
				}
			}
			)
		}

	}

	@JsName("save")
	fun save(): dynamic {
		return editPane.model.save()
	}

	@JsName("loadJson")
	fun loadJson(json: dynamic) {
		recreateView(Context().loadModel(json))
	}

}