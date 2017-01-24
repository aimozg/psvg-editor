import svg = require("../svg");
import dom = require("../dom");
import Dictionary = _.Dictionary;
import List = _.List;
import {Part, DisplayMode, ModelPoint, EPartCategory, ModelNode, ModelLoader} from "./api";
import {ModelPath} from "./path";
import {ModelParam} from "./param";
import {ValueFloat} from "./vfloat";
class LoaderLib {
	all: ModelLoader[] = [];
	byjstype: Dictionary<ModelLoader[]> = {};/*{
	 //TODO [index:JsTypename]
	 [index:string]:ModelElementLoader[]
	 }*/
	bytypefield: Dictionary<ModelLoader> = {};
	constructor(){}
	public put(loader:ModelLoader) {
		this.all.push(loader);
		if (loader.typename) {
			this.bytypefield[loader.typename] = loader;
		}
		for (let ot of loader.objtypes || []) {
			let byjt: ModelLoader[] = this.byjstype[ot] || [];
			byjt.push(loader);
			this.byjstype[ot] = byjt;
		}
	}
}

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
		return _.find(this.parts, x => (x instanceof ModelPoint && x.name == name)) as ModelPoint;
	}
	public updated(part:Part,attr:string) {
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
			if (tfloader) return tfloader.loadStrict(this, json, ...args)!!;
		}
		let jtloaders = loaders.byjstype[type];
		if (!jtloaders || jtloaders.length == 0) throw "No loaders for " + cat + " "
		+ type + " " + JSON.stringify(json);
		for (let loader of jtloaders) {
			let ele = loader.loadRelaxed(this, json, ...args);
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
	public loadFloat(name:string,json:any,def:number|undefined=undefined,min:number=-Infinity,max:number=Infinity):ValueFloat {
		return ValueFloat.load(name,this,json,def,min,max);
	}
	public static registerLoader(loader: ModelLoader) {
		let lib: LoaderLib = ModelContext.loaders[loader.cat] || new LoaderLib();
		lib.put(loader);
		ModelContext.loaders[loader.cat] = lib;
	}
	public doPostload() {
		for (let toa of this.postloadQueue) toa();
		this.postloadQueue.splice(0);
	}
	public queuePostload(code: () => any) {
		this.postloadQueue.push(code);
	}
	public static loadersFor(category:EPartCategory):ModelLoader[] {
		return ModelContext.loaders[category].all.concat([]);
	}
	private static loaders: {
		//TODO [index:EModelElementCategory]: {
		[index: string]: LoaderLib;
	} = {}
}
