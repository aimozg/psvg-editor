import svg = require("../svg");
import {ModelLoader, Part, EPartCategory} from "./api";
import {ModelContext} from "./_ctx";
import {ValueFloat} from "./vfloat";

export class ModelParam extends Part {
	public get category(): EPartCategory {
		return "Param";
	}

	constructor(name: string,
				ctx: ModelContext,
				public readonly defVal: ValueFloat,
				public readonly minVal: ValueFloat,
				public readonly maxVal: ValueFloat) {
		super(name, ctx,[defVal, minVal, maxVal]);
	}

	protected updated(other: Part, attr: string) {
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
export const PARAM_LOADER: ModelLoader = new class extends ModelLoader{
	loadStrict(m: ModelContext, json: any): ModelParam {
		return new ModelParam(json['name'],m,
			m.loadFloat('default',json['defVal'],0.5),
			m.loadFloat('min',json['minVal'],0),
			m.loadFloat('max',json['maxVal'],1));
	}

}('Param','Param',null,['object']);
ModelContext.registerLoader(PARAM_LOADER);
