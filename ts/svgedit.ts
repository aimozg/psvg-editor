import dom = require('./dom');
import svg = require("./svg");
require("jstree-css");

import {ALL_LOADERS} from "./svgedit/_all";
import kotlinjs = require("kotlinjs");
import ModelPane = kotlinjs.com.aimozg.psvg.ModelPane;
import Editor = kotlinjs.com.aimozg.psvg.Editor;

//noinspection JSUnusedGlobalSymbols
export const importz = {dom, svg, ALL_LOADERS};



//noinspection JSUnusedGlobalSymbols
export function setup(editorDiv: HTMLElement,
					  treeDiv: HTMLElement,
					  previewDivs: HTMLElement[],
					  objviewDiv: HTMLElement): Editor {
	return new Editor(editorDiv, treeDiv, previewDivs, objviewDiv);
}

