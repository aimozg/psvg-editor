import {ModelPoint, EPointAttr, Model, ModelLoader, DisplayMode} from "./api";
import {TXY} from "../svg";
import {CommonNode} from "./ncommon";
import svg = require("../svg");

export const NODE_CUSP_TYPE = 'cusp';
export class CuspNode extends CommonNode<ModelPoint> {
	constructor(name:string|undefined,
				pos: ModelPoint,
				public h1: ModelPoint|null,
				public h2: ModelPoint|null) {
		super(name,pos, 'cusp_node', []);
	}

	protected updated(other: ModelPoint, attr: EPointAttr) {
		if (other == this.pos) this.update("pos");
		else  this.update("handle");
	}

	protected attachChildren() {
		super.attachChildren();
		this.attachAll([this.h1, this.h2], "pos");
	}

	protected draw(mode:DisplayMode): SVGElement|null {
		let g = super.draw(mode);
		if (this.ctx.mode == "edit" && g) {
			if (!this.first && this.h1) {
				const h1 = this.h1.display("pt_handle");
				if (h1) g.appendChild(h1);
			}
			if (!this.last && this.h2) {
				const h2 = this.h2.display("pt_handle");
				if (h2) g.appendChild(h2);
			}
		}
		return this.g;
	}

	protected calcHandles(): [TXY, TXY] {
		return [
			this.first ? [NaN, NaN] : this.h1!!.calculate(),
			this.last ? [NaN, NaN] : this.h2!!.calculate()];
	}

	public save(): any {
		return {
			type: NODE_CUSP_TYPE,
			pos: this.pos.save(),
			name: this.name,
			handle1: this.first ? undefined : this.h1!!.save(),
			handle2: this.last ? undefined : this.h2!!.save()
		}
	}

}
export const NODE_CUSP_LOADER: ModelLoader = {
	cat:'Node',
	name:'CuspNode',
	typename:NODE_CUSP_TYPE,
	loaderfn:(m: Model, json: any)=> new CuspNode(json['name'],
		m.loadPoint(json['pos']),
		json['handle1'] ? m.loadPoint(json['handle1']) : null,
		json['handle2'] ? m.loadPoint(json['handle2']) : null
	)
};
Model.registerLoader(NODE_CUSP_LOADER);
