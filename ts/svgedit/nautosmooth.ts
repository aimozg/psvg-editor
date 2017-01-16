import {TXY} from "../svg";
import {
	CommonNode,
	ModelPoint,
	ModelNode,
	EPointAttr,
	ENodeAttr,
	Model,
	ModelLoader,
	CModelPoint,
	CModelNode
} from "./api";
import svg = require("../svg");

export const NODE_AUTOSMOOTH_TYPE = 'autosmooth';
export class AutosmoothNode extends CommonNode<ModelPoint|ModelNode> {
	constructor(name: string|undefined, pos: ModelPoint) {
		super(NODE_AUTOSMOOTH_LOADER,name, pos, 'autosmooth_node');
	}

	protected updated(other: ModelPoint|ModelNode, attr: EPointAttr|ENodeAttr) {
		if (other instanceof CModelPoint) this.update("*");
		if (other instanceof CModelNode && (attr == "pos" || attr == "*")) {
			this.update("handle");
		}
	}

	protected attachChildren() {
		super.attachChildren();
		this.dependOn(this.prevNode(),"pos");
		this.dependOn(this.nextNode(),"pos");
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
export const NODE_AUTOSMOOTH_LOADER:ModelLoader = {
	cat:'Node',
	name:'AutosmoothNode',
	typename:NODE_AUTOSMOOTH_TYPE,
	loaderfn:(m: Model, json: any) =>
		new AutosmoothNode(json['name'], m.loadPoint(json['pos']))
};
Model.registerLoader(NODE_AUTOSMOOTH_LOADER);