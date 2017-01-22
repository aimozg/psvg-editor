import {ModelPoint, ModelElement, Model, ModelLoader, CModelPoint, CModelNode, DisplayMode, Value} from "./api";
import {TXY} from "../svg";
import {norm2fixed} from "./ptnorm";
import {CommonNode} from "./ncommon";
import {ValueFloat} from "./vfloat";
import svg = require("../svg");

export const NODE_FLOW1_TYPE = "flow1";
export class FlowNode extends CommonNode<ModelPoint> {
	constructor(name: string|undefined,
				pos: ModelPoint,
				private h1ab: [ValueFloat, ValueFloat]|undefined,
				private h2ab: [ValueFloat, ValueFloat]|undefined) {
		super(NODE_FLOW1_LOADER, name, pos, 'flow1_node',
			(h1ab ? [h1ab[0], h1ab[1]] : []).concat(h2ab ? [h2ab[0], h2ab[1]]:[])
		);
	}


	public valueUpdated<T>(value: Value<T>) {
		this.update("handle");
	}

	protected updated(other: ModelElement, attr: string) {
		if (other instanceof CModelPoint) this.update("*");
		if (other instanceof CModelNode && (attr == "pos" || attr == "*")) {
			this.update("handle");
		}
	}

	protected attachChildren(): any {
		super.attachChildren();
		if (this.h1ab) this.dependOn(this.prevNode(), "pos");
		if (this.h2ab) this.dependOn(this.nextNode(), "pos");
	}

	protected draw(mode: DisplayMode): SVGElement|null {
		return super.draw(mode);
		// TODO draggable ctrl points
	}

	protected calcHandles(): [TXY, TXY] {
		let pos = this.center();
		let ab1 = this.h1ab, ab2 = this.h2ab;
		let prev = this.prevNode(), next = this.nextNode();
		return [
			(prev && ab1) ? norm2fixed(prev.center(), pos, ab1[0].get(), ab1[1].get()) : [0, 0],
			(next && ab2) ? norm2fixed(pos, next.center(), ab2[0].get(), ab2[1].get()) : [0, 0]
		]
	}

	public save(): any {
		return {
			type: NODE_FLOW1_TYPE,
			name: this.name,
			pos: this.pos.save(),
			h1ab: this.h1ab ? [this.h1ab[0].save(), this.h1ab[1].save()] : undefined,
			h2ab: this.h2ab ? [this.h2ab[0].save(), this.h2ab[1].save()] : undefined
		}
	}
}
export const NODE_FLOW1_LOADER: ModelLoader = {
	cat: 'Node',
	name: 'Flow1Node',
	typename: NODE_FLOW1_TYPE,
	loaderfn: (m: Model, json: any) => new FlowNode(json['name'],
		m.loadPoint(json['pos']),
		json['h1ab'] ? [
				ValueFloat.load('prev_tangent', json['h1ab'][0]),
				ValueFloat.load('prev_normal', json['h1ab'][1])] : undefined,
		json['h2ab'] ? [
				ValueFloat.load('next_tangent', json['h2ab'][0]),
				ValueFloat.load('next_normal', json['h2ab'][1])] : undefined)
};
Model.registerLoader(NODE_FLOW1_LOADER);