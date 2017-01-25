import dom = require('./dom');
import svg = require("./svg");
require("jstree-css");

import {Model, Part, ModelElement, Value} from "./svgedit/api";
import {ALL_LOADERS} from "./svgedit/_all";
import {FixedPoint} from "./svgedit/ptfixed";
import {updateElement, SVGItem} from "./dom";
import {ModelContext} from "./svgedit/_ctx";
import {ValueFloat} from "./svgedit/vfloat";
import kotlinjs = require("kotlinjs");
import ModelPane = kotlinjs.com.aimozg.psvg.ModelPane;

//noinspection JSUnusedGlobalSymbols
export const importz = {dom, svg, ALL_LOADERS};

export class Editor {
	private tree: JSTree;
	private editPane: ModelPane;
	private previews: ModelPane[] = [];
	private scaledown: SVGTransformable[];

	get model(): Model {
		return this.editPane.model
	}

	selection: Part|null = null;

	constructor(private readonly canvasDiv: HTMLElement,
				private readonly treeDiv: HTMLElement,
				private readonly previewDivs: HTMLElement[],
				private readonly objviewDiv: HTMLElement) {
		this.scaledown = [];
		document.addEventListener('wheel', ev => {
			if (ev.ctrlKey) {
				let panes = [this.editPane].concat(this.previews);
				for (let t: EventTarget|null = ev.target; t && t instanceof Element; t = t.parentElement) {
					for (let p of panes.filter(p => p.div == t)) {
						ev.preventDefault();
						let steps = ev.deltaY < 0 ? 1 : -1;
						let zf = p.zoomfact = (p.zoomfact *= Math.pow(1.1, steps));
						if (p == this.editPane) {
							for (let obj of this.scaledown) obj.transform.baseVal.initialize(svg.tfscale(1.0 / zf));
						}
					}
				}
			}
		});
	}

	recreateView(model: Model) {
		this.editPane = new ModelPane(model, 'edit', this.canvasDiv,
			[SVGItem({
				tag: 'path',
				id: 'svgpt_diamond_sm',
				d: 'M -5 0 0 -5 5 0 0 5 z',
				style: {'any': 'inherit'},
				callback: (e) => this.scaledown.push(e as any as SVGTransformable)
			}), SVGItem({
				tag: 'path',
				id: 'svgpt_diamond',
				d: 'M -10 0 0 -10 10 0 0 10 z',
				style: {'any': 'inherit'},
				callback: (e) => this.scaledown.push(e as any as SVGTransformable)
			}), SVGItem({
				tag: 'circle',
				id: 'svgpt_circle_sm',
				cx: 0, cy: 0, r: 5,
				style: {'any': 'inherit'},
				callback: (e) => this.scaledown.push(e as any as SVGTransformable)
			}), SVGItem({
				tag: 'circle',
				id: 'svgpt_circle',
				cx: 0, cy: 0, r: 10,
				style: {'any': 'inherit'},
				callback: (e) => this.scaledown.push(e as any as SVGTransformable)
			}), SVGItem({
				tag: 'rect',
				id: 'svgpt_box_sm',
				x: -3, y: -3, width: 6, height: 6,
				style: {'any': 'inherit'},
				callback: (e) => this.scaledown.push(e as any as SVGTransformable)
			}), SVGItem({
				tag: 'rect',
				id: 'svgpt_box',
				x: -5, y: -5, width: 10, height: 10,
				style: {'any': 'inherit'},
				callback: (e) => this.scaledown.push(e as any as SVGTransformable)
			}), SVGItem({
				tag: 'use', id: 'svg_FixedPoint', href: '#svgpt_box'
			}), SVGItem({
				tag: 'use', id: 'svg_PointAtProjection', href: '#svgpt_box_sm'
			}), SVGItem({
				tag: 'use', id: 'svg_PointAtIntersection', href: '#svgpt_box_sm'
			}), SVGItem({
				tag: 'use', id: 'svg_PointFromNormal', href: '#svgpt_box_sm'
			}), SVGItem({
				tag: 'use', id: 'svg_CuspNode', href: '#svgpt_diamond'
			}), SVGItem({
				tag: 'use', id: 'svg_Flow1Node', href: '#svgpt_circle'
			}), SVGItem({
				tag: 'use', id: 'svg_SmoothNode', href: '#svgpt_circle'
			}), SVGItem({
				tag: 'use', id: 'svg_SymmetricNode', href: '#svgpt_box'
			})]);
		for (let pd of this.previewDivs) {
			this.previews.push(new ModelPane(model, 'view', pd));
		}

		if (this.tree) this.tree.destroy();
		this.tree = $(this.treeDiv).jstree({
			core: {
				data: this.editPane.model.treeNodeFull().children,
				check_callback: true
			}
		}).jstree();
		this.tree.get_container().on('select_node.jstree', (e, data: JSTreeNodeEvent) => {
			let id = data.node.id.split('_');
			if (id[0] == 'ModelPart') this.select(this.editPane.ctx.parts[id[1]]);
		}).on('click dblclick', (/*e*/) => {
			// console.log(e, this.tree.jstree().get_node(e.target))
		});
		this.editPane.ctx.onUpdate = (obj: Part) => {
			//console.log(obj);
			let id = obj.id;
			if (obj instanceof ValueFloat || obj instanceof FixedPoint) {
				for (let m of this.previews) {
					let p = m.ctx.parts[id];
					//console.log(obj,id,p);
					if (p instanceof FixedPoint && obj instanceof FixedPoint) {
						p.set(obj.x.get(), obj.y.get());
					} else if (p instanceof ValueFloat && obj instanceof ValueFloat) {
						p.set(obj.get())
					}
					// if (p) p.update();
				}
			}
			this.tree.rename_node(obj.treeNodeId(), obj.treeNodeText());
		};
		this.editPane.eModel.addEventListener('click', (e: MouseEvent) => {
			for (let element = e.target as Element|null; element; element = element.parentElement) {
				let part = this.editPane.model.ctx.parts[element.getAttribute('data-partid') || ''];
				if (part) {
					this.select(part);
					return;
				}
			}
		});
		this.select(null);
	}

