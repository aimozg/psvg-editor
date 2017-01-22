import {ModelElement, Model, ModelLoader, CModelPoint, DisplayMode} from "./api";
import {TXY, SvgDragEvent, IXY} from "../svg";
import svg = require("../svg");

export const POINT_FIXED_TYPE = 'F';
export class FixedPoint extends CModelPoint<any> {
	constructor(name: string|undefined, public readonly pt: TXY) {
		super(POINT_FIXED_LOADER, name, 'fixed_pt');
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
			this.pt[0] = arg1;
			this.pt[1] = arg2!!;
		} else {
			this.pt[0] = arg1[0];
			this.pt[1] = arg1[1];
		}
		this.update('pos');
		return this;
	}

	protected fcalculate(): TXY {
		return [this.pt[0], this.pt[1]];
	}

	protected attachChildren() {
	}

	protected updated(other: ModelElement, attr: string) {
	}

	public save(): any {
		if (this.name === undefined) return [this.pt[0], this.pt[1]];
		return [this.name, this.pt[0], this.pt[1]];
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
			if (length === 2) return new FixedPoint(undefined, [+json[0], +json[1]]);
			if (length === 3) return new FixedPoint('' + json[0], [+json[1], +json[2]]);
			return null;
		}
		return new FixedPoint(json['name'], [+json['pt'][0], +json['pt'][1]])
	}
};
Model.registerLoader(POINT_FIXED_LOADER);