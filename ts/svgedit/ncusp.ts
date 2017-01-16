import {CommonNode, ModelPoint, EPointAttr, Model, ModelLoader} from "./api";
import {TXY} from "../svg";
import svg = require("../svg");

export const NODE_CUSP_TYPE = 'cusp';
export class CuspNode extends CommonNode<ModelPoint> {
	constructor(name:string|undefined,
				pos: ModelPoint,
				public h1: ModelPoint|undefined,
				public h2: ModelPoint|undefined) {
		super(NODE_CUSP_LOADER,name,pos, 'cusp_node');
	}

	protected updated(other: ModelPoint, attr: EPointAttr) {
		switch (other.role) {
			case 'node':
				return this.update("pos");
			case 'handle':
				return this.update("handle");
		}
	}

	protected attachChildren() {
		super.attachChildren();
		this.attachAll([this.h1, this.h2], "handle", "pos");
	}

	protected draw(): SVGElement {
		super.draw();
		if (!this.first) this.g.appendChild(this.h1.display());
		if (!this.last) this.g.appendChild(this.h2.display());
		return this.g;
	}

	protected calcHandles(): [TXY, TXY] {
		return [
			this.first ? [NaN, NaN] : this.h1.calculate(),
			this.last ? [NaN, NaN] : this.h2.calculate()];
	}

	points(): ModelPoint[] {
		return [this.pos].concat(this.first ? [] : this.h1, this.last ? [] : this.h2);
	}

	public save(): any {
		return {
			type: NODE_CUSP_TYPE,
			pos: this.pos.save(),
			name: this.name,
			handle1: this.first ? undefined : this.h1.save(),
			handle2: this.last ? undefined : this.h2.save()
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
