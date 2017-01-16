import {TXY, IXY, DNode, NodePath} from "../svg";
import {SVGItem, updateElement, CEASvgPath} from "../dom";
import svg = require("../svg");
import dom = require("../dom");
import Dictionary = _.Dictionary;

export type JsTypename = 'object'|'function'|'undefined'|'string'|'number'|'boolean'|'symbol';

export type DisplayMode = 'edit'|'view';
export interface ModelCtx {
	model: Model;
	mode: DisplayMode;
	center: TXY;
}

export type EModelPartCategory = "Point"|"Node"|"Path"|"Param"|"Model";
export abstract class ModelPart {
	private static Counter = 1;
	public readonly id = '' + ModelPart.Counter++;
	public parent: ModelPart;
	public readonly children: ModelPart[];
	public role:string;
	protected ctx: ModelCtx|undefined;

	constructor(public readonly loader: ModelLoader,
				public name: string|undefined) {

	}

	protected attached(parent:ModelPart){
		this.ctx = parent.ctx;
		this.parent = parent;
	}

	public treeNodeId(): string {
		return 'ModelElement_' + this.id;
	}

	public treeNodeText(): string {
		return this.role + ' ' + this.loader.name + (this.name ? '"' + this.name + '"' : '');
	}

	public treeNodeSelf(): JSTreeNodeInit {
		return {
			id: this.treeNodeId(),
			text: this.treeNodeText()
		}
	}

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
	private graphic: SVGElement;
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

	protected attach(child: CHILD, role: string) {
		child.role=role;
		this.children.push(child);
		child.attached(this);
	}

	public display(): SVGElement {
		if (!this.graphic) {
			this.graphic = this.draw();
			this.redraw("*");
		}
		return this.graphic;
	}

	protected dependOn<A2 extends string>(other: CModelElement<any,any,A2>| (() => CModelElement<any,any,A2>),
										  attr: A2) {
		if (typeof other === 'function') this.ctx.model.queuePostload(() => other().dependants.push([attr, this]));
		else other.dependants.push([attr, this]);
	}

	protected update(attr: ATTR|"*") {
		this.redraw(attr);
		this.fireUpdated(attr);
	}

	protected fireUpdated(attr: ATTR|"*") {
		for (let dep of this.dependants) if (attr == '*' || dep[0] == '*' || attr == dep[0]) dep[1].updated(this, attr);
		if (this.ctx) this.ctx.model.updated(this, attr)
	}

	protected abstract draw(): SVGElement;

	protected abstract redraw(attr: ATTR|"*");

	protected abstract attachChildren();

	protected abstract updated<A2 extends string>(other: CModelElement<any,any,A2>, attr: A2);
}

export type EPointRole = 'node'|'handle'|'ref';
export type EPointAttr = '*'|'pos';
export type ModelPoint = CModelPoint<any>;
export abstract class CModelPoint<CHILD extends ModelElement> extends CModelElement<any,CHILD,EPointAttr> {
	public xy: TXY = [0, 0];
	public readonly g: SVGGElement = SVGItem('g');
	private inner: SVGPathElement;
	private outer: SVGPathElement;

	constructor(loader: ModelLoader,
				name: string|undefined,
				public readonly cssclass: string) {
		super(loader, name);
	}

	calculate(): TXY {
		return this.xy = this.fcalculate();
	}

	protected draw(): SVGGElement {
		this.inner = SVGItem('path', {
			'class': 'inner',
			d: 'M 0,0 z'
		});
		this.outer = SVGItem('path', {
			'class': 'outer',
			d: 'M 0,0 z'
		});
		updateElement(this.g, {
			'class': `${this.cssclass} elem point pt_${this.role}`,
			items: [this.outer, this.inner]
		});
		return this.g;
	}

	protected abstract fcalculate(): TXY;

	protected redraw(attr: EPointAttr) {
		this.calculate();
		let dstr = 'M ' + this.xy.join(',') + ' z';
		this.inner.setAttribute('d', dstr);
		this.outer.setAttribute('d', dstr);
	}

