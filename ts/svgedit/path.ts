import {ModelLoader, ModelElement, ModelNode, DisplayMode, EPartCategory} from "./api";
import {ModelContext} from "./_ctx";
import {SVGItem, updateElement} from "../dom";
import {NodePath} from "../svg";
import svg = require("../svg");

export type EPathAttr = "*"|"d";
export class ModelPath extends ModelElement {
	private g: SVGGElement|null;
	private p: SVGPathElement;


	public get category(): EPartCategory {
		return "Path";
	}

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
export const PATH_LOADER: ModelLoader = new class extends ModelLoader{
	loadStrict(ctx: ModelContext, json: any): ModelPath {
		return new ModelPath(
			json['name'] as string,ctx,
			json['nodes'].map(j => ctx.loadNode(j)),
			json['style']||{},
			!!json['closed'])
	}


}('Path','Path',null,['object']);
ModelContext.registerLoader(PATH_LOADER);

