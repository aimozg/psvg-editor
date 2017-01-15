import {CommonNode, ModelPoint, ModelElement, EPointAttr, Model, ModelElementLoader} from "./api";
import {TXY} from "../svg";
import svg = require("../svg");

export const NODE_CUSP_TYPE = 'cusp';
export class CuspNode extends CommonNode {
	constructor(name:string|undefined,
				pos: ModelPoint,
				public h1: ModelPoint|undefined,
				public h2: ModelPoint|undefined) {
		super(name,pos, 'cusp_node');
	}

	protected updated(other: ModelElement<any, any, EPointAttr>, attr: EPointAttr) {
		if (other instanceof ModelPoint) {
			switch (other.role) {
				case 'node':
					return this.update("pos");
				case 'handle':
					return this.update("handle");
			}
		}
	}

	protected attachChildren() {
		super.attachChildren();
		if (this.h1) {
			this.h1.attach(this, "handle");
			this.dependOn(this.h1, "pos");
		}
		if (this.h2) {
			this.h2.attach(this, "handle");
			this.dependOn(this.h2, "pos");
		}
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

	public repr(): string {
		return 'cusp';
	}
}
export const NODE_CUSP_LOADER: ModelElementLoader<CuspNode> = {
	cat:'Node',
	typename:NODE_CUSP_TYPE,
	loaderfn:(m: Model, json: any)=> new CuspNode(json['name'],
		m.loadPoint(json['pos']),
		json['handle1'] ? m.loadPoint(json['handle1']) : null,
		json['handle2'] ? m.loadPoint(json['handle2']) : null
	)
};
Model.registerLoader(NODE_CUSP_LOADER);
