import {TXY, IXY, DNode} from "../svg";
import {SVGItem, updateElement} from "../dom";
import {ModelPath} from "./path";
import {ModelParam} from "./param";
import {ValueFloat} from "./vfloat";
import svg = require("../svg");
import dom = require("../dom");
import Dictionary = _.Dictionary;
import List = _.List;

export type JsTypename = 'object'|'function'|'undefined'|'string'|'number'|'boolean'|'symbol';

export type DisplayMode = 'edit'|'view';

export class ModelContext {
	public id: number = 0;
	public readonly parts: {
		[id: string]: Part;
	} = {};
	public onUpdate: (obj: Part,attr:string) => any = (x => void(0));
	private readonly postloadQueue: (() => any)[] = [];
	constructor(
		public readonly mode:DisplayMode
	) {
	}

	public findPoint(name:string):ModelPoint|null {
		return _.find(this.parts, x => (x instanceof CModelPoint && x.name == name)) as ModelPoint;
	}
	public updated<A2 extends string>(part:CPart<any,A2>,attr:A2) {
		this.onUpdate(part,attr);
	}
	private loadPart(cat: EPartCategory, json: any, ...args:any[]): Part {
		const type = typeof json;
		let loaders: LoaderLib = ModelContext.loaders[cat] || {
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
	public loadParam(json: any): ModelParam {
		return this.loadPart('Param', json) as ModelParam;
	}
	public loadFloat(name:string,json:any,def?:number):ValueFloat {
		return ValueFloat.load(name,this,json,def);
	}
	public static registerLoader(loader: ModelLoader) {
		let lib: LoaderLib = ModelContext.loaders[loader.cat] || {
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
		ModelContext.loaders[loader.cat] = lib;
	}
	public doPostload() {
		for (let toa of this.postloadQueue) toa();
		this.postloadQueue.splice(0);
	}
	public queuePostload(code: () => any) {
		this.postloadQueue.push(code);
	}

	private static loaders: {
		//TODO [index:EModelElementCategory]: {
		[index: string]: LoaderLib;
	} = {}
}

export type EPartCategory = "Point"|"Node"|"Path"|"Model"|"Param"|"Value";
export type Part = CPart<any,string>;
export type PartDependency<A extends string> = [A,CPart<any,A>];
export type ItemDeclaration = null|Part|[(Part|(()=>Part)),string|undefined];
export abstract class CPart<PARENT extends Part,ATTR extends string> {
	private static GCounter = 1;
	public readonly gid = '' + CPart.GCounter++;
	public id:number;
	public owner: PARENT;
	public readonly children: Part[] = [];
	protected dependants: PartDependency<string>[] = [];

	constructor(public name: string|undefined,
				public readonly ctx:ModelContext,
				items: ItemDeclaration[]) {
		this.id = ctx.id++;
		this.ctx.parts[this.id] = this;
		for (let icd of items) {
			if (icd === null) continue;
			let item:Part|(()=>Part);
			let dependency:string|undefined;
			if (icd instanceof CPart) {
				item = icd;
				dependency = '*';
			} else {
				[item,dependency] = icd;
			}
			if (typeof item != 'function') {
				if (item.owner == null) {
					this.children.push(item);
					item.owner = this;
				}
			}
			if (dependency) this.dependOn(item,dependency);
		}
	}

	protected uhref():string {
		return '#svg_'+this.classname;
	}

	public get classname():string {
		return this.constructor['name']||'ModelPart';
	}

	protected dependOn<A2 extends string>(other: CPart<any,A2> | (() => CPart<any,A2>),
										  attr: A2) {
		if (typeof other === 'function') {
			this.ctx.queuePostload(() => this.dependOn(other(),attr))
		} else if (other != this) {
			other.dependants.push([attr, this]);
		} else throw "Self-dependency in "+this.treeNodeText()
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
		this.fireUpdated(attr);
	}

	protected fireUpdated(attr: ATTR|"*") {
		for (let dep of this.dependants) if (attr == '*' || dep[0] == '*' || attr == dep[0]) dep[1].updated(this, attr);
		this.ctx.updated(this, attr)
	}

	public treeNodeFull(): JSTreeNodeInit {
		let self = this.treeNodeSelf();
		self.children = this.children.filter(c=>!(c instanceof Value)).map(c => c.treeNodeFull());
		return self;
	}

	protected updated<A2 extends string>(other: CPart<any,A2>, attr: A2){}

	public abstract save():any;
}
export type EValueAttr = "*";
export abstract class Value<T> extends CPart<Part,EValueAttr> {
	constructor(name:string,
				ctx:ModelContext){
		super(name,ctx,[]);
	}
	public abstract get():T;
	public abstract editorElement():HTMLElement;
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
ModelCtx.registerLoader(VALUE_FIXNUM_LOADER);*/

export type ModelElement = CModelElement<any,string>;
export abstract class CModelElement<
	PARENT extends Part,
	ATTR extends string> extends CPart<PARENT,ATTR> {
	public graphic: SVGElement|null;

	constructor(name: string|any,
				ctx: ModelContext,
				items: ItemDeclaration[]) {
		super(name, ctx, items);
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


	public update(attr: ATTR|"*"='*') {
		super.update(attr);
		this.redraw(attr,this.ctx.mode);
	}

	protected abstract draw(mode:DisplayMode): SVGElement|null;

	protected abstract redraw(attr: ATTR|"*",mode:DisplayMode);

}

export type EPointAttr = '*'|'pos';
export type ModelPoint = CModelPoint;
export abstract class CModelPoint extends CModelElement<any,EPointAttr> {
	public xy: TXY = [0, 0];
	public g: SVGGElement|null;
	private use: SVGUseElement|null;

	constructor(name: string|undefined,
				ctx: ModelContext,
				public readonly cssclass: string,
				items: ItemDeclaration[]) {
		super(name, ctx, items);
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
export type ModelNode = CModelNode;
export abstract class CModelNode extends CModelElement<ModelPath,ENodeAttr> {
	constructor(name: string|any, ctx: ModelContext, items: ItemDeclaration[]) {
		super(name, ctx, items);
	}

	private _first: boolean;
	private _last: boolean;
	private _index: number;
	private _loaded = false;
	private _load() {
		this._index = this.owner.nodes.indexOf(this);
		this._first = !this.owner.closed && this._index == 0;
		this._last = !this.owner.closed && this._index == this.owner.nodes.length - 1;
		this._loaded = true;
	}

	get index(): number {
		if (!this._loaded) this._load();
		return this._index;
	}
	get last(): boolean {
		if (!this._loaded) this._load();
		return this._last;
	}
	get first(): boolean {
		if (!this._loaded) this._load();
		return this._first;
	}

	protected prevNode(): ModelNode {
		let n = this.owner.nodes.length;
		return this.owner.nodes[(this.index + n - 1) % n];
	}

	protected nextNode(): ModelNode {
		let n = this.owner.nodes.length;
		return this.owner.nodes[(this.index + 1) % n];
	}

	public abstract center(): IXY;

	public abstract save(): any;

	public abstract toDNode(): DNode;
}


export type EModelAttr = "*";
export class Model extends CModelElement<any,EModelAttr> {
	public readonly g: SVGGElement = SVGItem('g', {'class': 'model'});

	constructor(name:string|undefined,ctx:ModelContext,
				private readonly paths:ModelPath[],
				private readonly params:ModelParam[]
	) {
		super(name,ctx,
			paths.map(p=>[p,'*'] as [ModelPath,string]));
	}

	public save(): any {
		return {
			name: this.name,
			paths: this.paths.map(p => p.save()),
			params: this.params.map(p => p.save())
		}
	}

	public updated(other: ModelElement, attr: string) {
	}

	public static load(mode:DisplayMode, json: any): Model {
		const ctx = new ModelContext(mode);
		let m = new Model(json['name']||'unnamed',ctx,
			(json['paths']||[]).map(j=>ctx.loadPath(j)),
			(json['params']||[]).map(j=>ctx.loadParam(j))
		);
		ctx.doPostload();
		return m;
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


	/*public loadValue<T>(json: any, name:string):Value<T> {
		return this.loadPart('Value',json,name) as Value<T>;
	}*/

	public clone(mode:DisplayMode):Model {
		// TODO optimize
		return Model.load(mode,this.save());
	}

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
	loaderfn(ctx: ModelContext, json: any, strict: boolean, ...args:any[]):Part|null;
	typename?: string;
	objtypes?: JsTypename[];
}

