import {TXY} from "../svg";
import {ModelPoint, ModelNode, EPointAttr, ENodeAttr, Model, ModelLoader, CModelPoint, CModelNode} from "./api";
import {CommonNode} from "./ncommon";
import svg = require("../svg");

export const NODE_SMOOTH_TYPE = 'smooth';
const DEFAULT_ABQ = 0.3;
const DEFAULT_ACQ = 0.3;
const DEFAULT_ROT = 0;
export class SmoothNode extends CommonNode<ModelPoint|ModelNode> {
	constructor(name: string|undefined,
				pos: ModelPoint,
				public readonly abq: number = DEFAULT_ABQ,
				public readonly acq: number = DEFAULT_ACQ,
				public readonly rot: number = DEFAULT_ROT) {
		super(NODE_SMOOTH_LOADER, name, pos, 'smooth_node');
	}

	protected updated(other: ModelPoint|ModelNode, attr: EPointAttr|ENodeAttr) {
		if (other instanceof CModelPoint) this.update("*");
		if (other instanceof CModelNode && (attr == "pos" || attr == "*")) {
			this.update("handle");
		}
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
		], this.abq, this.acq, this.rot)
	}

	public save(): any {
		return {
			type: NODE_SMOOTH_TYPE,
			name: this.name,
			pos: this.pos.save(),
			b: this.abq != DEFAULT_ABQ ? this.abq : undefined,
			c: this.acq != DEFAULT_ACQ ? this.acq : undefined,
			rot: this.rot != DEFAULT_ROT ? this.rot : undefined
		}
	}

}
function def<T>(x: T|undefined, d: T): T {
	return x === undefined ? d : x
}
export const NODE_SMOOTH_LOADER: ModelLoader = {
	cat: 'Node',
	name: 'SmoothNode',
	typename: NODE_SMOOTH_TYPE,
	loaderfn: (m: Model, json: any) =>
		new SmoothNode(json['name'],
			m.loadPoint(json['pos']),
			+def(json['b'], DEFAULT_ABQ), +def(json['c'], DEFAULT_ACQ),
			+def(json['rot'], DEFAULT_ROT))
};
Model.registerLoader(NODE_SMOOTH_LOADER);