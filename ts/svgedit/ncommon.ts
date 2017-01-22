import svg = require("../svg");
import dom = require("../dom");
import {TXY, DNode, IXY} from "../svg";
import {CModelNode, ModelPoint, DisplayMode, ENodeAttr, Value, ItemDeclaration, Part, ModelContext} from "./api";
import {SVGItem} from "../dom";

export abstract class CommonNode<CHILD extends Part> extends CModelNode<any> {
	protected l1: SVGLineElement|null;
	protected l2: SVGLineElement|null;
	protected u0: SVGUseElement|null;
	protected g: SVGGElement|null;

	constructor(name: string|undefined,
				ctx: ModelContext,
				public pos: ModelPoint,
				private gclass: string,
				items: ItemDeclaration<CHILD>[],
				values: Value<any>[]) {
		super(name, ctx, items,values);
	}

	protected attachChildren() {
		super.attachChildren();
		this.attach(this.pos, "pos");
	}

	protected draw(mode:DisplayMode): SVGElement|null {
		this.l1 = null;
		this.l2 = null;
		if (mode == 'edit') {
			this.g = dom.SVGItem('g', {
				'class': 'node '+(this.gclass||''),
				items: [
					this.u0 = SVGItem('use', {href: this.uhref()}),
					this.pos.display("pt_node")
				]
			});
		}
		return this.g;
	}

	public center(): IXY {
		return this.pos.calculate();
	}

	public toDNode(): DNode {
		let h12xy = this.calcHandles();
		return {
			p: this.pos.calculate(),
			h1: h12xy[0],
			h2: h12xy[1]
		}
	}

	protected redraw(attr: ENodeAttr,mode:DisplayMode) {
		let pxy = this.pos.calculate();
		if (mode == 'edit' && this.g && this.u0) {
			svg.tf2list(svg.tftranslate(pxy[0], pxy[1]), this.u0.transform.baseVal);
			let h12xy = this.calcHandles();
			if (this.l1) this.g.removeChild(this.l1);
			if (this.l2) this.g.removeChild(this.l2);
			this.l1 = this.l2 = null;
			if (!this.first) {
				this.l1 = dom.SVGItem('line', {
					x1: pxy[0], x2: h12xy[0][0],
					y1: pxy[1], y2: h12xy[0][1], 'class': 'handle1'
				});
				this.g.insertBefore(this.l1, this.g.firstChild);
			}
			if (!this.last) {
				this.l2 = dom.SVGItem('line', {
					x1: pxy[0], x2: h12xy[1][0],
					y1: pxy[1], y2: h12xy[1][1], 'class': 'handle2'
				});
				this.g.insertBefore(this.l2, this.g.firstChild);
			}
		}
	}


	protected abstract calcHandles(): [TXY, TXY];
}
