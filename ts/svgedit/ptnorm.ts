import {ModelPoint, EPointAttr, ModelLoader, DisplayMode, EValueAttr} from "./api";
import {ModelContext} from "./_ctx";
import {TXY, solve2, vsub, vrot90, IXY, vlinj} from "../svg";
import {ValueFloat} from "./vfloat";
import svg = require("../svg");

export const POINT_FROM_NORMAL_TYPE = 'N';
export const POINT_FROM_NORMAL_CLASS = 'pt_norm';
export class PointFromNormal extends ModelPoint {
	constructor(ctx:ModelContext,
				name:string|undefined,
				public readonly pt0: ModelPoint,
				public readonly pt1: ModelPoint,
				public readonly alpha: ValueFloat,
				public readonly beta: ValueFloat) {
		super(ctx,name, POINT_FROM_NORMAL_CLASS,[pt0,pt1,alpha,beta])
	}

	protected draw(mode:DisplayMode): SVGGElement|null {
		super.draw(mode);
		if (mode == 'edit' && this.g) {
			for (let p of [this.pt0, this.pt1]) {
				const g2 = p.display("pt_ref");
				if (g2) this.g.appendChild(g2);
			}
		}
		return this.g;
	}

	protected updated(other: ModelPoint|ValueFloat, attr: EPointAttr|EValueAttr) {
		this.update("pos");
	}

	protected fcalculate(): TXY {
		let a = this.pt0.calculate();
		let b = this.pt1.calculate();
		return norm2fixed(a,b,this.alpha.get(),this.beta.get());
	}

	save(): any {
		return {
			type: POINT_FROM_NORMAL_TYPE,
			name: this.name,
			pt0: this.pt0.save(),
			pt1: this.pt1.save(),
			alpha: this.alpha.save(),
			beta: this.beta.save()
		}
	}

}
export function fixed2norm(a:IXY, c:IXY, b:IXY):TXY{
	let v1 = vsub(b,a);
	let v2 = vrot90(v1);
	let tgt = vsub(c,a);
	return solve2(v1, v2, tgt);
}
export function norm2fixed(a:IXY, b:IXY, alpha:number, beta:number):TXY {
	let v1 = vsub(b,a);
	let v2 = vrot90(v1);
	return vlinj([1,a],[alpha,v1],[beta,v2]);
}
export const POINT_FROM_NORMAL_LOADER:ModelLoader = new class extends ModelLoader{
	loadStrict(ctx: ModelContext, json: any) {
		return new PointFromNormal(ctx,json['name'],
			ctx.loadPoint(json['pt0']),
			ctx.loadPoint(json['pt1']),
			ctx.loadFloat('tangent', json['alpha'], 0),
			ctx.loadFloat('normal', json['beta'], 0));
	}
}('Point','PointFromNormal',POINT_FROM_NORMAL_TYPE);
ModelContext.registerLoader(POINT_FROM_NORMAL_LOADER);