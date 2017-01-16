import svg = require("../svg");
import dom = require("../dom");
import {ModelPoint, EPointAttr, Model, ModelLoader, CModelPoint} from "./api";
import {TXY} from "../svg";
import {SVGItem} from "../dom";

export const POINT_AT_PROJECTION_TYPE = 'PROJ';
export const POINT_AT_PROJECTION_CLASS = 'pt_proj';
export class PointAtProjection extends CModelPoint<ModelPoint> {
	protected lab: SVGLineElement;
	protected lpq: SVGLineElement;
	constructor(name: string|undefined,
				public a:ModelPoint,
				public b:ModelPoint,
				public p:ModelPoint) {
		super(POINT_AT_PROJECTION_LOADER,name,POINT_AT_PROJECTION_CLASS)
	}
	
	protected attachChildren() {
		for (let pt of [this.a,this.b,this.p]) {
			this.attach(pt,"ref");
			this.dependOn(pt,"pos");
		}
	}

	protected updated(other: ModelPoint, attr: EPointAttr) {
		this.update("pos");
	}

	protected fcalculate(): TXY {
		return svg.ptproj(this.a.calculate(),this.b.calculate(),this.p.calculate());
	}

	protected draw(): SVGGElement {
		super.draw();
		for (let pt of [this.a,this.b,this.p]) {
			this.g.appendChild(pt.display());
		}
		this.lab = undefined;
		this.lpq = undefined;
		return this.g;
	}
	
	protected redraw(attr: EPointAttr): any {
		super.redraw(attr);
		let [a,b,p,q] = [this.a.calculate(),this.b.calculate(),this.p.calculate(),this.calculate()];
		if (this.lab) this.g.removeChild(this.lab);
		if (this.lpq) this.g.removeChild(this.lpq);
		this.lab = this.lpq = undefined;
		this.lab = SVGItem('line', {
			x1: a[0], x2: b[0],
			y1: a[1], y2: b[1], 'class': 'lineref'
		});
		this.g.insertBefore(this.lab, this.g.firstChild);
		this.lpq = SVGItem('line', {
			x1: p[0], x2: q[0],
			y1: p[1], y2: q[1], 'class': 'handle2'
		});
		this.g.insertBefore(this.lpq, this.g.firstChild);
	}


	repr(): string {
		return POINT_AT_PROJECTION_TYPE+" ("+this.a.repr()+":"+this.b.repr()+" X "+this.p.repr()+")";
	}

	save(): any {
		return {
			type:POINT_AT_PROJECTION_TYPE,
			name:this.name,
			a:this.a.save(),
			b:this.b.save(),
			p:this.p.save()
		}
	}


}
export const POINT_AT_PROJECTION_LOADER:ModelLoader = {
	cat:'Point',
	name:'PointAtProjection',
	typename:POINT_AT_PROJECTION_TYPE,
	loaderfn:(model:Model,json:any)=>new PointAtProjection(json['name'],
		model.loadPoint(json['a']),
		model.loadPoint(json['b']),
		model.loadPoint(json['p'])
	)
};
Model.registerLoader(POINT_AT_PROJECTION_LOADER);
