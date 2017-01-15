import dom = require('./dom');
import svg = require("./svg");

import {NodePath} from "./svg";
import {DisplayMode, Model, ModelPath, ModelNode, TModelItem} from "./svgedit/api";
import {FixedPoint} from "./svgedit/ptfixed";
import {ALL_LOADERS} from "./svgedit/_all";
import {CuspNode} from "./svgedit/ncusp";

//noinspection JSUnusedGlobalSymbols
export const importz = {dom,svg,ALL_LOADERS};

let document: Document;

export class Editor {
	public root: SVGSVGElement;
	private tree: JSTree;
	private eModel: SVGElement = null;
	private zoombox: SVGGElement = null;
	private zoomfact = 2;
	private mode: DisplayMode = 'edit';
	public model: Model = new Model(this.mode);

	selPath: ModelPath;
	selNode: ModelNode;

	constructor(canvasDiv: HTMLElement, private treeDiv: HTMLElement) {
		canvasDiv.innerHTML = '';
		document = canvasDiv.ownerDocument;
		let width = 100;//canvasDiv.clientWidth;
		let height = 100;//canvasDiv.clientHeight;
		let x0 = -Math.floor(width / 2);
		let y0 = -Math.floor(height / 2);
		this.root = dom.SVG({
			width: width,
			height: height,
			items: [{
				tag: 'defs',
				items: [/*{
				 tag:'g',
				 id: 'node',
				 items:[{
				 tag: 'path',
				 'class': 'outer',
				 d: 'M 0,0 0,0 z'
				 },{
				 tag: 'path',
				 'class': 'inner',
				 d: 'M 0,0 0,0 z'
				 }]
				 }*/]
			}, {
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
		/*let handler = (ev: MouseEvent) => {
		 let tgt = ev.target as Element;
		 while (tgt && tgt != this.root && !tgt.classList.contains('elem')) tgt = tgt.parentElement;
		 if (!tgt) return;
		 let path = this.model.findPath(tgt.getAttribute('data-pathid'));
		 let node = path ? path.nodes[tgt.getAttribute('data-nodeid')] : undefined;

		 let pt = this.root.createSVGPoint();
		 pt.x = ev.clientX;
		 pt.y = ev.clientY;
		 };
		 for (let e of ['mousemove', 'mousedown', 'mouseup', 'mouseout']) this.root.addEventListener(e, handler);*/
		canvasDiv.appendChild(this.root);
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

		if (this.tree) this.tree.destroy();
		this.tree = $(this.treeDiv).jstree({
			core: {
				data: this.model.toTreeNodes(),
				check_callback: true
			}
		}).jstree();
		this.tree.get_container().on('select_node.jstree', (e, data: JSTreeNodeEvent) => {
			let nid = data.node.id.split('_');
			switch (nid[0]) {
				case 'Model':
					break;
				case 'ModelNode':
					this.selectNode(nid[1], +nid[2]);
					break;
				case 'ModelPoint':
					break;
				case 'ModelPath':
					this.selectPath(nid[1]);
					break;
			}
		}).on('click dblclick', (/*e*/) => {
			// console.log(e, this.tree.jstree().get_node(e.target))
		});
		this.model.onUpdate = (obj: TModelItem) => {
			this.tree.rename_node(this.model.treeNodeId(obj), this.model.treeNodeText(obj));
		}
	}

	private resizeView() {
		let brect = svg.rect_cpy(this.zoombox.getBBox());
		svg.rect_expand(brect, 50);
		svg.rect_cpy(brect, this.root.viewBox.baseVal);
		svg.rect_scale(brect, this.zoomfact);
		this.root.width.baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX, brect.width);
		this.root.height.baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX, brect.height);

	}

	selectPath(id: string): ModelPath {
		if (this.selPath) this.selPath.g.classList.remove('-selected');
		let path = this.selPath = this.model.findPath(id);
		path.g.classList.add('-selected');
		path.g.parentNode.appendChild(path.g);
		return path;
	}

	selectNode(pathid: string, i: number): ModelNode {
		if (this.selNode) this.selNode.g.classList.remove('-selected');
		let node = this.selNode = this.selectPath(pathid).nodes[i];
		node.g.classList.add('-selected');
		node.g.parentNode.appendChild(node.g);
		return node;
	}

	public save(): any {
		return this.model.save();
	}

	public loadJson(json: any) {
		this.model = Model.load(this.mode, json);
		this.recreateView();
	}

	loadSvgStruct(el: SVGElement) {
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
	}
}


//noinspection JSUnusedGlobalSymbols
export function setup(canvasDiv: HTMLElement, treeDiv: HTMLElement): Editor {
	return new Editor(canvasDiv, treeDiv);
}

