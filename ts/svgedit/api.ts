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
	public updated<A2 extends string>(part:CPart<A2>,attr:A2) {
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
export type Part = CPart<any>;
export abstract class CPart<ATTR extends string> {
	private static GCounter = 1;
	public readonly gid = '' + CPart.GCounter++;
	public id:number;
	public owner: Part;
	public readonly children: Part[];

	constructor(public name: string|undefined,
				public readonly ctx:ModelContext,
				public readonly values: Value<any>[]) {
		for (let v of values) v.owner = this;
		this.id = ctx.id++;
	}

	protected uhref():string {
		return '#svg_'+this.classname;
	}

	public get classname():string {
		return this.constructor['name']||'ModelPart';
	}

	protected attached(parent: Part) {
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
export type EValueAttr = "*";
export abstract class Value<T> {
	public owner:Part;
	constructor(public readonly name:string){
	}
	public get index():number { return this.owner.values.indexOf(this); }
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

export type ModelElement = CModelElement<any,any,any>;
export type ItemDeclaration<CHILD extends CPart<any>> = [(CHILD|(()=>CHILD)),string|undefined];
export abstract class CModelElement<
	PARENT extends Part,
	CHILD extends ModelElement,
	ATTR extends string> extends CPart<ATTR> {
	public owner: PARENT;
	public graphic: SVGElement|null;
	protected dependants: [string, ModelElement][] = [];
	public readonly children: CHILD[] = [];

	constructor(name: string|any,
				ctx: ModelContext,
				items: ItemDeclaration<CHILD>[],
				values: Value<any>[]) {
		super(name, ctx, values);
		for (let icd of items) {
			const [item,dependency] = icd;
			if (typeof item != 'function') this.attach(item);
			if (dependency) this.dependOn(item,dependency);
		}
	}

	public attached(parent: PARENT) {
		super.attached(parent);
		this.ctx.parts[this.id] = this;
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
		if (typeof other === 'function') this.ctx.queuePostload(() => other().dependants.push([attr, this]));
		else other.dependants.push([attr, this]);
	}

	public update(attr: ATTR|"*"='*') {
		super.update(attr);
		this.redraw(attr,this.ctx.mode);
		this.fireUpdated(attr);
	}

	protected fireUpdated(attr: ATTR|"*") {
		for (let dep of this.dependants) if (attr == '*' || dep[0] == '*' || attr == dep[0]) dep[1].updated(this, attr);
		this.ctx.updated(this, attr)
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
				ctx: ModelContext,
				public readonly cssclass: string,
				items: ItemDeclaration<CHILD>[],
				values: Value<any>[]) {
		super(name, ctx, items, values);
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
export abstract class CModelNode<CHILD extends ModelElement> extends CModelElement<ModelPath,CHILD,ENodeAttr> {

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
	public readonly g: SVGGElement = SVGItem('g', {'class': 'model'});

	constructor(name:string|undefined,ctx:ModelContext,
				private readonly paths:ModelPath[],
				private readonly params:ModelParam[]
	) {
		super(name,ctx,
			paths.map(p=>[p,'*'] as [ModelPath,string]), []);
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

