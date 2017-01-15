import {ModelPoint, EPointAttr, Model, ModelElementLoader} from "./api";
import {TXY, solve2, vsub, vrot90, IXY, vlinj} from "../svg";
import svg = require("../svg");

export const POINT_FROM_NORMAL_TYPE = 'N';
export const POINT_FROM_NORMAL_CLASS = 'pt_norm';
export class PointFromNormal extends ModelPoint {
	constructor(name: string|undefined,
				public readonly pt0: ModelPoint,
				public readonly pt1: ModelPoint,
				public alpha: number,
				public beta: number) {
		super(name, POINT_FROM_NORMAL_CLASS)
	}

	protected attachChildren() {
		this.pt0.attach(this, "ref");
		this.pt1.attach(this, "ref");
		this.dependOn(this.pt0, "pos");
		this.dependOn(this.pt1, "pos");
	}

	protected draw(): SVGGElement {
		super.draw();
		for (let p of [this.pt0,this.pt1]) this.g.appendChild(p.display());
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

	repr(): string {
		return this.alpha.toFixed(3) + ' x (' + this.pt0.repr() + ':' + this.pt1.repr()+') + '+
			this.beta.toFixed(3) + ' x NORMAL';
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
export const POINT_FROM_NORMAL_LOADER:ModelElementLoader<PointFromNormal> = {
	cat:'Point',
	typename:POINT_FROM_NORMAL_TYPE,
	loaderfn:(model:Model,json:any)=> new PointFromNormal(json['name'],
		model.loadPoint(json['pt0']),
		model.loadPoint(json['pt1']),
		+json['alpha'], +json['beta'])
};
Model.registerLoader(POINT_FROM_NORMAL_LOADER);