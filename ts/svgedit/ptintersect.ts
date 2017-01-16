import svg = require("../svg");
import dom = require("../dom");
import {ModelPoint, EPointAttr, Model, ModelLoader, CModelPoint} from "./api";
import {TXY} from "../svg";
import {SVGItem} from "../dom";

export const POINT_AT_INTERSECTION_TYPE = 'I';
export const POINT_AT_INTERSECTION_CLASS = 'pt_intsec';
export class PointAtIntersect extends CModelPoint<ModelPoint> {
	protected l1: SVGLineElement;
	protected l2: SVGLineElement;
	constructor(name: string|undefined,
				public a1:ModelPoint,
				public a2:ModelPoint,
				public b1:ModelPoint,
				public b2:ModelPoint) {
		super(POINT_AT_INTERSECTION_LOADER,name,POINT_AT_INTERSECTION_CLASS)
	}

	protected attachChildren() {
		for (let pt of [this.a1,this.a2,this.b1,this.b2]) {
			this.attach(pt,"ref");
			this.dependOn(pt,"pos");
		}
	}

	protected updated(other: ModelPoint, attr: EPointAttr) {
		this.update("pos");
	}

	protected fcalculate(): TXY {
		return svg.v22intersect(
			this.a1.calculate(),this.a2.calculate(),
			this.b1.calculate(),this.b2.calculate())
	}


	protected draw(): SVGGElement {
		super.draw();
		for (let pt of [this.a1,this.a2,this.b1,this.b2]) {
			this.g.appendChild(pt.display());
		}
		this.l1 = undefined;
		this.l2 = undefined;
		return this.g;
	}

	protected redraw(attr: EPointAttr) {
		super.redraw(attr);
		let [a1,a2,b1,b2] = [this.a1.calculate(),this.a2.calculate(),this.b1.calculate(),this.b2.calculate()];
		if (this.l1) this.g.removeChild(this.l1);
		if (this.l2) this.g.removeChild(this.l2);
		this.l1 = this.l2 = undefined;
		this.l1 = SVGItem('line', {
			x1: a1[0], x2: a2[0],
			y1: a1[1], y2: a2[1], 'class': 'handle1'
		});
		this.g.insertBefore(this.l1, this.g.firstChild);
		this.l2 = SVGItem('line', {
			x1: b1[0], x2: b2[0],
			y1: b1[1], y2: b2[1], 'class': 'handle2'
		});
		this.g.insertBefore(this.l2, this.g.firstChild);
	}

	repr(): string {
		return POINT_AT_INTERSECTION_TYPE+" (" + this.a1.repr() + ":" + this.a2.repr() + " X " + this.b1.repr() + ":" + this.b2.repr() + ")"
	}

	save(): any {
		return {
			type:POINT_AT_INTERSECTION_TYPE,
			name:this.name,
			a1:this.a1.save(),
			a2:this.a2.save(),
			b1:this.b1.save(),
			b2:this.b2.save()
		}
	}
}
export const POINT_AT_INTERSECTION_LOADER:ModelLoader = {
	cat:'Point',
	name:'PointAtIntersection',
	typename:POINT_AT_INTERSECTION_TYPE,
	loaderfn:(model:Model,json:any)=>new PointAtIntersect(json['name'],
		model.loadPoint(json['a1']),
		model.loadPoint(json['a2']),
		model.loadPoint(json['b1']),
		model.loadPoint(json['b2']))
};
Model.registerLoader(POINT_AT_INTERSECTION_LOADER);
