import svg = require("../svg");
import dom = require("../dom");
import {ModelPoint, EPointAttr, ModelLoader, DisplayMode} from "./api";
import {ModelContext} from "./_ctx";
import {TXY} from "../svg";
import {SVGItem} from "../dom";

export const POINT_AT_PROJECTION_TYPE = 'PROJ';
export const POINT_AT_PROJECTION_CLASS = 'pt_proj';
export class PointAtProjection extends ModelPoint {
	protected lab: SVGLineElement|null;
	protected lpq: SVGLineElement|null;
	constructor(name: string|undefined,
				ctx: ModelContext,
				public a:ModelPoint,
				public b:ModelPoint,
				public p:ModelPoint) {
		super(name,ctx,POINT_AT_PROJECTION_CLASS,[a,b,p])
	}
	
	protected updated(other: ModelPoint, attr: EPointAttr) {
		this.update("pos");
	}

	protected fcalculate(): TXY {
		return svg.ptproj(this.a.calculate(),this.b.calculate(),this.p.calculate());
	}

	protected draw(mode:DisplayMode): SVGGElement|null {
		super.draw(mode);
		if (mode == 'edit' && this.g) {
			for (let pt of [this.a, this.b, this.p]) {
				const g2 = pt.display("pt_ref");
				if (g2) this.g.appendChild(g2);
			}
		}
		this.lab = null;
		this.lpq = null;
		return this.g;
	}
	
	protected redraw(attr: EPointAttr,mode:DisplayMode): any {
		super.redraw(attr,mode);
		if (mode == 'edit' && this.g) {
			let [a, b, p, q] = [this.a.calculate(), this.b.calculate(), this.p.calculate(), this.calculate()];
			if (this.lab) this.g.removeChild(this.lab);
			if (this.lpq) this.g.removeChild(this.lpq);
			this.lab = this.lpq = null;
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
	loaderfn:(ctx:ModelContext, json:any)=>new PointAtProjection(json['name'],ctx,
		ctx.loadPoint(json['a']),
		ctx.loadPoint(json['b']),
		ctx.loadPoint(json['p'])
	)
};
ModelContext.registerLoader(POINT_AT_PROJECTION_LOADER);
