import {ModelPoint, EPointAttr, Model, ModelLoader, CModelPoint} from "./api";
import {TXY, solve2, vsub, vrot90, IXY, vlinj} from "../svg";
import svg = require("../svg");

export const POINT_FROM_NORMAL_TYPE = 'N';
export const POINT_FROM_NORMAL_CLASS = 'pt_norm';
export class PointFromNormal extends CModelPoint<ModelPoint> {
	constructor(name: string|undefined,
				public readonly pt0: ModelPoint,
				public readonly pt1: ModelPoint,
				public alpha: number,
				public beta: number) {
		super(POINT_FROM_NORMAL_LOADER,name, POINT_FROM_NORMAL_CLASS)
	}

	protected attachChildren() {
		this.attachAll([this.pt0,this.pt1], "pos");
	}

	protected draw(): SVGGElement {
		super.draw();
		for (let p of [this.pt0,this.pt1]) this.g.appendChild(p.display("pt_ref"));
		return this.g;
	}

	protected updated(other: ModelPoint, attr: EPointAttr) {
		this.update("pos");
	}

	protected fcalculate(): TXY {
		let a = this.pt0.calculate();
		let b = this.pt1.calculate();
		return norm2fixed(a,b,this.alpha,this.beta);
	}

	save(): any {
		return {
			type: POINT_FROM_NORMAL_TYPE,
			name: this.name,
			pt0: this.pt0.save(),
			pt1: this.pt1.save(),
			alpha: this.alpha,
			beta: this.beta
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
export const POINT_FROM_NORMAL_LOADER:ModelLoader = {
	cat:'Point',
	name:'PointFromNormal',
	typename:POINT_FROM_NORMAL_TYPE,
	loaderfn:(model:Model,json:any)=> new PointFromNormal(json['name'],
		model.loadPoint(json['pt0']),
		model.loadPoint(json['pt1']),
		+json['alpha'], +json['beta'])
};
Model.registerLoader(POINT_FROM_NORMAL_LOADER);