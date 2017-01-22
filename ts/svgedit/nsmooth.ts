import {TXY} from "../svg";
import {
	ModelPoint,
	ModelNode,
	EPointAttr,
	ENodeAttr,
	ModelLoader,
	CModelPoint,
	CModelNode,
	Value,
	ModelContext
} from "./api";
import {CommonNode} from "./ncommon";
import {ValueFloat} from "./vfloat";
import svg = require("../svg");

export const NODE_SMOOTH_TYPE = 'smooth';
const DEFAULT_ABQ = 0.3;
const DEFAULT_ACQ = 0.3;
const DEFAULT_ROT = 0;
export class SmoothNode extends CommonNode<ModelPoint|ModelNode> {
	constructor(name: string|undefined,
				ctx: ModelContext,
				pos: ModelPoint,
				public readonly abq: ValueFloat,
				public readonly acq: ValueFloat,
				public readonly rot: ValueFloat) {
		super(name,ctx, pos, 'smooth_node',[],[abq,acq,rot]);
	}

	protected updated(other: ModelPoint|ModelNode, attr: EPointAttr|ENodeAttr) {
		if (other instanceof CModelPoint) this.update("*");
		if (other instanceof CModelNode && (attr == "pos" || attr == "*")) {
			this.update("handle");
		}
	}


	public valueUpdated<T>(value: Value<T>) {
		this.update("handle");
	}

	protected attachChildren() {
		super.attachChildren();
		this.dependOn(this.prevNode(), "pos");
		this.dependOn(this.nextNode(), "pos");
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
			pos: this.pos.save(),
			b: this.abq.save(),
			c: this.acq.save(),
			rot: this.rot.save()
		}
	}

}
export const NODE_SMOOTH_LOADER: ModelLoader = {
	cat: 'Node',
	name: 'SmoothNode',
	typename: NODE_SMOOTH_TYPE,
	loaderfn: (m: ModelContext, json: any) =>
		new SmoothNode(json['name'],m,
			m.loadPoint(json['pos']),
			ValueFloat.load('prev',json['b'],DEFAULT_ABQ),
			ValueFloat.load('next',json['c'],DEFAULT_ACQ),
			ValueFloat.load('rotation',json['rot'],DEFAULT_ROT))
};
ModelContext.registerLoader(NODE_SMOOTH_LOADER);