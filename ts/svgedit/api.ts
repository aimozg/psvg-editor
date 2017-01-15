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

export abstract class ModelElement<PARENT extends ModelElement<any,any,any>,ROLE,ATTR extends string> {
	private static Counter = 1;
	public readonly id = '' + ModelElement.Counter++;

	constructor(public name: string|undefined) {
	}

	public parent: PARENT;
	public role: ROLE;

	public attach(parent: PARENT, role: ROLE) {
		this.ctx = parent.ctx;
		this.parent = parent;
		this.role = role;
		this.attachChildren();
	}

	public display(): SVGElement {
		if (!this.graphic) {
			this.graphic = this.draw();
			this.redraw("*");
		}
		return this.graphic;
	}

	private graphic: SVGElement;

	protected ctx: ModelCtx|undefined;
	protected dependants: [string, ModelElement<any,any,any>][] = [];

	protected dependOn<A2 extends string>(other: ModelElement<any,any,A2>|(() => ModelElement<any,any,A2>), attr: A2) {
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

	protected abstract redraw(attr:ATTR|"*");

	protected abstract attachChildren();

	protected abstract updated<A2 extends string>(other: ModelElement<any,any,A2>, attr: A2);
}

export type EPointRole = 'node'|'handle'|'ref';
export type EPointAttr = '*'|'pos';
export abstract class ModelPoint extends ModelElement<ModelNode|ModelPoint,EPointRole,EPointAttr> {
	public xy: TXY = [0, 0];
	public readonly g: SVGGElement = SVGItem('g');
	private inner: SVGPathElement;
	private outer: SVGPathElement;

	constructor(name: string|undefined,
				public readonly cssclass: string) {
		super(name);
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
			'data-pathid': this.parent.parent.id,
			'data-nodeid': this.parent.role,
			'class': `${this.cssclass} elem point pt_${this.role}`,
			items: [this.outer, this.inner]
		});
		return this.g;
	}

	protected abstract fcalculate(): TXY;

	protected redraw(attr:EPointAttr) {
		this.calculate();
		let dstr = 'M ' + this.xy.join(',') + ' z';
		this.inner.setAttribute('d', dstr);
		this.outer.setAttribute('d', dstr);
	}

	abstract repr(): string;

	abstract save(): any;

}

export type ENodeAttr = '*'|'pos'|'handle';
export abstract class ModelNode extends ModelElement<ModelPath,number,ENodeAttr> {

	public readonly g: SVGGElement = dom.SVGItem('g', {
		'class': 'node'
	});

	protected prevNode(): ModelNode {
		let n = this.parent.nodes.length;
		return this.parent.nodes[(this.role + n - 1) % n];
	}

	protected nextNode(): ModelNode {
		let n = this.parent.nodes.length;
		return this.parent.nodes[(this.role + 1) % n];
	}

	protected first: boolean;
	protected last: boolean;

	protected attachChildren() {
		let parent = this.parent;
		let role = this.role;
		this.first = !parent.closed && role == 0;
		this.last = !parent.closed && role == parent.nodes.length - 1;
	}

	abstract points(): ModelPoint[];

	public abstract center(): IXY;

	public abstract save(): any;

	public abstract repr(): string;

	public abstract toDNode(): DNode;
}
export abstract class CommonNode extends ModelNode {
	protected l1: SVGLineElement;
	protected l2: SVGLineElement;

	constructor(name: string|undefined,
				public pos: ModelPoint,
				gclass: string) {
		super(name);
		this.g.classList.add(gclass);
	}

