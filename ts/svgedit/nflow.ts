import {CommonNode, ModelPoint, ModelElement, ModelNode, Model, ModelElementLoader} from "./api";
import {TXY} from "../svg";
import {norm2fixed} from "./ptnorm";
import svg = require("../svg");

export const NODE_FLOW1_TYPE = "flow1";
export class FlowNode extends CommonNode {
	constructor(name: string|undefined,
				pos: ModelPoint,
				public h1ab: [number,number]|undefined,
				public h2ab: [number,number]|undefined) {
		super(name, pos, 'flow1_node');
	}

	protected updated<A2 extends string>(other: ModelElement<any, any, A2>, attr: A2) {
		if (other instanceof ModelPoint) this.update("*");
		if (other instanceof ModelNode && (attr == "pos" || attr=="*")) {
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

	points(): ModelPoint[] {
		return [this.pos];
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
	public repr(): string {
		let ab1 = this.h1ab, ab2 = this.h2ab;
		return 'flow'+
			(ab1?' ['+ab1[0].toFixed(3)+','+ab1[1].toFixed(3)+']':'')+
			(ab2?' ['+ab2[0].toFixed(3)+','+ab2[1].toFixed(3)+']':'');
	}
}
export const NODE_FLOW1_LOADER:ModelElementLoader<FlowNode> = {
	cat:'Node',
	typename:NODE_FLOW1_TYPE,
	loaderfn:(m:Model,json:any) => new FlowNode(json['name'],
	m.loadPoint(json['pos']),
	json['h1ab']?[+json['h1ab'][0],+json['h1ab'][1]]:undefined,
	json['h2ab']?[+json['h2ab'][0],+json['h2ab'][1]]:undefined)
};
Model.registerLoader(NODE_FLOW1_LOADER);