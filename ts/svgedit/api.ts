import {TXY, IXY, DNode} from "../svg";
import {SVGItem, updateElement} from "../dom";
import {ModelPath} from "./path";
import {ModelParam} from "./param";
import {ModelContext} from "./_ctx";
import svg = require("../svg");
import dom = require("../dom");
import Dictionary = _.Dictionary;
import List = _.List;

export type JsTypename = 'object'|'function'|'undefined'|'string'|'number'|'boolean'|'symbol';

export type DisplayMode = 'edit'|'view';

export type EPartCategory = "Point"|"Node"|"Path"|"Model"|"Param"|"ValueFloat";
export type PartDependency = [string,Part];
export type ItemDeclaration = null|Part|[(Part|(()=>Part)),string|undefined];
export abstract class Part {
	private static GCounter = 1;
	public readonly gid = '' + Part.GCounter++;
	public id:number;
	public owner: Part;
	public readonly children: Part[] = [];
	protected dependants: PartDependency[] = [];
	public abstract get category(): EPartCategory;

	constructor(public name: string|undefined,
				public readonly ctx:ModelContext,
				items: ItemDeclaration[]) {
		this.id = ctx.id++;
		this.ctx.parts[this.id] = this;
		for (let icd of items) {
			if (icd === null) continue;
			let item:Part|(()=>Part);
			let dependency:string|undefined;
			if (icd instanceof Part) {
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

	protected dependOn(other: Part | (() => Part), attr: string) {
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

	public update(attr: string='*') {
		this.fireUpdated(attr);
	}

	protected fireUpdated(attr: string) {
		for (let dep of this.dependants) if (attr == '*' || dep[0] == '*' || attr == dep[0]) dep[1].updated(this, attr);
		this.ctx.updated(this, attr)
	}

	public treeNodeFull(): JSTreeNodeInit {
		let self = this.treeNodeSelf();
		self.children = this.children.filter(c=>!(c instanceof Value)).map(c => c.treeNodeFull());
		return self;
	}

	protected updated(other: Part, attr: string){}

	public abstract save():any;
}
export type EValueAttr = "*";
export abstract class Value<T> extends Part {
	constructor(name:string,
				ctx:ModelContext){
		super(name,ctx,[]);
	}
	public abstract get():T;
	public abstract editorElement():HTMLElement;
	public abstract save():any;
}

export abstract class ModelElement extends Part {
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

	public update(attr: string='*') {
		super.update(attr);
		this.redraw(attr,this.ctx.mode);
	}

	protected abstract draw(mode:DisplayMode): SVGElement|null;

	protected abstract redraw(attr: string,mode:DisplayMode);
}

export type EPointAttr = '*'|'pos';
export abstract class ModelPoint extends ModelElement {
	public xy: TXY = [0, 0];
	public g: SVGGElement|null;
	private use: SVGUseElement|null;

	public get category(): EPartCategory {
		return 'Point';
	}

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
export abstract class ModelNode extends ModelElement {

	public get category(): EPartCategory {
		return "Node";
	}

	constructor(name: string|any, ctx: ModelContext, items: ItemDeclaration[]) {
		super(name, ctx, items);
	}

	private _first: boolean;
	private _last: boolean;
	private _index: number;
	private _loaded = false;
	private _load() {
		const path = this.owner as ModelPath;
		this._index = path.nodes.indexOf(this);
		this._first = !path.closed && this._index == 0;
		this._last = !path.closed && this._index == path.nodes.length - 1;
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
		const path = this.owner as ModelPath;
		return path.nodes[(this.index + path.nodes.length - 1) % path.nodes.length];
	}

	protected nextNode(): ModelNode {
		const path = this.owner as ModelPath;
		return path.nodes[(this.index + 1) % path.nodes.length];
	}

	public abstract center(): IXY;

	public abstract save(): any;

	public abstract toDNode(): DNode;
}


export type EModelAttr = "*";
export class Model extends ModelElement {
	public readonly g: SVGGElement = SVGItem('g', {'class': 'model'});

	public get category(): EPartCategory {
		return 'Model';
	}

	constructor(name:string|undefined,ctx:ModelContext,
				private readonly paths:ModelPath[],
				private readonly params:ModelParam[]
	) {
		super(name,ctx,
			paths.map(p=>[p,'*'] as [ModelPath,string]));
		ctx.doPostload();
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

	public static load(ctx:ModelContext, json: any): Model {
		return new Model(json['name'] || 'unnamed', ctx,
			(json['paths'] || []).map(j => ctx.loadPath(j)),
			(json['params'] || []).map(j => ctx.loadParam(j))
		);
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

	public clone(ctx:ModelContext):Model {
		// TODO optimize
		return Model.load(ctx,this.save());
	}

}

export abstract class ModelLoader {
	constructor(
		public readonly cat: EPartCategory,
		public readonly name: string,
		public readonly typename: string|null,
		public readonly objtypes: JsTypename[] = []
	) {
	}
	abstract loadStrict(ctx:ModelContext,json:any,...args:any[]):Part;
	//noinspection JSMethodCanBeStatic
	loadRelaxed(ctx:ModelContext,json:any,...args:any[]):Part|null {
		return this.loadStrict(ctx,json,...args);
	}
}

