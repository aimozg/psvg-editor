import {TXY, IXY, DNode, NodePath} from "../svg";
import {SVGItem, updateElement} from "../dom";
import svg = require("../svg");
import dom = require("../dom");
import Dictionary = _.Dictionary;
import List = _.List;

export type JsTypename = 'object'|'function'|'undefined'|'string'|'number'|'boolean'|'symbol';

export type DisplayMode = 'edit'|'view';
export interface ModelCtx {
	model: Model;
	mode: DisplayMode;
	id: number;
	center: TXY;
}

export type EModelPartCategory = "Point"|"Node"|"Path"|"Param"|"Model";
export abstract class ModelPart {
	private static GCounter = 1;
	public readonly gid = '' + ModelPart.GCounter++;
	public id:number;
	public parent: ModelPart;
	public readonly children: ModelPart[];
	protected ctx: ModelCtx;

	constructor(public readonly loader: ModelLoader,
				public name: string|undefined) {

	}

	protected uhref():string {
		return '#svg_'+this.loader.name;
	}

	protected attached(parent: ModelPart) {
		this.ctx = parent.ctx;
		this.id = parent.ctx.id++;
		this.parent = parent;
	}

	public treeNodeId(): string {
		return 'ModelPart_' + this.id;
	}

	public treeNodeText(): string {
		return (this.name? '"'+this.name+'"' : '#'+this.id) + ' ('+this.loader.name+')';
	}

	public treeNodeSelf(): JSTreeNodeInit {
		return {
			id: this.treeNodeId(),
			text: this.treeNodeText()
		}
	}

	public update() {}

	public treeNodeFull(): JSTreeNodeInit {
		let self = this.treeNodeSelf();
		self.children = this.children.map(c => c.treeNodeFull());
		return self;
	}
}
export type ModelElement = CModelElement<any,any,any>
export abstract class CModelElement<
	PARENT extends ModelPart,
	CHILD extends ModelElement,
	ATTR extends string> extends ModelPart {
	public parent: PARENT;
	public graphic: SVGElement|null;
	protected dependants: [string, ModelElement][] = [];
	public readonly children: CHILD[] = [];

	constructor(loader: ModelLoader, name: string|undefined) {
		super(loader, name);
	}

	public attached(parent: PARENT) {
		super.attached(parent);
		this.ctx.model.parts[this.id] = this;
		this.attachChildren();
	}

	protected attach(child: CHILD, dependency?: string) {
		this.children.push(child);
		child.attached(this);
		if (dependency) this.dependOn(child,dependency);
	}

	protected attachAll(...items:[CHILD,string][]);
	protected attachAll(items:[CHILD|null],dependency?:string);
	protected attachAll() {
		if (typeof arguments[1] == 'string') {
			let items = arguments[0] as (CHILD|null)[];
			for (let obj of items) if (obj) this.attach(obj, arguments[1]);
		} else {
			let items = arguments as List<[CHILD,string]>;
			_.each(items,obj=>{if (obj[0]) this.attach(obj[0], obj[1])});
		}
	}

	public display(addclass?:string): SVGElement|null {
		if (!this.graphic) {
			this.graphic = this.draw(this.ctx.mode);
			if (this.graphic) {
				this.graphic.setAttribute('data-partid', ''+this.id);
				if (addclass) this.graphic.classList.add(addclass);
				this.redraw("*", this.ctx.mode);
			}
		}
		return this.graphic;
	}

	protected dependOn<A2 extends string>(other: CModelElement<any,any,A2> | (() => CModelElement<any,any,A2>),
										  attr: A2) {
		if (typeof other === 'function') this.ctx.model.queuePostload(() => other().dependants.push([attr, this]));
		else other.dependants.push([attr, this]);
	}

	public update(attr: ATTR|"*"='*') {
		super.update();
		this.redraw(attr,this.ctx.mode);
		this.fireUpdated(attr);
	}

	protected fireUpdated(attr: ATTR|"*") {
		for (let dep of this.dependants) if (attr == '*' || dep[0] == '*' || attr == dep[0]) dep[1].updated(this, attr);
		if (this.ctx) this.ctx.model.updated(this, attr)
	}

	protected abstract draw(mode:DisplayMode): SVGElement|null;

	protected abstract redraw(attr: ATTR|"*",mode:DisplayMode);

	protected abstract attachChildren();

	protected abstract updated<A2 extends string>(other: CModelElement<any,any,A2>, attr: A2);
}

