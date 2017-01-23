import {Model, ModelLoader, CModelElement, ModelNode, DisplayMode, ModelContext} from "./api";
import {SVGItem, updateElement} from "../dom";
import {NodePath} from "../svg";
import svg = require("../svg");

export type EPathAttr = "*"|"d";
export type ModelPath = CModelPath;
export class CModelPath extends CModelElement<Model,EPathAttr> {
	private g: SVGGElement|null;
	private p: SVGPathElement;

	constructor(name: string|undefined,
				ctx: ModelContext,
				public nodes: ModelNode[],
				public style: any,
				public closed: boolean = true) {
		super(name,ctx, nodes);
	}

	protected draw(mode:DisplayMode): SVGElement {
		this.p = SVGItem('path',{
			d: this.toSvgD(),
		});
		if (mode == 'edit') {
			let enodes = this.nodes.map(n => n.display());
			this.g = SVGItem('g', {
				'class': 'elem path',
				items: ([this.p] as (Element|null)[]).concat(enodes)
			});
			return this.g;
		} else {
			return this.p;
		}
	}

	public redraw(attr: EPathAttr,mode:DisplayMode) {
		this.p.setAttribute('d', this.toSvgD());
		updateElement(this.p,{
			d: this.toSvgD(),
			style: mode=='edit'?{}:this.style
		});
	}


	protected updated(other: ModelNode, attr: string) {
		if (attr == 'pos' || attr == 'handle' || attr == '*') this.update("*");
	}

	public toNodePath(): NodePath {
		return {
			z: this.closed,
			nodes: this.nodes.map(n => n.toDNode())
		}
	}

	public toSvgD(): string {
		return svg.dtostr(svg.nodestoels(this.toNodePath()));
	}

	public save(): any {
		return {
			name: this.name,
			closed: this.closed,
			style: this.style||{},
			nodes: this.nodes.map(n => n.save())
		};
	}

}
export const PATH_LOADER: ModelLoader = {
	cat: 'Path',
	name: 'Path',
	objtypes: ['object'],
	loaderfn: (m: ModelContext, json: any, strict: boolean) => new CModelPath(
		json['name'] as string,m,
		json['nodes'].map(j => m.loadNode(j)),
		json['style']||{},
		!!json['closed'])
};
ModelContext.registerLoader(PATH_LOADER);

