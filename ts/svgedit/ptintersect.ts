import svg = require("../svg");
import dom = require("../dom");
import {ModelPoint, EPointAttr, Model, ModelLoader, CModelPoint, DisplayMode} from "./api";
import {TXY} from "../svg";
import {SVGItem} from "../dom";

export const POINT_AT_INTERSECTION_TYPE = 'I';
export const POINT_AT_INTERSECTION_CLASS = 'pt_intsec';
export class PointAtIntersect extends CModelPoint<ModelPoint> {
	protected l1: SVGLineElement|null;
	protected l2: SVGLineElement|null;
	constructor(name: string|undefined,
				public a1:ModelPoint,
				public a2:ModelPoint,
				public b1:ModelPoint,
				public b2:ModelPoint) {
		super(name,POINT_AT_INTERSECTION_CLASS,[])
	}

	protected attachChildren() {
		this.attachAll([this.a1,this.a2,this.b1,this.b2],"pos");
	}

	protected updated(other: ModelPoint, attr: EPointAttr) {
		this.update("pos");
	}

	protected fcalculate(): TXY {
		return svg.v22intersect(
			this.a1.calculate(),this.a2.calculate(),
			this.b1.calculate(),this.b2.calculate())
	}


	protected draw(mode:DisplayMode): SVGGElement|null {
		super.draw(mode);
		if (mode == 'edit' && this.g) {
			for (let pt of [this.a1, this.a2, this.b1, this.b2]) {
				const g2 = pt.display("pt_ref");
				if (g2) this.g.appendChild(g2);
			}
			this.l1 = null;
			this.l2 = null;
		}
		return this.g;
	}

	protected redraw(attr: EPointAttr, mode: DisplayMode) {
		super.redraw(attr, mode);
		if (mode == "edit" && this.g) {
			let [a1, a2, b1, b2] = [this.a1.calculate(), this.a2.calculate(), this.b1.calculate(), this.b2.calculate()];
			if (this.l1) this.g.removeChild(this.l1);
			if (this.l2) this.g.removeChild(this.l2);
			this.l1 = this.l2 = null;
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
