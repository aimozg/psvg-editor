import {ModelPoint, EPointAttr, ModelLoader, Model, CModelPoint, DisplayMode} from "./api";
import {TXY} from "../svg";

export const POINT_REF_TYPE = '@';
export class PointRef extends CModelPoint<ModelPoint> {
	constructor(name: string|undefined, public readonly ref: string) {
		super(POINT_REF_LOADER,name, 'ref_pt');
	}

	protected draw(mode:DisplayMode): SVGGElement {
		return this.g;
	}

	protected redraw(attr:EPointAttr,mode:DisplayMode): any {
	}

	protected fcalculate(): TXY {
		return this.obj().calculate();
	}

	public obj(): ModelPoint {
		return this.ctx.model.findPoint(this.ref)!!;
	}

	protected attachChildren() {
		this.dependOn(() => this.obj(), 'pos');
	}

	protected updated(other: ModelPoint, attr: EPointAttr) {
		if (attr == 'pos' || attr == '*') this.update('pos');
	}

	public save(): any {
		if (this.name === undefined) return '@' + this.ref;
		return {
			name: this.name,
			type: '@' + this.ref
		}
	}
}
export const POINT_REF_LOADER:ModelLoader = {
	cat:'Point',
	name:'PointRef',
	typename:POINT_REF_TYPE,
	objtypes:['string','object'],
	loaderfn:(m:Model,json:any,strict:boolean)=>{
		if (!strict) {
			if (typeof json == 'string') return new PointRef(undefined, json.substr(1));
			if (json['type'][0] == '@') return new PointRef(json['name'], json['type'].substr(1));
			return null;
		}
		return new PointRef(json['name'],json['ref']);
	}
};
Model.registerLoader(POINT_REF_LOADER);