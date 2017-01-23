import svg = require("../svg");
import {ModelLoader, CPart} from "./api";
import {ModelContext} from "./_ctx";
import {ValueFloat} from "./vfloat";

export type EParamAttr="*"|"meta"|"value";
export class ModelParam extends CPart<any,EParamAttr> {
	constructor(name: string,
				ctx: ModelContext,
				public readonly defVal: ValueFloat,
				public readonly minVal: ValueFloat,
				public readonly maxVal: ValueFloat) {
		super(name, ctx,[defVal, minVal, maxVal]);
	}

	protected updated<A2 extends string>(other: CPart<any, A2>, attr: A2) {
		this.update("meta");
	}

	public save(): any {
		return {
			name: this.name,
			defVal: this.defVal.save(),
			minVal: this.minVal.save(),
			maxVal: this.maxVal.save()
		}
	}
}
export const PARAM_LOADER: ModelLoader = {
	cat: 'Param',
	name: 'Param',
	objtypes: ['object'],
	loaderfn: (m: ModelContext, json: any, strict: boolean) =>
		new ModelParam(json['name'],m,
			m.loadFloat('default',json['defVal'],0.5),
			m.loadFloat('min',json['minVal'],0),
			m.loadFloat('max',json['maxVal'],1))
};
ModelContext.registerLoader(PARAM_LOADER);
