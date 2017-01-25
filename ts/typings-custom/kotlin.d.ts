import {Model, DisplayMode, Part} from "../svgedit/api";
import {ModelContext} from "../svgedit/_ctx";
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
								defs?: (Element|undefined)[])
				}

				class Editor {
					readonly model: Model;
					selection: Part|null;

					constructor(canvasDiv: HTMLElement,
								treeDiv: HTMLElement,
								previewDivs: HTMLElement[],
								objviewDiv: HTMLElement);

					select(part: Part|null);
					save(): any;
					loadJson(json: any);
				}

			}
		}
	}
}

export = kotlinjs