	abstract repr(): string;

	abstract save(): any;

}

export type ENodeAttr = '*'|'pos'|'handle';
export type ModelNode = CModelNode<any>;
export abstract class CModelNode<CHILD> extends CModelElement<ModelPath,ModelPoint,ENodeAttr> {

	public readonly g: SVGGElement = dom.SVGItem('g', {
		'class': 'node'
	});
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
		this.index = +this.role;
		let parent = this.parent;
		let role = this.index;
		this.first = !parent.closed && +role == 0;
		this.last = !parent.closed && +role == parent.nodes.length - 1;
	}

	public abstract center(): IXY;

	public abstract save(): any;

	public abstract repr(): string;

	public abstract toDNode(): DNode;
}
export abstract class CommonNode<CHILD> extends CModelNode<any> {
	protected l1: SVGLineElement;
	protected l2: SVGLineElement;

	constructor(loader: ModelLoader,
				name: string|undefined,
				public pos: ModelPoint,
				gclass: string) {
		super(loader, name);
		this.g.classList.add(gclass);
	}

	protected attachChildren() {
		super.attachChildren();
		this.attach(this.pos, "node");
		this.dependOn(this.pos, "pos");
	}

	protected draw(): SVGElement {
		this.l1 = undefined;
		this.l2 = undefined;
		updateElement(this.g, {items: [this.pos.display()]});
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

	protected redraw(attr: ENodeAttr) {
		let pxy = this.pos.calculate();
		let h12xy = this.calcHandles();
		if (this.l1) this.g.removeChild(this.l1);
		if (this.l2) this.g.removeChild(this.l2);
		this.l1 = this.l2 = undefined;
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


	protected abstract calcHandles(): [TXY, TXY];
}

export type EPathAttr = "*"|"d";
export type ModelPath = CModelPath;
export class CModelPath extends CModelElement<Model,ModelNode,EPathAttr> {
	public readonly g: SVGGElement = SVGItem('g', {'class': 'elem path'});
	p: SVGPathElement;

	constructor(name: string|undefined,
				public nodes: ModelNode[],
				public closed: boolean = true) {
		super(PATH_LOADER, name);
	}

	protected attachChildren() {
		for (let i = 0, ns = this.nodes, n = ns.length; i < n; i++) {
			this.attach(ns[i], ''+i);
			this.dependOn(ns[i], '*');
		}
	}

	protected draw(): SVGElement {
		let enodes = this.nodes.map(n => n.display());
		updateElement(this.g, {
			items: _.flatten([{
				tag: 'path',
				d: this.toSvgD(),
				callback: el => this.p = el
			} as CEASvgPath, enodes])
		});
		return this.g;
	}

	public redraw(attr: EPathAttr) {
		this.p.setAttribute('d', this.toSvgD());
	}


	protected updated(other: ModelNode, attr: string) {
		if (attr == 'pos' || attr == '*') this.redraw("*");
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
		json['closed'] as boolean)
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
			m.attach(path, 'path');
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

	public draw(): SVGElement {
		return updateElement(this.g, {
			items: _.flatten(this.paths.map(p => p.display()))
		});
	}

	protected redraw(attr: EModelAttr) {
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
			if (tfloader) return tfloader.loaderfn(this, json, true);
		}
		let jtloaders = loaders.byjstype[type];
		if (!jtloaders || jtloaders.length == 0) throw "No loaders for " + cat + " "
		+ type + " " + JSON.stringify(json);
		for (let loader of jtloaders) {
			let ele = loader.loaderfn(this, json, false);
			if (ele) return ele;
		}
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
	loaderfn: (model: Model, json: any, strict: boolean) => ModelPart;
	typename?: string;
	objtypes?: JsTypename[];
}

Model.registerLoader(PATH_LOADER);
Model.registerLoader(PARAM_LOADER);