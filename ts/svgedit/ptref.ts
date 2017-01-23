import {ModelPoint, EPointAttr, ModelLoader, CModelPoint, DisplayMode, ModelContext} from "./api";
import {TXY} from "../svg";

export const POINT_REF_TYPE = '@';
export class PointRef extends CModelPoint {
	constructor(name: string|undefined, ctx:ModelContext, public readonly ref: string) {
		super(name,ctx, 'ref_pt',[
			[() => this.obj(), 'pos']
		]);
	}

	protected draw(mode:DisplayMode): SVGGElement|null {
		return super.draw(mode);
	}

	protected redraw(attr:EPointAttr,mode:DisplayMode): any {
	}

	protected fcalculate(): TXY {
		return this.obj().calculate();
	}

	public obj(): ModelPoint {
		return this.ctx.findPoint(this.ref)!!;
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
	loaderfn:(m:ModelContext, json:any, strict:boolean)=>{
		if (!strict) {
			if (typeof json == 'string') return new PointRef(undefined,m, json.substr(1));
			if (json['type'][0] == '@') return new PointRef(json['name'],m, json['type'].substr(1));
			return null;
		}
		return new PointRef(json['name'],m,json['ref']);
	}
};
ModelContext.registerLoader(POINT_REF_LOADER);