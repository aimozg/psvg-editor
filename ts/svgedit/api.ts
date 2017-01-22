import {TXY, IXY, DNode} from "../svg";
import {SVGItem, updateElement} from "../dom";
import {ModelPath} from "./path";
import {ModelParam} from "./param";
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

export type EPartCategory = "Point"|"Node"|"Path"|"Model"|"Param"/*|"Value"*/;
export abstract class Value<T> {
	public owner:Part;
	constructor(public readonly name:string){
	}
	public get index():number { return this.owner.values.indexOf(this); }
	public abstract get():T;
	public abstract editorElement():HTMLElement;
	public abstract save():any;
}
export type Part = CPart<any>;
export abstract class CPart<ATTR extends string> {
	private static GCounter = 1;
	public readonly gid = '' + CPart.GCounter++;
	public id:number;
	public owner: Part;
	public readonly children: Part[];
	protected ctx: ModelCtx;

	constructor(public name: string|undefined,
				public readonly values: Value<any>[]) {
		for (let v of values) v.owner = this;
	}

	protected uhref():string {
		return '#svg_'+this.classname;
	}

	public get classname():string {
		return this.constructor['name']||'ModelPart';
	}

	protected attached(parent: Part) {
		this.ctx = parent.ctx;
		this.id = parent.ctx.id++;
		this.owner = parent;
	}

	public treeNodeId(): string {
		return 'ModelPart_' + this.id;
	}

	public treeNodeText(): string {
		return (this.name? '"'+this.name+'"' : '#'+this.id) + ' ('+this.classname+')';
	}

	public treeNodeSelf(): JSTreeNodeInit {
		return {
			id: this.treeNodeId(),
			text: this.treeNodeText()
		}
	}

	public update(attr: ATTR|"*"='*') {

	}

	public treeNodeFull(): JSTreeNodeInit {
		let self = this.treeNodeSelf();
		self.children = this.children.map(c => c.treeNodeFull());
		return self;
	}

	public valueUpdated<T>(value:Value<T>){}

	public abstract save():any;
}

/*export const VALUE_FIXNUM_TYPE = 'fixnum';
export const VALUE_FIXNUM_LOADER:ModelLoader = {
	cat:'Value',
	name:'ValueFixedNumber',
	typename:VALUE_FIXNUM_TYPE,
	objtypes:['number'],
	loaderfn(model: Model, json: any, strict: boolean,name:string):ValueFixedNumber|null {
		if (!strict) {
			if (typeof json == 'number') return new ValueFixedNumber(json, name);
			return null;
		}
		return new ValueFixedNumber(+json['value'], name);
	}
};
Model.registerLoader(VALUE_FIXNUM_LOADER);*/

export type ModelElement = CModelElement<any,any,any>
export abstract class CModelElement<
	PARENT extends Part,
	CHILD extends ModelElement,
	ATTR extends string> extends CPart<ATTR> {
	public owner: PARENT;
	public graphic: SVGElement|null;
	protected dependants: [string, ModelElement][] = [];
	public readonly children: CHILD[] = [];

	constructor(name: string|any, values: Value<any>[]) {
		super(name, values);
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
		super.update(attr);
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

	constructor(name: string|undefined,
				public readonly cssclass: string,
				values: Value<any>[]) {
		super(name, values);
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
		let n = this.owner.nodes.length;
		return this.owner.nodes[(this.index + n - 1) % n];
	}

	protected nextNode(): ModelNode {
		let n = this.owner.nodes.length;
		return this.owner.nodes[(this.index + 1) % n];
	}

	protected first: boolean;
	protected last: boolean;

	protected attachChildren() {
		this.index = this.owner.nodes.indexOf(this);
		let parent = this.owner;
		this.first = !parent.closed && +this.index == 0;
		this.last = !parent.closed && +this.index == parent.nodes.length - 1;
	}

	public abstract center(): IXY;

	public abstract save(): any;

	public abstract toDNode(): DNode;
}


export type EModelAttr = "*";
export class Model extends CModelElement<Model,any,EModelAttr> {
	private paths: ModelPath[] = [];
	private params: ModelParam[] = [];
	public readonly parts: {
		[index: string]: Part;
	} = {};
	public readonly g: SVGGElement = SVGItem('g', {'class': 'model'});
	public onUpdate: (obj: Part) => any = (x => void(0));
	private postloadQueue: (() => any)[] = [];

	constructor(mode: DisplayMode) {
		super("unnamed", []);
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

	private loadPart(cat: EPartCategory, json: any, ...args:any[]): Part {
		const type = typeof json;
		let loaders: LoaderLib = Model.loaders[cat] || {
				bytypefield: {},
				byjstype: {}
			};
		if (type == 'object') {
			let tfloader = loaders.bytypefield[json['type']];
			if (tfloader) return tfloader.loaderfn(this, json, true, ...args)!!;
		}
		let jtloaders = loaders.byjstype[type];
		if (!jtloaders || jtloaders.length == 0) throw "No loaders for " + cat + " "
		+ type + " " + JSON.stringify(json);
		for (let loader of jtloaders) {
			let ele = loader.loaderfn(this, json, false, ...args);
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
	/*public loadValue<T>(json: any, name:string):Value<T> {
		return this.loadPart('Value',json,name) as Value<T>;
	}*/

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

export interface LoaderLib {
	byjstype: Dictionary<ModelLoader[]>/*{
	 //TODO [index:JsTypename]
	 [index:string]:ModelElementLoader[]
	 }*/
	;
	bytypefield: Dictionary<ModelLoader>;
}

export interface ModelLoader {
	cat: EPartCategory;
	name: string;
	loaderfn(model: Model, json: any, strict: boolean, ...args:any[]):Part|null;
	typename?: string;
	objtypes?: JsTypename[];
}