export type EPointAttr = '*'|'pos';
export type ModelPoint = CModelPoint<any>;
export abstract class CModelPoint<CHILD extends ModelElement> extends CModelElement<any,CHILD,EPointAttr> {
	public xy: TXY = [0, 0];
	public g: SVGGElement|null;
	private use: SVGUseElement|null;

	constructor(loader: ModelLoader,
				name: string|undefined,
				public readonly cssclass: string) {
		super(loader, name);
	}

	calculate(): TXY {
		return this.xy = this.fcalculate();
	}

	protected draw(mode:DisplayMode): SVGGElement|null {
		if (mode == "edit") {
			this.g = SVGItem('g');
			this.use = SVGItem('use', {'href': this.uhref()});
			updateElement(this.g, {
				'class': `${this.cssclass} elem point`,
				items: [this.use]
			});
			return this.g;
		}
		return null;
	}

	protected abstract fcalculate(): TXY;

	protected redraw(attr: EPointAttr,mode:DisplayMode) {
		let [x,y] =this.calculate();
		if (mode == 'edit' && this.use) {
			svg.tf2list(svg.tftranslate(x, y), this.use.transform.baseVal);
			/*this.use.x.baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX,x);
			 this.use.y.baseVal.newValueSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX,y);*/
		}
	}

	abstract save(): any;

}

export type ENodeAttr = '*'|'pos'|'handle';
export type ModelNode = CModelNode<any>;
export abstract class CModelNode<CHILD> extends CModelElement<ModelPath,ModelPoint,ENodeAttr> {

	public index: number;

	protected prevNode(): ModelNode {
		let n = this.parent.nodes.length;
		return this.parent.nodes[(this.index + n - 1) % n];
	}

	protected nextNode(): ModelNode {
		let n = this.parent.nodes.length;
		return this.parent.nodes[(this.index + 1) % n];
	}

	protected first: boolean;
	protected last: boolean;

	protected attachChildren() {
		this.index = this.parent.nodes.indexOf(this);
		let parent = this.parent;
		this.first = !parent.closed && +this.index == 0;
		this.last = !parent.closed && +this.index == parent.nodes.length - 1;
	}

	public abstract center(): IXY;

	public abstract save(): any;

	public abstract toDNode(): DNode;
}
export abstract class CommonNode<CHILD> extends CModelNode<any> {
	protected l1: SVGLineElement|null;
	protected l2: SVGLineElement|null;
	protected u0: SVGUseElement|null;
	protected g: SVGGElement|null;

	constructor(loader: ModelLoader,
				name: string|undefined,
				public pos: ModelPoint,
				private gclass: string) {
		super(loader, name);
	}

	protected attachChildren() {
		super.attachChildren();
		this.attach(this.pos, "pos");
	}

	protected draw(mode:DisplayMode): SVGElement|null {
		this.l1 = null;
		this.l2 = null;
		if (mode == 'edit') {
			this.g = dom.SVGItem('g', {
				'class': 'node '+(this.gclass||''),
				items: [
					this.u0 = SVGItem('use', {href: this.uhref()}),
					this.pos.display("pt_node")
				]
			});
		}
		return this.g;
	}

	public center(): IXY {
		return this.pos.calculate();
	}

	public toDNode(): DNode {
		let h12xy = this.calcHandles();
		return {
			p: this.pos.calculate(),
			h1: h12xy[0],
			h2: h12xy[1]
		}
	}

	protected redraw(attr: ENodeAttr,mode:DisplayMode) {
		let pxy = this.pos.calculate();
		if (mode == 'edit' && this.g && this.u0) {
			svg.tf2list(svg.tftranslate(pxy[0], pxy[1]), this.u0.transform.baseVal);
			let h12xy = this.calcHandles();
			if (this.l1) this.g.removeChild(this.l1);
			if (this.l2) this.g.removeChild(this.l2);
			this.l1 = this.l2 = null;
			if (!this.first) {
				this.l1 = dom.SVGItem('line', {
					x1: pxy[0], x2: h12xy[0][0],
					y1: pxy[1], y2: h12xy[0][1], 'class': 'handle1'
				});
				this.g.insertBefore(this.l1, this.g.firstChild);
			}
			if (!this.last) {
				this.l2 = dom.SVGItem('line', {
					x1: pxy[0], x2: h12xy[1][0],
					y1: pxy[1], y2: h12xy[1][1], 'class': 'handle2'
				});
				this.g.insertBefore(this.l2, this.g.firstChild);
			}
		}
	}


