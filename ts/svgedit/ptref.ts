import {ModelPoint, EPointAttr, ModelLoader, DisplayMode} from "./api";
import {ModelContext} from "./_ctx";
import {TXY} from "../svg";

export const POINT_REF_TYPE = '@';
export class PointRef extends ModelPoint {
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
export const POINT_REF_LOADER:ModelLoader = new class extends ModelLoader {

	loadRelaxed(ctx: ModelContext, json: any, ...args): PointRef|any {
		if (typeof json == 'string') return new PointRef(undefined, ctx, json.substr(1));
		if (json['type'][0] == '@') return new PointRef(json['name'], ctx, json['type'].substr(1));
		return null;
	}

	loadStrict(m: ModelContext, json: any):PointRef {
		return new PointRef(json['name'], m, json['ref']);
	}
}('Point','PointRef',POINT_REF_TYPE,['string','object']);
ModelContext.registerLoader(POINT_REF_LOADER);