	select(part: Part|null) {
		if (this.selection == part) return;
		let model = this.editPane.model;
		for (let s = this.selection; s && s instanceof ModelElement && s != model; s = s.owner) {
			if (s.graphic) s.graphic.classList.remove('-selected', '-primary');
		}
		this.tree.deselect_all(true);
		this.selection = part;
		if (part && part instanceof ModelElement && part != model) {
			this.tree.select_node(part.treeNodeId(), true);
			if (part.graphic) part.graphic.classList.add('-selected', '-primary');
			for (let s: Part = part; s && s instanceof ModelElement && s != model; s = s.owner) {
				const g = s.graphic;
				if (g) {
					g.classList.add('-selected');
					if (g.parentElement) g.parentElement.appendChild(g);
				}
			}
		}
		updateElement(this.objviewDiv, {
			items: [
				{
					tag: 'div', 'class': 'partValues',
					items: (part ? part.children : []).map(v => (v instanceof Value) ? v.editorElement() : undefined)
				}, {
					tag: 'div', 'class': 'partReplace',
					items: (part ? ModelContext.loadersFor(part.category) : []).map(l => ({
						tag: 'div',
						text: 'Replace with ' + l.name
					}))
				}
			],
		});
	}

	public save(): any {
		return this.editPane.model.save();
	}

	public loadJson(json: any) {
		this.recreateView(Model.load(new ModelContext('edit'), json));
	}

	/*loadSvgStruct(el: SVGElement) {
	 interface JPath {
	 name: string;
	 np: NodePath;
	 }
	 let paths = [] as JPath[];
	 dom.traverse(el, (el) => {
	 let type = el.classList.item(0) || 'unknown';
	 //console.log(el.tagName + (el.id ? '#' + el.id : '') + '.' + type);
	 switch (type) {
	 case 'path':
	 let els = svg.dparse(el.getAttribute('d'));
	 let p = {
	 name: (el.getAttribute('inkscape:label') || el.id),
	 np: svg.eltonodes(els)
	 };
	 //console.log(svg.drepr(els) + ' -> ' + svg.noderepr(p.np) + ' -> ' + svg.drepr(svg.nodestoels(p.np)));
	 paths.push(p);
	 break;
	 }
	 return true
	 });
	 //console.log(paths);
	 let m = new Model("edit");
	 for (let p of paths) {
	 m.addPath(new ModelPath(p.name,
	 p.np.nodes.map(node => {
	 return new CuspNode(undefined,
	 new FixedPoint(undefined, node.p),
	 new FixedPoint(undefined, node.h1),
	 new FixedPoint(undefined, node.h2)
	 )
	 }), p.np.z))
	 }
	 this.model = m;
	 this.recreateView();
	 }*/
}


//noinspection JSUnusedGlobalSymbols
export function setup(editorDiv: HTMLElement,
					  treeDiv: HTMLElement,
					  previewDivs: HTMLElement[],
					  objviewDiv: HTMLElement): Editor {
	return new Editor(editorDiv, treeDiv, previewDivs, objviewDiv);
}

