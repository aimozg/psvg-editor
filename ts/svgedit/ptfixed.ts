import {ModelElement, Model, ModelLoader, CModelPoint, DisplayMode, Value} from "./api";
import {TXY, SvgDragEvent, IXY} from "../svg";
import {ValueFloat} from "./vfloat";
import svg = require("../svg");

export const POINT_FIXED_TYPE = 'F';
export class FixedPoint extends CModelPoint<any> {
	public readonly x:ValueFloat;
	public readonly y:ValueFloat;
	constructor(name: string|undefined,
				[xjson,yjson]:[any,any]) {
		super(POINT_FIXED_LOADER, name, 'fixed_pt');
		this.x = ValueFloat.load(this,'x',xjson);
		this.y = ValueFloat.load(this,'y',yjson);
	}

	protected draw(mode:DisplayMode): SVGGElement|null {
		let g = super.draw(mode);
		if (mode == "edit" && g) {
			svg.makeDraggable(g);
			g.addEventListener('sdragstart', (e: SvgDragEvent) => {
				let pos = this.calculate();
				e.start.x = pos[0];
				e.start.y = pos[1];
			});
			g.addEventListener('sdrag', (e: SvgDragEvent) => {
				e.preventDefault();
				this.set(e.start.x + e.movement.x, e.start.y + e.movement.y);
			});
		}
		return g;
	}

	public set(tgt: IXY): this;
	public set(x: number, y: number): this;
	public set(arg1: IXY|number, arg2?: number): this {
		if (typeof arg1 == 'number') {
			this.x.set(arg1, true);
			this.y.set(arg2!!, true);
		} else {
			this.x.set(arg1[0], true);
			this.y.set(arg1[1], true);
		}
		this.update('pos');
		return this;
	}

	protected fcalculate(): TXY {
		return [this.x.get(), this.y.get()];
	}

	protected attachChildren() {
	}

	protected updated(other: ModelElement, attr: string) {
	}

	public valueUpdated<T>(value: Value<T>) {
		this.set(this.x.get(),this.y.get());
	}

	public save(): any {
		if (this.name === undefined) return [this.x.get(), this.y.get()];
		return [this.name, this.x.get(), this.y.get()];
		/*
		 return {
		 name: this.name,
		 type: POINT_FIXED_TYPE,
		 pt: [this.pt[0], this.pt[1]]
		 };
		 */
	}

}
export const POINT_FIXED_LOADER:ModelLoader = {
	cat:'Point',
	name:'FixedPoint',
	typename:POINT_FIXED_TYPE,
	objtypes:['object'], // arrays supported
	loaderfn:(model:Model,json:any,strict:boolean)=>{
		if (!strict) {
			const length = json['length'];
			if (length === 2) return new FixedPoint(undefined, [json[0], json[1]]);
			if (length === 3) return new FixedPoint('' + json[0], [json[1], json[2]]);
			return null;
		}
		return new FixedPoint(json['name'], [json['pt'][0],json['pt'][1]])
	}
};
Model.registerLoader(POINT_FIXED_LOADER);