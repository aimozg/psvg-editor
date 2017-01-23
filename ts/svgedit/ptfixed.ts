import {ModelElement, ModelLoader, ModelPoint, DisplayMode} from "./api";
import {ModelContext} from "./_ctx";
import {TXY, SvgDragEvent, IXY} from "../svg";
import {ValueFloat} from "./vfloat";
import svg = require("../svg");

export const POINT_FIXED_TYPE = 'F';
export class FixedPoint extends ModelPoint {
	constructor(name: string|undefined,
				ctx: ModelContext,
				public readonly x:ValueFloat,
				public readonly y:ValueFloat) {
		super(name, ctx,'fixed_pt',[x,y]);
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

	protected updated(other: ModelElement, attr: string) {
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
	loaderfn:(ctx:ModelContext, json:any, strict:boolean)=>{
		let x:any,y:any,name:string|undefined = undefined;
		if (!strict) {
			const length = json['length'];
			if (length === 2) {
				[x,y] = [json[0],json[1]];
			} else if (length === 3) {
				[name,x,y] = [''+json[0], json[1], json[2]];
			} else return null;
		} else {
			[name,x,y] = [json['name'], json['pt'][0],json['pt'][1]];
		}
		return new FixedPoint(name,ctx,ctx.loadFloat('x',x),ctx.loadFloat('y',y))
	}
};
ModelContext.registerLoader(POINT_FIXED_LOADER);