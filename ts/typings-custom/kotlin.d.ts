import {Model, DisplayMode} from "../svgedit/api";
import {ModelContext} from "../svgedit/_ctx";
import {CreateElementAttrs} from "../dom";
declare namespace kotlinjs {
	namespace com {
		namespace aimozg {
			namespace psvg {

				class ModelPane {

					public readonly model: Model;
					public readonly eModel: SVGElement;
					public readonly ctx: ModelContext;

					zoomfact: number;
					div: HTMLElement;

					constructor(model: Model,
								mode: DisplayMode,
								div: HTMLElement,
								defs?: (CreateElementAttrs|Element|undefined)[])
				}
			}
		}
	}
}

export = kotlinjs