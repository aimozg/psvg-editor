import dom = require('./dom');
import svg = require("./svg");
require("jstree-css");
require("style.scss");

import kotlinjs = require("kotlinjs");
import Editor = kotlinjs.com.aimozg.psvg.editor.Editor;
import ALL_LOADERS = kotlinjs.com.aimozg.psvg.model.ALL_LOADERS;

//noinspection JSUnusedGlobalSymbols
export const importz = {dom, svg, ALL_LOADERS, kotlinjs};

//noinspection JSUnusedGlobalSymbols
export function setup(editorDiv: HTMLElement,
					  sidebar: HTMLElement,
					  previewDivs: HTMLElement[]): Editor {
	return new Editor(editorDiv, sidebar, previewDivs);
}