	protected abstract calcHandles(): [TXY, TXY];
}

export type EPathAttr = "*"|"d";
export type ModelPath = CModelPath;
export class CModelPath extends CModelElement<Model,ModelNode,EPathAttr> {
	private g: SVGGElement|null;
	private p: SVGPathElement;

	constructor(name: string|undefined,
				public nodes: ModelNode[],
				public style: any,
				public closed: boolean = true) {
		super(PATH_LOADER, name);
	}

	protected attachChildren() {
		for (let i = 0, ns = this.nodes, n = ns.length; i < n; i++) {
			this.attach(ns[i], '*');
		}
	}

	protected draw(mode:DisplayMode): SVGElement {
		this.p = SVGItem('path',{
			d: this.toSvgD(),
		});
		if (mode == 'edit') {
			let enodes = this.nodes.map(n => n.display());
			this.g = SVGItem('g', {
				'class': 'elem path',
				items: ([this.p] as (Element|null)[]).concat(enodes)
			});
			return this.g;
		} else {
			return this.p;
		}
	}

	public redraw(attr: EPathAttr,mode:DisplayMode) {
		this.p.setAttribute('d', this.toSvgD());
		updateElement(this.p,{
			d: this.toSvgD(),
			style: mode=='edit'?{}:this.style
		});
	}


	protected updated(other: ModelNode, attr: string) {
		if (attr == 'pos' || attr == 'handle' || attr == '*') this.update("*");
	}

	public toNodePath(): NodePath {
		return {
			z: this.closed,
			nodes: this.nodes.map(n => n.toDNode())
		}
	}

	public toSvgD(): string {
		return svg.dtostr(svg.nodestoels(this.toNodePath()));
	}

	public save(): any {
		return {
			name: this.name,
			closed: this.closed,
			style: this.style||{},
			nodes: this.nodes.map(n => n.save())
		};
	}

}
export const PATH_LOADER: ModelLoader = {
	cat: 'Path',
	name: 'Path',
	objtypes: ['object'],
	loaderfn: (m: Model, json: any, strict: boolean) => new CModelPath(
		json['name'] as string,
		json['nodes'].map(j => m.loadNode(j)),
		json['style']||{},
		!!json['closed'])
};

export class ModelParam extends ModelPart {
	constructor(name: string,
				public defVal: number = 0.5,
				public minVal: number = 0,
				public maxVal: number = 1) {
		super(PARAM_LOADER, name);
	}

	public save(): any {
		return {
			name: this.name, defVal: this.defVal, minVal: this.minVal, maxVal: this.maxVal
		}
	}
}
export const PARAM_LOADER: ModelLoader = {
	cat: 'Param',
	name: 'Param',
	objtypes: ['object'],
	loaderfn: (m: Model, json: any, strict: boolean) => new ModelParam(json['name'], json['defVal'], json['minVal'], json['maxVal'])
};
export type EModelAttr = "*";


export class Model extends CModelElement<Model,any,EModelAttr> {
	private paths: ModelPath[] = [];
	private params: ModelParam[] = [];
	public readonly parts: {
		[index: string]: ModelPart;
	} = {};
	public readonly g: SVGGElement = SVGItem('g', {'class': 'model'});
	public onUpdate: (obj: ModelPart) => any = (x => void(0));
	private postloadQueue: (() => any)[] = [];

	constructor(mode: DisplayMode) {
		super(MODEL_LOADER, "unnamed");
		this.ctx = {
			model: this,
			mode: mode,
			id: 0,
			center: [0, 0]
		}
	}

	public save(): any {
		return {
			name: this.name,
			paths: this.paths.map(p => p.save()),
			params: this.params.map(p => p.save())
		}
	}

