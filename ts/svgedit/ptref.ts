import {ModelPoint, EPointAttr, ModelElement, ModelElementLoader, Model} from "./api";
import {TXY} from "../svg";

export const POINT_REF_TYPE = '@';
export class ModelPointRef extends ModelPoint {
	constructor(name: string|undefined, public readonly ref: string) {
		super(name, 'ref_pt');
	}

	protected draw(): SVGGElement {
		return this.g;
	}

	protected redraw(attr:EPointAttr): any {
	}

	protected fcalculate(): TXY {
		return this.ctx.model.findPoint(this.ref).calculate();
	}

	protected attachChildren() {
		this.dependOn(() => this.ctx.model.findPoint(this.ref), 'pos');
	}

	protected updated(other: ModelElement<any, any, EPointAttr>, attr: EPointAttr) {
		if (attr == 'pos' || attr == '*') this.update('pos');
	}

	public save(): any {
		if (this.name === undefined) return '@' + this.ref;
		return {
			name: this.name,
			type: '@' + this.ref
		}
	}

	public repr(): string {
		return '@' + this.ref;
	}
}
export const POINT_REF_LOADER:ModelElementLoader<ModelPointRef> = {
	cat:'Point',
	typename:POINT_REF_TYPE,
	objtypes:['string','object'],
	loaderfn:(m:Model,json:any,strict:boolean)=>{
		if (!strict) {
			if (typeof json == 'string') return new ModelPointRef(undefined, json.substr(1));
			if (json['type'][0] == '@') return new ModelPointRef(json['name'], json['type'].substr(1));
			return undefined;
		}
		return new ModelPointRef(json['name'],json['ref']);
	}
};
Model.registerLoader(POINT_REF_LOADER);