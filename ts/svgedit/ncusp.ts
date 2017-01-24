import {ModelPoint, EPointAttr, ModelLoader, DisplayMode} from "./api";
import {ModelContext} from "./_ctx";
import {TXY} from "../svg";
import {CommonNode} from "./ncommon";
import svg = require("../svg");

export const NODE_CUSP_TYPE = 'cusp';
export class CuspNode extends CommonNode {
	constructor(name: string|undefined,
				ctx: ModelContext,
				pos: ModelPoint,
				public h1: ModelPoint|null,
				public h2: ModelPoint|null) {
		super(name, ctx, pos, 'cusp_node', [h1, h2]);
	}

	protected updated(other: ModelPoint, attr: EPointAttr) {
		if (other == this.pos) this.update("pos");
		else  this.update("handle");
	}

	protected draw(mode: DisplayMode): SVGElement|null {
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
		let [x, y] = this.pos.calculate();
		return [
			this.h1 ? this.h1.calculate() : [x, y],
			this.h2 ? this.h2!!.calculate() : [x, y]
		];
	}

	public save(): any {
		return {
			type: NODE_CUSP_TYPE,
			pos: this.pos.save(),
			name: this.name,
			handle1: this.h1 ? this.h1.save() : undefined,
			handle2: this.h2 ? this.h2.save() : undefined
		}
	}

}
export const NODE_CUSP_LOADER: ModelLoader = new class extends ModelLoader {
	loadStrict(ctx: ModelContext, json: any): CuspNode {
		return new CuspNode(json['name'], ctx,
			ctx.loadPoint(json['pos']),
			json['handle1'] ? ctx.loadPoint(json['handle1']) : null,
			json['handle2'] ? ctx.loadPoint(json['handle2']) : null
		)
	}
}('Node', 'CuspNode', NODE_CUSP_TYPE);
ModelContext.registerLoader(NODE_CUSP_LOADER);
