import {ModelPoint, ModelElement, ModelLoader, ModelNode, DisplayMode, ItemDeclaration} from "./api";
import {TXY} from "../svg";
import {norm2fixed} from "./ptnorm";
import {CommonNode} from "./ncommon";
import {ValueFloat} from "./vfloat";
import {ModelContext} from "./_ctx";
import svg = require("../svg");

export const NODE_FLOW1_TYPE = "flow1";
export class Flow1Node extends CommonNode {
	constructor(ctx:ModelContext,
				name:string|undefined,
				ownOrigin: ModelPoint|null,
				pos: ModelPoint,
				private h1ab: [ValueFloat, ValueFloat]|undefined,
				private h2ab: [ValueFloat, ValueFloat]|undefined) {
		super(ctx, name, ownOrigin, pos, 'flow1_node',
			(h1ab
				? [h1ab[0], h1ab[1], [() => this.prevNode(), 'pos'] as ItemDeclaration]
				: []).concat(
				h2ab
					? [h2ab[0], h2ab[1], [() => this.nextNode(), 'pos'] as ItemDeclaration]
					: [])
		);
	}


	protected updated(other: ModelElement, attr: string) {
		if (other instanceof ModelPoint) this.update("*");
		if (other instanceof ModelNode && (attr == "pos" || attr == "*") || (other instanceof ValueFloat)) {
			this.update("handle");
		}
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
export const NODE_FLOW1_LOADER: ModelLoader = new class extends ModelLoader {
	loadStrict(ctx: ModelContext, json: any) {
		return new Flow1Node(ctx,
			json['name'],
			json['origin']?ctx.loadPoint(json['origin']):json['origin'],
			ctx.loadPoint(json['pos']),
			json['h1ab'] ? [
					ctx.loadFloat('prev_tangent', json['h1ab'][0]),
					ctx.loadFloat('prev_normal', json['h1ab'][1])] : undefined,
			json['h2ab'] ? [
					ctx.loadFloat('next_tangent', json['h2ab'][0]),
					ctx.loadFloat('next_normal', json['h2ab'][1])] : undefined);
	}
}('Node','Flow1Node',NODE_FLOW1_TYPE);
ModelContext.registerLoader(NODE_FLOW1_LOADER);