export function escape(s: string): string {
	return ('' + ((s === undefined || s === null) ? '' : s)).replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
export function byId(id: string): HTMLElement|undefined {
	return document.getElementById(id)
}

export function querySelector(el: Document|HTMLElement|undefined, selector: string): HTMLElement[] {
	return el ? nodesToElements(el.querySelectorAll(selector)) : []
}

export function children(el: HTMLElement|undefined): HTMLElement[] {
	return el ? nodesToElements(el.childNodes) : []
}

export function show(el: HTMLElement|HTMLElement[]|undefined) {
	if (el instanceof HTMLElement) el.style.display = "";
	else if (el) for (let e of el) e.style.display = "";
}

export function hide(el: HTMLElement|HTMLElement[]|null) {
	if (el instanceof HTMLElement) el.style.display = "none";
	else if (el) for (let e of el) e.style.display = "none";
}

export function clone(el: HTMLElement, ext?: (old: HTMLElement, copy: HTMLElement) => void) {
	let e2 = document.createElement(el.tagName);
	for (let an of attrsToList(el.attributes)) {
		if (an.name != 'id') e2.setAttribute(an.name, an.value);
	}
	if (ext) ext(el, e2);
	traverseAll(el, node => {
		switch (node.nodeType) {
			case Node.TEXT_NODE:
				e2.appendChild(document.createTextNode(node.textContent || ""));
				return false;
			case Node.ELEMENT_NODE:
				e2.appendChild(clone(node as HTMLElement, ext));
				return false;
		}
		return false;
	});
	return e2;
}

export function traverse(root: Element, deep: (el: Element) => boolean) {
	let e = root.firstElementChild;
	while (e) {
		let e0 = e;
		let p = e0.parentElement;
		if (deep(e0)) {
			if (e = e0.firstElementChild) continue;
		}
		if (e = e0.nextElementSibling) continue;
		if (p && p != root) {
			do {
				e = p.nextElementSibling;
				p = p.parentElement;
			} while (p && !e && p != root);
		}
	}
}

export function traverseAll(root: Node, deep: (n: Node) => boolean) {
	let e: Node|null = root.firstChild;
	while (e) {
		let e0 = e;
		let p = e0.parentNode;
		let s = e0.nextSibling;
		if (deep(e0)) {
			if (e = e0.firstChild) continue;
		}
		if (e = e0.nextSibling) continue;
		if (e = s) continue;
		if (p && p != root) {
			do {
				e = p.nextSibling;
				p = p.parentNode;
			} while (p && !e && p != root);
		}
	}
}
export function clear<T extends Node>(e:T,nodeType?:number):T {
	let n = e.firstChild;
	while(n){
		let n2 = n.nextSibling;
		if (nodeType === undefined || nodeType == n.nodeType) e.removeChild(n);
		n=n2;
	}
	return e;
}

export function neww(tag: string, clazz?: string): HTMLElement {
	let e = document.createElement(tag);
	if (clazz) e.className = clazz;
	return e;
}

export function nodesToElements(nl: NodeList): HTMLElement[] {
	let result: HTMLElement[] = [];
	for (let i = 0; i < nl.length; i++) {
		let e = nl.item(i);
		if (e instanceof HTMLElement) result.push(e);
	}
	return result;
}

export function nodesToList(nl: NodeList): Node[] {
	let result: Node[] = [];
	for (let i = 0; i < nl.length; i++) result.push(nl.item(i));
	return result;
}
export function tokensToList(nl: DOMTokenList): string[] {
	let result: string[] = [];
	for (let i = 0; i < nl.length; i++) result.push(nl.item(i));
	return result;
}

export function attrsToList(nl: NamedNodeMap): Attr[] {
	let result: Attr[] = [];
	for (let i = 0; i < nl.length; i++) result.push(nl.item(i));
	return result;
}
/*export function findWithDotDot(j:JQuery, selector:string):JQuery {
 if (selector.indexOf("..") == 0) {
 for (let si of selector.split("/")) {
 if (si == "..") j = j.parent();
 else return j.find(si);
 }
 return j;
 } else return $(selector);
 }*/
export interface CreateElementAttrsLite {
	tag?: string;
	parent?: HTMLElement;
	items?: (CreateElementAttrs|undefined)[];
	text?: string;
	style?: CEAStyle;
	callback?: (el:Element,attrs:CreateElementAttrs)=>any;
	[index: string]: any;
}
export interface CreateElementAttrsFull extends CreateElementAttrsLite {
	tag: string;
}
export interface CEASvgPath extends CreateElementAttrsFull {
	tag: 'path';
	d: string;
	callback?: (el:SVGPathElement,attrs:CEASvgPath)=>any;
}
export interface CEASvgG extends CreateElementAttrsFull {
	tag: 'g';
	callback?: (el:SVGGElement,attrs:CEASvgG)=>any;
}
export interface CEASvgUse extends CreateElementAttrsFull {
	tag: 'use';
	href: string;
	x?: number;
	y?: number;
	callback?: (el:SVGUseElement,attrs:CEASvgUse)=>any;
}
export type CreateElementAttrs = CreateElementAttrsFull | CreateElementAttrsLite;
export interface CEAStyle {
	[index: string]: any;
}
export function merge1d<T>(dst: T, ...src: (any|undefined)[]): T {
	for (let s of src) if (s) for (let k of Object.keys(s)) dst[k] = s[k];
	return dst;
}
interface CEAFns<T extends Element> {
	dce: (tag: string, params: CreateElementAttrs) => T;
	specialAttrs?: string[];
	pre?: (i: T, attrs: CreateElementAttrs) => void;
	prea?: (child: T, parent: T) => void;
}
function CEAApply<T extends Element>(i:T,fns:CEAFns<T>,attrs:CreateElementAttrs):T{
	const tag = attrs.tag;
	const parent = attrs.parent;
	const items = attrs.items;
	const text = attrs.text;
	const callback = attrs.callback;
	if (fns.pre) fns.pre(i, attrs);
	for (let a in attrs) {
		if (['tag', 'parent', 'items', 'text','callback'].indexOf(a) == -1 &&
			(fns.specialAttrs || []).indexOf(a) == -1) {
			if (['checked', 'disabled'].indexOf(a) == -1) i.setAttribute(a, attrs[a]);
			else i[a] = attrs[a];
		}
	}
	if (text) i.textContent = text;
	if (callback) callback(i,attrs);
	if (items) {
		clear(i);
		for (let arg of items) {
			if (arg === undefined || arg === null) continue;
			const item = (arg instanceof Element) ? arg as T : CreateElement(fns, arg);
			if (fns.prea) fns.prea(item, i);
			i.appendChild(item);
		}
	}
	if (parent) parent.appendChild(i);
	return i;
}
function CreateElement<T extends Element>(fns:CEAFns<T>, attrs: CreateElementAttrs): T {
	return CEAApply(fns.dce(attrs.tag,attrs),fns,attrs);
}
const CeaFnHtml: CEAFns<HTMLElement> = {
	dce: tag =>document.createElement(tag),
	specialAttrs: ['style'],
	pre: (ele:HTMLElement, attrs:CreateElementAttrs) =>{
		const style = attrs.style;
		if (style) for (let s in style) {
			ele.style[s] = style[s];
		}
	}
};
export function createElement(attrs: CreateElementAttrsFull): HTMLElement {
	return CreateElement(CeaFnHtml,attrs);
}
const CeaFnSvg: CEAFns<SVGElement> = {
	dce: tag => document.createElementNS("http://www.w3.org/2000/svg", tag) as SVGElement,
	specialAttrs:['style'],
	pre:(ele:SVGElement, attrs:CreateElementAttrs) =>{
		const style = attrs.style;
		if (style) for (let s in style) {
			ele.setAttribute(s, style[s]);
		}
	}
};
export function updateElement<T extends HTMLElement>(e:T,attrs:CreateElementAttrs):T;
export function updateElement<T extends SVGElement>(e:T,attrs:CreateElementAttrs):T;
export function updateElement(e:Element,attrs:CreateElementAttrs):Element {
	let fns: CEAFns<HTMLElement>|CEAFns<SVGElement>;
	if (e instanceof HTMLElement) fns = CeaFnHtml;
	else if (e instanceof SVGElement) fns = CeaFnSvg;
	else throw e;
	CEAApply(e,fns,attrs);
	return e;
}

export function SVGItem(attrs: CreateElementAttrsFull): SVGElement;
export function SVGItem(tag: string, attrs?: CreateElementAttrs): SVGElement;
export function SVGItem(tag: 'g', attrs?: CreateElementAttrs): SVGGElement;
export function SVGItem(tag: 'line', attrs?: CreateElementAttrs): SVGLineElement;
export function SVGItem(tag: 'path', attrs?: CreateElementAttrs): SVGPathElement;
export function SVGItem(tag: 'svg', attrs?: CreateElementAttrs): SVGSVGElement;
export function SVGItem(tag: 'use', attrs?: CreateElementAttrs): SVGUseElement;
export function SVGItem(arg1: any, arg2?: CreateElementAttrs): SVGElement {
	/*if (typeof arg1 == 'string') {
	 return
	 }*/
	return CreateElement(CeaFnSvg,(typeof arg1 == 'string') ?
		merge1d({tag: arg1}, arg2) : arg1);
}
export interface CreateSVGAttrs {
	items: (CreateElementAttrs|undefined)[];
	width: number;
	height: number;
}

export function SVG(items: CreateSVGAttrs,
					viewBox: [number, number, number, number] = [0, 0, items.width, items.height]): SVGSVGElement {
	return SVGItem('svg', {
		version: 1.1,
		"xmlns:xlink": "http://www.w3.org/1999/xlink",
		viewBox: viewBox.join(' '),
		width: items.width + 'px',
		height: items.height + 'px',
		items: items.items
	});
}
