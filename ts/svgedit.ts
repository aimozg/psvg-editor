import dom = require('./dom');
import svg = require("./svg");
require("jstree-css");

import {Model, ModelPart, CModelElement} from "./svgedit/api";
import {ALL_LOADERS} from "./svgedit/_all";

//noinspection JSUnusedGlobalSymbols
export const importz = {dom,svg,ALL_LOADERS};

let document: Document;

export class Editor {
	public root: SVGSVGElement;
	private tree: JSTree;
	private eModel: SVGElement|null = null;
	private zoombox: SVGGElement;
	private zoomfact = 2;
	public model: Model = new Model('edit');
	private scaledown:SVGTransformable[];
	private previews:SVGElement[] = [];

	selection:ModelPart|null = null;

	constructor(canvasDiv: HTMLElement,
				private treeDiv: HTMLElement,
				previewDivs: HTMLElement[]) {
		canvasDiv.innerHTML = '';
		document = canvasDiv.ownerDocument;
		let width = 100;//canvasDiv.clientWidth;
		let height = 100;//canvasDiv.clientHeight;
		let x0 = -Math.floor(width / 2);
		let y0 = -Math.floor(height / 2);
		this.scaledown = [];
		this.root = dom.SVG({
			width: width,
			height: height,
			items: [{
				tag: 'defs',
				items: [{
					tag:'path',
					id:'svgpt_diamond_sm',
					d:'M -5 0 0 -5 5 0 0 5 z',
					style: { 'any': 'inherit' },
					callback:(e)=>this.scaledown.push(e as any as SVGTransformable)
				},{
					tag:'path',
					id:'svgpt_diamond',
					d:'M -10 0 0 -10 10 0 0 10 z',
					style: { 'any': 'inherit' },
					callback:(e)=>this.scaledown.push(e as any as SVGTransformable)
				},{
					tag:'circle',
					id:'svgpt_circle_sm',
					cx:0,cy:0,r:5,
					style: {'any':'inherit'},
					callback:(e)=>this.scaledown.push(e as any as SVGTransformable)
				},{
					tag:'circle',
					id:'svgpt_circle',
					cx:0,cy:0,r:10,
					style: {'any':'inherit'},
					callback:(e)=>this.scaledown.push(e as any as SVGTransformable)
				},{
					tag:'rect',
					id:'svgpt_box_sm',
					x: -2.5, y: -2.5, width:5,height:5,
					style: {'any':'inherit'},
					callback:(e)=>this.scaledown.push(e as any as SVGTransformable)
				},{
					tag:'use',id:'svg_FixedPoint',href:'#svgpt_box_sm'
				},{
					tag:'use',id:'svg_CuspNode',href:'#svgpt_diamond'
				},{
					tag:'use',id:'svg_Flow1Node',href:'#svgpt_circle'
				},{
					tag:'use',id:'svg_AutosmoothNode',href:'#svgpt_circle'
				}]
			},{
				tag: 'rect',
				x: '-50%', y: '-50%',
				height: '100%',
				width: '100%',
				'class': 'viewport'
			}, {
				tag: 'g',
				id: 'zoombox',
				//transform:'scale('+this.zoomfact+')',
				callback: el => this.zoombox = el as SVGGElement,
				items: []
			}]
		}, [x0, y0, width, height]);
		this.root.setAttribute('tabindex','0');
		canvasDiv.appendChild(this.root);
		for (let pd of previewDivs) {
			let svg = dom.SVG({width: width,height: height,items:[]},[x0,y0,width,height]);
			svg.setAttribute('tabindex','0');
			pd.appendChild(svg);
			this.previews.push(svg);
		}
		document.addEventListener('wheel', ev => {
			if (ev.ctrlKey) {
				ev.preventDefault();
				this.modZoom(ev.deltaY < 0 ? 1 : -1);
			}
		});
	}

	modZoom(steps: number) {
		this.zoomfact *= Math.pow(1.1, steps);
		//svg.tf2list(svg.tfscale(this.zoomfact),this.zoombox.transform.baseVal);
		this.resizeView();
	}

	recreateView() {
		if (this.eModel) {
			this.zoombox.removeChild(this.eModel);
		}
		this.eModel = this.model.display();
		this.zoombox.appendChild(this.eModel);
		this.resizeView();
		if (this.previews.length>0) {
			let j = this.model.save();
			for (let v of this.previews) {
				v.innerHTML='';
				let m2 = Model.load("view", j);
				v.appendChild(m2.display());
			}
		}

		if (this.tree) this.tree.destroy();
		this.tree = $(this.treeDiv).jstree({
			core: {
				data: this.model.treeNodeFull().children,
				check_callback: true
			}
		}).jstree();
		this.tree.get_container().on('select_node.jstree', (e, data: JSTreeNodeEvent) => {
			let id = data.node.id.split('_');
			if (id[0] == 'ModelPart') this.select(this.model.parts[id[1]]);
		}).on('click dblclick', (/*e*/) => {
			// console.log(e, this.tree.jstree().get_node(e.target))
		});
		this.model.onUpdate = (obj: ModelPart) => {
			this.tree.rename_node(obj.treeNodeId(), obj.treeNodeText());
		};
		this.eModel.addEventListener('click',(e:MouseEvent)=>{
			for (let element = e.target as Element|null;element;element = element.parentElement) {
				let part = this.model.parts[element.getAttribute('data-partid')||''];
				if (part) {
					this.select(part);
					return;
				}
			}
		});
	}

	private resizeView() {
		let brect = svg.rect_cpy(this.zoombox.getBBox());
		svg.rect_expand(brect, 50);
		svg.rect_cpy(brect, this.root.viewBox.baseVal);
		svg.rect_scale(brect, this.zoomfact);
		this.root.width.baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX, brect.width);
		this.root.height.baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX, brect.height);
		for (let obj of this.scaledown) {
			obj.transform.baseVal.initialize(svg.tfscale(1.0/this.zoomfact));
		}
	}

	select(part:ModelPart){
		if (this.selection == part) return;
		for (let s=this.selection;s && s instanceof CModelElement && s!=this.model;s=s.parent) {
			s.graphic.classList.remove('-selected','-primary');
		}
		this.tree.deselect_all(true);
		this.selection = part;
		if (part && part instanceof CModelElement && part!=this.model) {
			this.tree.select_node(part.treeNodeId(),true);
			part.graphic.classList.add('-selected','-primary');
			for (let s = part;s&&s instanceof CModelElement && s!=this.model;s=s.parent) {
				const g = s.graphic;
				if (g) {
					g.classList.add('-selected');
					if (g.parentElement) g.parentElement.appendChild(g);
				}
			}
		}
	}

	public save(): any {
		return this.model.save();
	}

	public loadJson(json: any) {
		this.model = Model.load('edit', json);
		this.recreateView();
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
					  previewDivs: HTMLElement[]=[]): Editor {
	return new Editor(editorDiv, treeDiv, previewDivs);
}