	protected attachChildren() {
		super.attachChildren();
		this.pos.attach(this, "node");
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

	protected redraw(attr:ENodeAttr) {
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
export class ModelPath extends ModelElement<Model,any,EPathAttr> {
	public readonly g: SVGGElement = SVGItem('g', {'class': 'elem path'});
	p: SVGPathElement;

	constructor(name: string|undefined,
				public nodes: ModelNode[],
				public closed: boolean = true) {
		super(name);
	}

	protected attachChildren() {
		for (let i = 0, ns = this.nodes, n = ns.length; i < n; i++) {
			ns[i].attach(this, i);
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

	public redraw(attr:EPathAttr) {
		this.p.setAttribute('d', this.toSvgD());
	}

	public save(): any {
		return {
			name: this.name,
			closed: this.closed,
			nodes: this.nodes.map(n => n.save())
		};
	}


	protected updated(other: ModelElement<any, any, ENodeAttr>, attr: ENodeAttr) {
		this.redraw("*");
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
}

export class ModelParam {
	constructor(public name: string,
				public defVal: number = 0.5,
				public minVal: number = 0,
				public maxVal: number = 1) {
	}

	public save(): any {
		return {
			name: this.name, defVal: this.defVal, minVal: this.minVal, maxVal: this.maxVal
		}
	}

	public static load(json: any): ModelParam {
		return new ModelParam(json['name'], json['defVal'], json['minVal'], json['maxVal']);
	}
}

export type TModelItem = ModelElement<any,any,any>|ModelParam;
export type EModelElementCategory = "Point"|"Node"|"Path"|"Param"|"Model";
export type EModelAttr = "*";


export class Model extends ModelElement<Model,any,EModelAttr> {
	private paths: ModelPath[] = [];
	private params: ModelParam[] = [];
	private points: ModelPoint[] = [];
	public readonly g: SVGGElement = SVGItem('g', {'class': 'model'});
	public onUpdate: (obj: TModelItem) => any = (x => void(0));
	private postloadQueue: (() => any)[] = [];

	constructor(mode: DisplayMode) {
		super("unnamed");
		this.ctx = {
			model: this,
			mode: mode,
			center: [0, 0]
		}
	}

	toTreeNodes(): JSTreeNodeInit[] {
		return [{
			id: `Model_${this.id}_Paths`,
			text: 'Paths',
			children: this.paths.map(p => {
				return {
					id: this.treeNodeId(p),
					text: this.treeNodeText(p),
					children: p.nodes.map(n => ({
						id: this.treeNodeId(n),
						text: this.treeNodeText(n),
						children: n.points().map(pt => ({
							id: this.treeNodeId(pt),
							text: this.treeNodeText(pt)
						}))
					}))
				};
			})
		}, {
			id: 'Model_' + this.id + '_Parameters',
			text: 'Parameters',
			children: this.params.map(p => {
				return {
					id: this.treeNodeId(p),
					text: this.treeNodeText(p)
				}
			})
		}]
	}

	//noinspection JSMethodCanBeStatic
	treeNodeId(obj: TModelItem): string {
		return obj instanceof ModelPath ? `ModelPath_${obj.id}` :
			obj instanceof ModelNode ? `ModelNode_${obj.parent.id}_${obj.role}` :
				obj instanceof ModelPoint ? `ModelPoint_${obj.parent.parent.id}_${obj.parent.role}_${obj.role}` :
					obj instanceof ModelParam ? `ModelParam_${obj.name}` : '<error>';
	}

	//noinspection JSMethodCanBeStatic
	treeNodeText(obj: TModelItem): string {
		return (obj.name ? '"' + obj.name + '" ' : '') +
			(obj instanceof ModelPath ? '' :
				obj instanceof ModelNode ? (obj.role + ' ' + obj.repr()) :
					obj instanceof ModelPoint ? (obj.role + ' ' + obj.repr()) :
						obj instanceof ModelParam ? `= ${obj.defVal} [${obj.minVal} .. ${obj.maxVal}]` : '<error>');
	}

	public save(): any {
		return {
			name: this.name,
			paths: this.paths.map(p => p.save()),
			params: this.params.map(p => p.save())
		}
	}

	public updated<A2 extends string>(other: ModelElement<any, any, A2>, attr: A2) {
		//console.log(other,attr);
		this.onUpdate(other);
	}

	public static load(mode: DisplayMode, json: any): Model {
		let m = new Model(mode);
		m.name = json['name'];
		for (let j of json['paths']) m.addPath(m.loadPath(j));
		for (let j of json['params'] || []) m.params.push(ModelParam.load(j));
		m.doPostload();
		return m;
	}

	public doPostload() {
		for (let toa of this.postloadQueue) toa();
		this.postloadQueue = [];
	}

	public addPath(path: ModelPath) {
		this.paths.push(path);
		path.attach(this, 'path');
	}

	protected attachChildren() {
	}

	public draw(): SVGElement {
		return updateElement(this.g, {
			items: _.flatten(this.paths.map(p => p.display()))
		});
	}

	protected redraw(attr:EModelAttr) {
		/*updateElement(this.g, {
		 items: this.paths.map(p => p.display())
		 });*/
	}

	public queuePostload<A2 extends string>(code: () => any) {
		this.postloadQueue.push(code);
	}

	public findPath(id: string): ModelPath|undefined {
		return _.find(this.paths, (p: ModelPath) => p.id == id);
	}

	public findParam(name: string): ModelParam|undefined {
		return _.find(this.params, (p: ModelParam) => p.name == name);
	}

	public findPoint(name: string): ModelPoint|undefined {
		return _.find(this.points, x => (x.name && x.name == name))
	}

	public loadElement(cat:EModelElementCategory,json:any):ModelElement<any,any,any> {
		const type = typeof json;
		let loaders:ElementLoaderLib = Model.loaders[cat] ||  {
			bytypefield:{},
			byjstype:{}
		};
		if (type == 'object') {
			let tfloader = loaders.bytypefield[json['type']];
			if (tfloader) return tfloader.loaderfn(this,json,true);
		}
		let jtloaders = loaders.byjstype[type];
		if (!jtloaders || jtloaders.length == 0) throw "No loaders for " +cat+" "
		+type+" "+JSON.stringify(json);
		for (let loader of jtloaders) {
			let ele = loader.loaderfn(this,json,false);
			if (ele) return ele;
		}
	}

	public loadPoint(json: any): ModelPoint {
		/*if (typeof json == 'string') return ModelPointRef.load(json);
		let type = json['type'];
		switch (type) {
			default:
				if (type[0] == '@') return ModelPointRef.load(json);
		}*/
		let point = this.loadElement('Point',json) as ModelPoint;
		this.points.push(point);
		return point;
	}

	public loadNode(json: any): ModelNode {
		return this.loadElement('Node',json) as ModelNode;
	}

	public loadPath(json: any): ModelPath {
		return new ModelPath(
			json['name'] as string,
			json['nodes'].map(j => this.loadNode(j)),
			json['closed'] as boolean);
	}

	public static registerLoader(loader:ModelElementLoader<any>) {
		let lib: ElementLoaderLib = Model.loaders[loader.cat] || {
				bytypefield: {},
				byjstype: {}
			};
		lib.bytypefield[loader.typename] = loader;
		for (let ot of loader.objtypes||[]) {
			let byjt:ModelElementLoader<any>[] = lib.byjstype[ot] || [];
			byjt.push(loader);
			lib.byjstype[ot] = byjt;
		}
		Model.loaders[loader.cat] = lib;
	}
	private static loaders: {
		//TODO [index:EModelElementCategory]: {
		[index:string]: ElementLoaderLib;
	} = {}
}

export interface ElementLoaderLib {
	byjstype: Dictionary<ModelElementLoader<any>[]>/*{
		//TODO [index:JsTypename]
		[index:string]:ModelElementLoader[]
	}*/;
	bytypefield: Dictionary<ModelElementLoader<any>>;
}

export interface ModelElementLoader<E extends ModelElement<any,any,any>> {
	cat:EModelElementCategory;
	typename:string;
	loaderfn:(model:Model,json:any,strict:boolean)=>E;
	objtypes?:JsTypename[];
}