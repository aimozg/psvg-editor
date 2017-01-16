import {CommonNode, ModelPoint, ModelElement, Model, ModelLoader, CModelPoint, CModelNode} from "./api";
import {TXY} from "../svg";
import {norm2fixed} from "./ptnorm";
import svg = require("../svg");

export const NODE_FLOW1_TYPE = "flow1";
export class FlowNode extends CommonNode<ModelPoint> {
	constructor(name: string|undefined,
				pos: ModelPoint,
				public h1ab: [number,number]|undefined,
				public h2ab: [number,number]|undefined) {
		super(NODE_FLOW1_LOADER,name, pos, 'flow1_node');
	}

	protected updated(other: ModelElement, attr: string) {
		if (other instanceof CModelPoint) this.update("*");
		if (other instanceof CModelNode && (attr == "pos" || attr=="*")) {
			this.update("handle");
		}
	}

	protected attachChildren(): any {
		super.attachChildren();
		if (this.h1ab) this.dependOn(this.prevNode(),"pos");
		if (this.h2ab) this.dependOn(this.nextNode(),"pos");
	}

	protected draw(): SVGElement {
		super.draw();
		// TODO draggable ctrl points
		return this.g;
	}

	protected calcHandles(): [TXY, TXY] {
		let pos = this.center();
		let ab1 = this.h1ab, ab2 = this.h2ab;
		let prev = this.prevNode(), next = this.nextNode();
		return [
			(prev&&ab1)?norm2fixed(prev.center(),pos,ab1[0],ab1[1]):[0,0],
			(next&&ab2)?norm2fixed(pos,next.center(),ab2[0],ab2[1]):[0,0]
		]
	}
	public save():any {
		return {
			type:NODE_FLOW1_TYPE,
			name:this.name,
			pos:this.pos.save(),
			h1ab:this.h1ab,
			h2ab:this.h2ab
		}
	}
}
export const NODE_FLOW1_LOADER:ModelLoader = {
	cat:'Node',
	name:'Flow1Node',
	typename:NODE_FLOW1_TYPE,
	loaderfn:(m:Model,json:any) => new FlowNode(json['name'],
	m.loadPoint(json['pos']),
	json['h1ab']?[+json['h1ab'][0],+json['h1ab'][1]]:undefined,
	json['h2ab']?[+json['h2ab'][0],+json['h2ab'][1]]:undefined)
};
Model.registerLoader(NODE_FLOW1_LOADER);