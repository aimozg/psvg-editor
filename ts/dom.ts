export function escape(s: string): string {
	return ('' + ((s === undefined || s === null) ? '' : s)).replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
export function byId(id: string): HTMLElement|null {
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

export function clone(el: HTMLElement, ext?: (old: HTMLElement, copy: HTMLElement) => void):HTMLElement {
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
export interface CEAStyle {
	[index: string]: any;
}
export interface CreateElementAttrsLite {
	tag?: string;
	parent?: Element;
	items?: (CreateElementAttrs|Element|undefined|null)[];
	text?: string;
	style?: CEAStyle;
	callback?: (el:Element,attrs:CreateElementAttrsLite)=>any;
	oninput?: string|((e:MouseEvent)=>any);
	onchange?: string|((e:Event)=>any);
	[index: string]: any;
}
export interface CreateElementAttrs extends CreateElementAttrsLite {
	tag: string;
}
const CEAevents = ['oninput','onchange'];
const CEAspecials = ['tag', 'parent', 'items', 'text', 'callback'];
export interface CEASvgPath extends CreateElementAttrs {
	tag: 'path';
	d: string;
	callback?: (el:SVGPathElement,attrs:CEASvgPath)=>any;
}
export interface CEASvgG extends CreateElementAttrs {
	tag: 'g';
	callback?: (el:SVGGElement,attrs:CEASvgG)=>any;
}
export interface CEASvgUse extends CreateElementAttrs {
	tag: 'use';
	href: string;
	x?: number;
	y?: number;
	callback?: (el:SVGUseElement,attrs:CEASvgUse)=>any;
}
export function merge1d<T>(dst: T, ...src: (any|undefined)[]): T {
	for (let s of src) if (s) for (let k of Object.keys(s)) dst[k] = s[k];
	return dst;
}
interface CEAFns<T extends Element> {
	dce: (tag: string, params: CreateElementAttrsLite) => T;
	specialAttrs?: string[];
	pre?: (i: T, attrs: CreateElementAttrsLite) => void;
	prea?: (child: T, parent: T) => void;
}
function CEAApply<T extends Element>(i:T,fns:CEAFns<T>,attrs:CreateElementAttrsLite):T{
	if (fns.pre) fns.pre(i, attrs);
	for (let a in attrs) {
		let v = attrs[a];
		if (CEAevents.indexOf(a) != -1 && typeof v == 'function') {
			i.addEventListener(a.substr(2), v);
		} else if (CEAspecials.indexOf(a) == -1 &&
			(fns.specialAttrs || []).indexOf(a) == -1) {
			if (['checked', 'disabled'].indexOf(a) == -1) i.setAttribute(a, v);
			else i[a] = v;
		}
	}
	const text = attrs.text;
	if (text) i.textContent = text;
	const callback = attrs.callback;
	if (callback) callback(i,attrs);
	const items = attrs.items;
	if (items) {
		clear(i);
		for (let arg of items) {
			if (arg === undefined || arg === null) continue;
			const item = (arg instanceof Element) ? arg as T : CreateElement(fns, arg);
			if (fns.prea) fns.prea(item, i);
			i.appendChild(item);
		}
	}
	const parent = attrs.parent;
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
export function createElement(attrs: CreateElementAttrs): HTMLElement;
export function createElement(tag: string, attrs: CreateElementAttrsLite): HTMLElement;
export function createElement(tag: 'div', attrs?: CreateElementAttrsLite): HTMLDivElement;
export function createElement(): HTMLElement {
	return CreateElement(CeaFnHtml,(typeof arguments[0] == 'string') ?
		merge1d({tag: arguments[0]}, arguments[1]) : arguments[0]);
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
export function updateElement<T extends HTMLElement|SVGElement>(e:T,attrs:CreateElementAttrsLite):T;
export function updateElement(e:Element,attrs:CreateElementAttrsLite):Element {
	let fns: CEAFns<HTMLElement>|CEAFns<SVGElement>;
	if (e instanceof HTMLElement) fns = CeaFnHtml;
	else if (e instanceof SVGElement) fns = CeaFnSvg;
	else throw e;
	CEAApply(e,fns,attrs);
	return e;
}

export function SVGItem(attrs: CreateElementAttrs): SVGElement;
export function SVGItem(tag: string, attrs?: CreateElementAttrsLite): SVGElement;
export function SVGItem(tag: 'g', attrs?: CreateElementAttrsLite): SVGGElement;
export function SVGItem(tag: 'line', attrs?: CreateElementAttrsLite): SVGLineElement;
export function SVGItem(tag: 'path', attrs?: CreateElementAttrsLite): SVGPathElement;
export function SVGItem(tag: 'svg', attrs?: CreateElementAttrsLite): SVGSVGElement;
export function SVGItem(tag: 'use', attrs?: CreateElementAttrsLite): SVGUseElement;
export function SVGItem(arg1: CreateElementAttrs|string, arg2?: CreateElementAttrsLite): SVGElement {
	/*if (typeof arg1 == 'string') {
	 return
	 }*/
	return CreateElement(CeaFnSvg,(typeof arg1 == 'string') ?
		merge1d({tag: arg1}, arg2) : arg1);
}
export interface CreateSVGAttrs {
	items: (CreateElementAttrs|Element|undefined)[];
	width: number;
	height: number;
	'class'?: string;
}

export function SVG(attrs: CreateSVGAttrs,
					viewBox: [number, number, number, number] = [0, 0, attrs.width, attrs.height]): SVGSVGElement {
	return SVGItem('svg', {
		version: 1.1,
		"xmlns:xlink": "http://www.w3.org/1999/xlink",
		viewBox: viewBox.join(' '),
		width: attrs.width + 'px',
		height: attrs.height + 'px',
		items: attrs.items,
		'class': attrs.class
	});
}
