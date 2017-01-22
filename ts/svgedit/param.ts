import svg = require("../svg");
import {Model, ModelLoader, ModelPart} from "./api";

export class ModelParam extends ModelPart {
	constructor(name: string,
				public defVal: number = 0.5,
				public minVal: number = 0,
				public maxVal: number = 1) {
		super(PARAM_LOADER, name);
	}

	public save(): any {
		return {
			name: this.name, defVal: this.defVal, minVal: this.minVal, maxVal: this.maxVal
		}
	}
}
export const PARAM_LOADER: ModelLoader = {
	cat: 'Param',
	name: 'Param',
	objtypes: ['object'],
	loaderfn: (m: Model, json: any, strict: boolean) => new ModelParam(json['name'], json['defVal'], json['minVal'], json['maxVal'])
};
Model.registerLoader(PARAM_LOADER);
