import dom = require('./dom');
import svg = require("./svg");
require("jstree-css");

import {Model, Part, CModelElement, DisplayMode, Value} from "./svgedit/api";
import {ALL_LOADERS} from "./svgedit/_all";
import {FixedPoint} from "./svgedit/ptfixed";
import {CreateElementAttrs, updateElement} from "./dom";
import {ModelContext} from "./svgedit/_ctx";
import {ValueFloat} from "./svgedit/vfloat";

//noinspection JSUnusedGlobalSymbols
export const importz = {dom, svg, ALL_LOADERS};

class ModelPane {
	public readonly model: Model;
	public readonly eModel: SVGElement;
	public readonly ctx: ModelContext;
	private zoombox: SVGGElement;
	private svg: SVGSVGElement;
	private _zoomfact:number=1;
	get zoomfact(): number {
		return this._zoomfact;
	}
	set zoomfact(value: number) {
		this._zoomfact = value;
		this.resizeView();
	}

	constructor(model: Model,
				public readonly mode: DisplayMode,
				public readonly div: HTMLElement,
				defs: (CreateElementAttrs|Element|undefined)[] = []) {
		this.ctx = new ModelContext(mode);
		this.model = model.clone(this.ctx);
		div.innerHTML = '';
		let width = 100;
		let height = 100;
		let x0 = -Math.floor(width / 2);
		let y0 = -Math.floor(height / 2);
		this.svg = dom.SVG({
			width: width,
			height: height,
			'class': 'modelpane-'+mode,
			items: [defs.length == 0 ? undefined : {
					tag: 'defs',
					items: defs
				}, {
				tag: 'rect',
				x: '-50%', y: '-50%',
				height: '100%',
				width: '100%',
				'class': 'viewport'
			}, {
				tag: 'g',
				//transform:'scale('+this.zoomfact+')',
				callback: el => this.zoombox = el as SVGGElement,
				items: []
			}]
		}, [x0, y0, width, height]);
		this.svg.setAttribute('tabindex', '0');
		div.appendChild(this.svg);
		this.eModel = this.model.display();
		this.zoombox.appendChild(this.eModel);
		this.resizeView();
	}

	private resizeView() {
		//svg.tf2list(svg.tfscale(this.zoomfact),this.zoombox.transform.baseVal);
		let brect = svg.rect_cpy(this.zoombox.getBBox());
		svg.rect_expand(brect, 50);
		svg.rect_cpy(brect, this.svg.viewBox.baseVal);
		svg.rect_scale(brect, this._zoomfact);
		this.svg.width.baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX, brect.width);
		this.svg.height.baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX, brect.height);
	}
}

export class Editor {
	private tree: JSTree;
	private editPane: ModelPane;
	private previews: ModelPane[] = [];
	private scaledown: SVGTransformable[];
	get model(): Model { return this.editPane.model }

	selection: Part|null = null;

	constructor(private readonly canvasDiv: HTMLElement,
				private readonly treeDiv: HTMLElement,
				private readonly previewDivs: HTMLElement[],
				private readonly objviewDiv: HTMLElement) {
		this.scaledown = [];
		document.addEventListener('wheel', ev => {
			if (ev.ctrlKey) {
				let panes = [this.editPane].concat(this.previews);
				for (let t:EventTarget|null = ev.target; t && t instanceof Element; t = t.parentElement) {
					for (let p of panes.filter(p=>p.div == t)) {
						ev.preventDefault();
						let steps = ev.deltaY < 0 ? 1 : -1;
						let zf = p.zoomfact = (p.zoomfact*=Math.pow(1.1, steps));
						if (p == this.editPane) {
							for (let obj of this.scaledown) obj.transform.baseVal.initialize(svg.tfscale(1.0 / zf));
						}
					}
				}
			}
		});
	}

	recreateView(model:Model) {
		this.editPane = new ModelPane(model, 'edit', this.canvasDiv,
			[{
				tag: 'path',
				id: 'svgpt_diamond_sm',
				d: 'M -5 0 0 -5 5 0 0 5 z',
				style: {'any': 'inherit'},
				callback: (e) => this.scaledown.push(e as any as SVGTransformable)
			}, {
				tag: 'path',
				id: 'svgpt_diamond',
				d: 'M -10 0 0 -10 10 0 0 10 z',
				style: {'any': 'inherit'},
				callback: (e) => this.scaledown.push(e as any as SVGTransformable)
			}, {
				tag: 'circle',
				id: 'svgpt_circle_sm',
				cx: 0, cy: 0, r: 5,
				style: {'any': 'inherit'},
				callback: (e) => this.scaledown.push(e as any as SVGTransformable)
			}, {
				tag: 'circle',
				id: 'svgpt_circle',
				cx: 0, cy: 0, r: 10,
				style: {'any': 'inherit'},
				callback: (e) => this.scaledown.push(e as any as SVGTransformable)
			}, {
				tag: 'rect',
				id: 'svgpt_box_sm',
				x: -2.5, y: -2.5, width: 5, height: 5,
				style: {'any': 'inherit'},
				callback: (e) => this.scaledown.push(e as any as SVGTransformable)
			}, {
				tag: 'rect',
				id: 'svgpt_box',
				x: -5, y: -5, width: 10, height: 10,
				style: {'any': 'inherit'},
				callback: (e) => this.scaledown.push(e as any as SVGTransformable)
			}, {
				tag: 'use', id: 'svg_FixedPoint', href: '#svgpt_box_sm'
			}, {
				tag: 'use', id: 'svg_CuspNode', href: '#svgpt_diamond'
			}, {
				tag: 'use', id: 'svg_Flow1Node', href: '#svgpt_circle'
			}, {
				tag: 'use', id: 'svg_SmoothNode', href: '#svgpt_circle'
			}, {
				tag: 'use', id: 'svg_SymmetricNode', href: '#svgpt_box'
			}]);
		for (let pd of this.previewDivs) {
			this.previews.push(new ModelPane(model,'view',pd));
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
		for (let s = this.selection; s && s instanceof CModelElement && s != model; s = s.owner) {
			if (s.graphic) s.graphic.classList.remove('-selected', '-primary');
		}
		this.tree.deselect_all(true);
		this.selection = part;
		if (part && part instanceof CModelElement && part != model) {
			this.tree.select_node(part.treeNodeId(), true);
			if (part.graphic) part.graphic.classList.add('-selected', '-primary');
			for (let s = part; s && s instanceof CModelElement && s != model; s = s.owner) {
				const g = s.graphic;
				if (g) {
					g.classList.add('-selected');
					if (g.parentElement) g.parentElement.appendChild(g);
				}
			}
		}
		this.objviewDiv.innerHTML = '';
		updateElement(this.objviewDiv,{
			items:(part?part.children:[]).map(v=>(v instanceof Value)?v.editorElement():undefined)
		});
	}

	public save(): any {
		return this.editPane.model.save();
	}

	public loadJson(json: any) {
		this.recreateView(Model.load(new ModelContext('edit'),json));
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

