import svg = require("../svg");
import {Model, ModelLoader, Value, CPart} from "./api";
import {ValueFloat} from "./vfloat";

export type EParamAttr="*"|"meta"|"value";
export class ModelParam extends CPart<EParamAttr> {
	constructor(name: string,
				public readonly defVal: ValueFloat,
				public readonly minVal: ValueFloat,
				public readonly maxVal: ValueFloat) {
		super(name, [defVal, minVal, maxVal]);
	}

	public valueUpdated<T>(value: Value<T>) {
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
	loaderfn: (m: Model, json: any, strict: boolean) =>
		new ModelParam(json['name'],
			ValueFloat.load('default',json['defVal']||0.5),
			ValueFloat.load('min',json['minVal']||0),
			ValueFloat.load('max',json['maxVal']||1))
};
Model.registerLoader(PARAM_LOADER);
