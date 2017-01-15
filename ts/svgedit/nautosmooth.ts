import {TXY} from "../svg";
import {CommonNode, ModelPoint, ModelNode, EPointAttr, ENodeAttr, Model, ModelElementLoader} from "./api";
import svg = require("../svg");

export const NODE_AUTOSMOOTH_TYPE = 'autosmooth';
export class AutosmoothNode extends CommonNode {
	constructor(name: string|undefined, pos: ModelPoint) {
		super(name, pos, 'autosmooth_node');
	}

	protected updated(other: ModelPoint|ModelNode, attr: EPointAttr|ENodeAttr) {
		if (other instanceof ModelPoint) this.update("*");
		if (other instanceof ModelNode && (attr == "pos" || attr == "*")) {
			this.update("handle");
		}
	}

	protected attachChildren() {
		super.attachChildren();
		this.dependOn(this.prevNode(),"pos");
		this.dependOn(this.nextNode(),"pos");
	}

	points(): ModelPoint[] {
		return [this.pos];
	}

	protected calcHandles(): [TXY, TXY] {
		return svg.smoothhandles([
			this.prevNode().center(),
			this.center(),
			this.nextNode().center()
		])
	}

	public save(): any {
		return {
			type: NODE_AUTOSMOOTH_TYPE,
			name: this.name,
			pos: this.pos.save()
		}
	}

	public repr(): string {
		return 'auto-smooth';
	}

}
export const NODE_AUTOSMOOTH_LOADER:ModelElementLoader<AutosmoothNode> = {
	cat:'Node',
	typename:NODE_AUTOSMOOTH_TYPE,
	loaderfn:(m: Model, json: any) =>
		new AutosmoothNode(json['name'], m.loadPoint(json['pos']))
};
Model.registerLoader(NODE_AUTOSMOOTH_LOADER);