	public updated(other: ModelElement, attr: string) {
		//console.log(other,attr);
		this.onUpdate(other);
	}

	public static load(mode: DisplayMode, json: any): Model {
		let m = new Model(mode);
		m.name = json['name'];
		for (let j of json['paths']) {
			const path = m.loadPath(j);
			m.paths.push(path);
			m.attach(path, "*");
		}
		for (let j of json['params'] || []) m.params.push(m.loadPart('Param', j) as ModelParam);
		m.doPostload();
		return m;
	}

	public doPostload() {
		for (let toa of this.postloadQueue) toa();
		this.postloadQueue = [];
	}

	protected attachChildren() {
	}

	public display(addclass?:string): SVGElement {
		return super.display(addclass)!!
	}

	public draw(mode:DisplayMode): SVGElement {
		return updateElement(this.g, {
			style: {
				stroke: 'transparent',
				fill: 'transparent',
				'stroke-opacity': 1,
				'fill-opacity': 1,
				'opacity': 1,
				'stroke-width': 1
			},
			items: _.flatten(this.paths.map(p => p.display()))
		});
	}

	protected redraw(attr: EModelAttr,mode:DisplayMode) {
		/*updateElement(this.g, {
		 items: this.paths.map(p => p.display())
		 });*/
	}

	public queuePostload<A2 extends string>(code: () => any) {
		this.postloadQueue.push(code);
	}

	public findPoint(name: string): ModelPoint|undefined {
		return _.find(this.parts, x => (x instanceof CModelPoint && x.name == name)) as ModelPoint;
	}

	public loadPart(cat: EModelPartCategory, json: any): ModelPart {
		const type = typeof json;
		let loaders: LoaderLib = Model.loaders[cat] || {
				bytypefield: {},
				byjstype: {}
			};
		if (type == 'object') {
			let tfloader = loaders.bytypefield[json['type']];
			if (tfloader) return tfloader.loaderfn(this, json, true)!!;
		}
		let jtloaders = loaders.byjstype[type];
		if (!jtloaders || jtloaders.length == 0) throw "No loaders for " + cat + " "
		+ type + " " + JSON.stringify(json);
		for (let loader of jtloaders) {
			let ele = loader.loaderfn(this, json, false);
			if (ele) return ele;
		}
		throw JSON.stringify(json)
	}

	public loadPoint(json: any): ModelPoint {
		return this.loadPart('Point', json) as ModelPoint;
	}

	public loadNode(json: any): ModelNode {
		return this.loadPart('Node', json) as ModelNode;
	}

	public loadPath(json: any): ModelPath {
		return this.loadPart('Path', json) as ModelPath;
	}

	public clone(mode:DisplayMode):Model {
		// TODO optimize
		return Model.load(mode,this.save());
	}

	public static registerLoader(loader: ModelLoader) {
		let lib: LoaderLib = Model.loaders[loader.cat] || {
				bytypefield: {},
				byjstype: {}
			};
		if (loader.typename) {
			lib.bytypefield[loader.typename] = loader;
		}
		for (let ot of loader.objtypes || []) {
			let byjt: ModelLoader[] = lib.byjstype[ot] || [];
			byjt.push(loader);
			lib.byjstype[ot] = byjt;
		}
		Model.loaders[loader.cat] = lib;
	}

	private static loaders: {
		//TODO [index:EModelElementCategory]: {
		[index: string]: LoaderLib;
	} = {}
}
export const MODEL_LOADER: ModelLoader = {
	cat: 'Model',
	name: 'Model',
	objtypes: ['object'],
	loaderfn: (m: Model, json: any, strict: boolean) => Model.load("edit", json)
};

export interface LoaderLib {
	byjstype: Dictionary<ModelLoader[]>/*{
	 //TODO [index:JsTypename]
	 [index:string]:ModelElementLoader[]
	 }*/
	;
	bytypefield: Dictionary<ModelLoader>;
}

export interface ModelLoader {
	cat: EModelPartCategory;
	name: string;
	loaderfn(model: Model, json: any, strict: boolean):ModelPart|null;
	typename?: string;
	objtypes?: JsTypename[];
}

Model.registerLoader(PATH_LOADER);
Model.registerLoader(PARAM_LOADER);