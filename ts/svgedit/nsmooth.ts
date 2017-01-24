import {TXY} from "../svg";
import {ModelPoint, EPointAttr, ENodeAttr, ModelLoader, ModelNode, ItemDeclaration} from "./api";
import {CommonNode} from "./ncommon";
import {ValueFloat} from "./vfloat";
import {ModelContext} from "./_ctx";
import svg = require("../svg");

export const NODE_SMOOTH_TYPE = 'smooth';
export class SmoothNode extends CommonNode {
	constructor(ctx:ModelContext,
				name:string|undefined,
				ownOrigin: ModelPoint|null,
				pos: ModelPoint,
				public readonly abq: ValueFloat,
				public readonly acq: ValueFloat,
				public readonly rot: ValueFloat) {
		super(ctx, name,ownOrigin,pos, 'smooth_node',[
			pos,abq,acq,rot,
			[()=>this.prevNode(),'pos'] as ItemDeclaration,
			[()=>this.nextNode(),'pos'] as ItemDeclaration
		]);
	}

	protected updated(other: ModelPoint|ModelNode, attr: EPointAttr|ENodeAttr) {
		if (other instanceof ModelPoint) this.update("*");
		if (other instanceof ModelNode && (attr == "pos" || attr == "*") || (other instanceof ValueFloat)) {
			this.update("handle");
		}
	}

	protected calcHandles(): [TXY, TXY] {
		return svg.smoothhandles([
			this.prevNode().center(),
			this.center(),
			this.nextNode().center()
		], this.abq.get(), this.acq.get(), this.rot.get())
	}

	public save(): any {
		return {
			type: NODE_SMOOTH_TYPE,
			name: this.name,
			origin: this.ownOrigin?this.ownOrigin.save():undefined,
			pos: this.pos.save(),
			b: this.abq.save(),
			c: this.acq.save(),
			rot: this.rot.save()
		}
	}

}
export const NODE_SMOOTH_LOADER: ModelLoader = new class extends ModelLoader {
	loadStrict(ctx: ModelContext, json: any):SmoothNode {
		return new SmoothNode(ctx,
			json['name'] as string,
			json['origin'] ? ctx.loadPoint(json['origin']) : null,
			ctx.loadPoint(json['pos']),
			ctx.loadFloat('prev', json['b'], 0.3),
			ctx.loadFloat('next', json['c'], 0.3),
			ctx.loadFloat('rotation', json['rot'], 0));
	}
}('Node', 'SmoothNode',NODE_SMOOTH_TYPE);
ModelContext.registerLoader(NODE_SMOOTH_LOADER);