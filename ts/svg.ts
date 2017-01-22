import {CEASvgUse, merge1d, CreateElementAttrsLite} from "./dom";
export const SVGNS = 'http://www.w3.org/2000/svg';
export type TXY = [number, number];
export type IXY = TXY|number[];
export interface PathDEl {
	c: string;
	p: TXY[];
}
export interface IPathDEl {
	c: string;
	p: IXY[];
}
export function dparse(str: string): PathDEl[] {
	const parts = str.split(' ').map(s => s.split(',').map(x => isFinite(+x) ? +x : x));
	const els: PathDEl[] = [];
	let e = '';
	for (let i = 0; i < parts.length;) {
		if (typeof parts[i][0] == 'string') e = parts[i++][0] as string;
		const el: PathDEl = {c: e, p: []};
		let n = {'m': 1, 'M': 1, 'c': 3, 'C': 3, 's': 2, 'S': 2, 'z': 0}[e];
		while (n-- > 0) el.p.push(parts[i++] as TXY);
		els.push(el);
	}
	return els;
}
export function pcpy(p: IXY[]): TXY[] {
	return p.map(x => [x[0], x[1]] as TXY);
}
export function dcpyel(el: IPathDEl): PathDEl {
	return {c: el.c, p: pcpy(el.p)};
}
export function dcpy(els: IPathDEl[]): PathDEl[] {
	return els.map(dcpyel);
}
export function drepr(els: IPathDEl[]): string {
	return els.map(s => '[' + [s.c].concat(s.p.map(u => u.join(','))).join(' ') + ']').join(' ');
}
export function dtostr(els: IPathDEl[]): string {
	return els.map(s => [s.c].concat(s.p.map(u => u.join(','))).join(' ')).join(' ');
}
export function dtoabs(els: IPathDEl[]): PathDEl[] {
	let xy = [0, 0];
	const els2: PathDEl[] = [];
	for (let el of els) {
		const n = el.p.length;
		const c = el.c.toUpperCase();
		const p2: PathDEl = {c, p: []};
		els2.push(p2);
		if (n == 1) continue;
		if (c == el.c) xy = [0, 0];
		for (let i = 1; i < n; i++) p2.p[i] = vadd(el.p[i], xy);
		xy = [p2.p[n - 1][0], p2.p[n - 1][1]];
	}
	return els2;
}
export function vscale(s: number, v: IXY): TXY {
	return [s * v[0], s * v[1]];
}
export function vadd(a: IXY, b: IXY): TXY {
	return [a[0] + b[0], a[1] + b[1]];
}
export function vsub(a: IXY, b: IXY): TXY {
	return [a[0] - b[0], a[1] - b[1]];
}
export function vrot90(a: IXY): TXY {
	return [a[1], -a[0]];
}
export function vnorm(v: IXY): number {
	return v[0] ** 2 + v[1] ** 2;
}
export function vlen(v: IXY): number {
	return Math.sqrt(vnorm(v));
}
export function vunit(v: IXY): TXY {
	return (v[0] == 0 && v[1] == 0) ? [0, 0] : vscale(1.0 / vlen(v), v);
}
export function vdot(a: IXY, b: IXY): number {
	return a[0]*b[0]+a[1]*b[1];
}
export function vsetlen(s: number, v: IXY): TXY {
	return vscale(s, vunit(v));
}
export function vlint(p: number, a: IXY, b: IXY): TXY {
	return vadd(a, vscale(p, vsub(b, a)));
}
export function vlinj(...vks: [number, IXY][]): TXY {
	return vks.map(vk => [
		vk[0] * vk[1][0],
		vk[0] * vk[1][1]
	] as TXY).reduce((v1, v2) => [
		v1[0] + v2[0],
		v1[1] + v2[1]
	]);
}
export function vlintlen(s: number, a: IXY, b: IXY): TXY {
	return vadd(a, vsetlen(s, vsub(b, a)));
}
export function vbisect(ab: IXY, ac: IXY): TXY {
	return vunit(vlint(0.5, ab, ac));
}
export function vnormbisect(ab: IXY, ac: IXY, ablen: number = vlen(ab), aclen: number = vlen(ac)): TXY {
	if (ablen == 0 || aclen == 0) return [1, 0];
	return vsub(vscale(ablen / aclen, ac), ab);
}
export function vlcomb(vs: IXY[], ws: number[]): TXY {
	let r: TXY = [0, 0];
	for (let i in vs) r = [r[0] + vs[i][0] * ws[i], r[1] + vs[i][1] * ws[i]];
	return r;
}
export interface DNode {
	p: TXY;
	h1: TXY;
	h2: TXY;
}
export interface NodePath {
	z: boolean;
	nodes: DNode[]
}

export function smoothhandles(bac: [IXY, IXY, IXY]): [TXY, TXY] {
	let b = bac[0], a = bac[1], c = bac[2];
	let ab = vsub(b, a);
	let ac = vsub(c, a);
	let ablen = vlen(ab);
	let aclen = vlen(ac);
	let dir = vunit(vnormbisect(ab, ac, ablen, aclen));
	return [vadd(a, vscale(-ablen / 3, dir)),
		vadd(a, vscale(aclen / 3, dir))];
}
export function autosmooth(pts: IXY[]): NodePath {
	let n = pts.length;
	if (n <= 2) return {z: true, nodes: []};
	let els: DNode[] = pts.map(a => ({p: [a[0], a[1]], h1: [a[0], a[1]], h2: [a[0], a[1]]} as DNode));
	for (let i = 0; i < n; i++) {
		let ae = els[i], a = pts[i],
			bi = (i + n - 1) % n, b = pts[bi],
			ci = (i + 1) % n, c = pts[ci];
		let aeh12 = smoothhandles([b, a, c]);
		ae.h1 = aeh12[0];
		ae.h2 = aeh12[1];
	}
	return {z: true, nodes: els};
}

export function noderepr(path: NodePath): string {
	return (path.z ? '[Z+ ' : '[Z- ') + path.nodes.map(n =>
		'' + n.h1[0] + ',' + n.h1[1] + '<=' + n.p[0] + ',' + n.p[1] + '=>' + n.h2[0] + ',' + n.h2[1]) + ']'
}

export function eltonodes(path: IPathDEl[]): NodePath {
	if (path.length == 0) return {z:false,nodes:[]};
	if (path[0].c != 'M') throw "Incorrect path start " + drepr(path);
	if (path.length < 3) {
		let p = path[0].p[0];
		return {
			z: false,
			nodes: [{
				p: [p[0],p[1]],
				h1: [p[0],p[1]],
				h2: [p[0],p[1]]
			}]
		};
	}
	let pts = path.filter(pt => pt.c == 'C');
	let nodes: DNode[] = [];
	let n = pts.length;
	let z = path[path.length - 1].c == 'Z';
	if (!z) {
		let p0: IXY = path[0].p[0];
		let p1: IXY = path[1].p[0];
		nodes.push({
			p: [p0[0], p0[1]],
			h1: [p0[0], p0[1]],
			h2: [p1[0], p1[1]]
		});
	}
	for (let i = 0; i < n; i++) {
		let a = pts[i].p, bi = (i + n - 1) % n, b = pts[bi].p;
		let ci = (i + 1) % n, c = pts[ci].p;
		nodes.push({
			p: [a[2][0], a[2][1]],
			h1: [a[1][0], a[1][1]],
			h2: [c[0][0], c[0][1]]
			/*p: [b[2][0], b[2][1]],
			 h1: [b[1][0], b[1][1]],
			 h2: [a[0][0], a[0][1]]*/
		});
	}
	return {z, nodes};
}

export function nodestoels(path: NodePath): PathDEl[] {
	let pts = path.nodes;
	if (pts.length == 0) return [];
	let M:PathDEl = {c: 'M', p: [[pts[0].p[0], pts[0].p[1]] as TXY]};
	if (pts.length == 1) return [M,{c:'z',p:[]} as PathDEl];
	let rslt: PathDEl[] = pts.map(a => ({c: 'C', p: [[0, 0], [0, 0], [0, 0]]as TXY[]}));
	let n = pts.length;
	let i0 = path.z ? 0 : 1;
	rslt[0].p[0] = [pts[0].p[0], pts[0].p[1]];
	for (let i = 0; i < n; i++) {
		let el = rslt[i], node = pts[i],
			prevIdx = (i + n - 1) % n,
			nextIdx = (i + 1) % n,
			prevEl = rslt[prevIdx],
			nextEl = rslt[nextIdx];
		prevEl.p[1] = [node.h1[0], node.h1[1]];
		prevEl.p[2] = [node.p[0], node.p[1]];
		el.p[0] = [node.h2[0], node.h2[1]];
	}
	rslt = [M].concat(rslt);
	if (path.z) {
		rslt.push({c: 'Z', p: []});
	} else {
		rslt.splice(n);
	}
	return rslt;
}

export function smoothd(pts: IXY[]): string {
	return dtostr(nodestoels(autosmooth(pts)))
}

let svgsvg: SVGSVGElement = document.createElementNS(SVGNS, 'svg');
export function tfscale(sx: number, sy: number = sx): SVGTransform {
	let t = svgsvg.createSVGTransform();
	t.setScale(sx, sy);
	return t;
}
export function tfrotate(angle: number, cx: number = 0, cy: number = 0): SVGTransform {
	let t = svgsvg.createSVGTransform();
	t.setRotate(angle, cx, cy);
	return t;
}
export function tftranslate(tx: number, ty: number): SVGTransform {
	let t = svgsvg.createSVGTransform();
	t.setTranslate(tx, ty);
	return t;
}
export function tf2list(src: SVGTransform, dst: SVGTransformList) {
	dst.clear();
	dst.appendItem(src);
}
export function svgscale(svg: SVGTransformable, sx: number, sy: number = sx) {
	let val = svg.transform.baseVal;
	val.appendItem(tfscale(sx, sy));
	val.consolidate();
}
export function svgrotate(svg: SVGTransformable, angle: number, cx: number = 0, cy: number = 0) {
	let val = svg.transform.baseVal;
	val.appendItem(tfrotate(angle, cx, cy));
	val.consolidate();
}
export function svgtranslate(svg: SVGTransformable, tx: number, ty: number) {
	let val = svg.transform.baseVal;
	val.appendItem(tftranslate(tx, ty));
	val.consolidate();
}
export function svguse(href: string, x: number, y: number,
					   attrs?: CreateElementAttrsLite): CEASvgUse {
	return merge1d({
		tag: 'use',
		href, x, y
	} as CEASvgUse, attrs);
}
export function mkrect(x: number, y: number, width: number, height: number): SVGRect {
	let t = svgsvg.createSVGRect();
	t.x = x;
	t.y = y;
	t.width = width;
	t.height = height;
	return t;
}
export function rect_expand(rect: SVGRect, horiz: number, vert: number = horiz): SVGRect {
	rect.x -= horiz;
	rect.y -= vert;
	rect.width += horiz * 2;
	rect.height += vert * 2;
	return rect;
}
export function rect_scale(rect: SVGRect, horiz: number, vert: number = horiz): SVGRect {
	rect.x *= horiz;
	rect.y *= vert;
	rect.width *= horiz;
	rect.height *= vert;
	return rect;
}
export function rect_cpy(src: SVGRect|ClientRect, dst: SVGRect = svgsvg.createSVGRect()): SVGRect {
	if (src instanceof SVGRect) {
		dst.x = src.x;
		dst.y = src.y;
	} else {
		dst.x = src.left;
		dst.y = src.top;
	}
	dst.width = src.width;
	dst.height = src.height;
	return dst;
}
export function unproject(svg: SVGLocatable, x: number, y: number): SVGPoint {
	let pt = svgsvg.createSVGPoint();
	pt.x = x;
	pt.y = y;
	return pt.matrixTransform(svg.getScreenCTM().inverse());
}
export type SVGLocatableElement = SVGElement & SVGTransformable;
type SvgDragEventType = 'sdrag'|'sdragstart'|'sdragstop';
export interface SvgDragEvent extends Event {
	type: SvgDragEventType;
	start: SVGPoint;
	movement: SVGPoint;
	initEvent(eventTypeArg: SvgDragEventType, canBubbleArg: boolean, cancelableArg: boolean): void;
}
export function makeDraggable(el: SVGLocatableElement) {
	let start = svgsvg.createSVGPoint();
	let movement = svgsvg.createSVGPoint();
	let dragging = false;

	function mkDragEvent(type: SvgDragEventType): SvgDragEvent {
		let dragEvent = document.createEvent("Event") as SvgDragEvent;
		dragEvent.initEvent(type, true, true);
		dragEvent.movement = movement;
		dragEvent.start = start;
		return dragEvent;
	}

	let onmove: (e: MouseEvent) => any;

	function stopDragging() {
		if (dragging) {
			dragging = false;
			let dragEvent = mkDragEvent("sdragstop");
			el.dispatchEvent(dragEvent);
		}
		el.ownerSVGElement.removeEventListener('mousemove', onmove, false);
		el.ownerDocument.body.removeEventListener('mouseup', onmup, false);
	}

	function onmdown(e: MouseEvent) {
		if (e.button != 0) return;
		let svg = el.ownerSVGElement;
		if (el.parentNode) el.parentNode.appendChild(el); // move to top
		const x = el.tagName == 'circle' ? 'cx' : 'x';
		const y = el.tagName == 'circle' ? 'cy' : 'y';
		const mouseStart = unproject(svg, e.clientX, e.clientY);
		let matrix0 = el.transform.baseVal.consolidate().matrix.translate(0, 0);
		start.x = matrix0.e;
		start.y = matrix0.f;
		movement.x = movement.y = 0;
		let dragEvent = mkDragEvent("sdragstart");
		el.dispatchEvent(dragEvent);
		if (dragEvent.defaultPrevented) return;
		dragging = true;
		el.ownerDocument.body.addEventListener('mouseup', onmup, false);
		onmove = (e: MouseEvent) => {
			if ((1 & e.buttons) == 0) return stopDragging();
			const current = unproject(svg, e.clientX, e.clientY);
			movement.x = current.x - mouseStart.x;
			movement.y = current.y - mouseStart.y;
			const unprojectmx = svg.getScreenCTM().inverse().multiply(el.getScreenCTM());
			unprojectmx.e = unprojectmx.f = 0;
			movement = movement.matrixTransform(unprojectmx);
			let dragEvent = mkDragEvent("sdrag");
			el.dispatchEvent(dragEvent);
			if (dragEvent.defaultPrevented) return;
			el.transform.baseVal.initialize(el.transform.baseVal.createSVGTransformFromMatrix(matrix0.translate(movement.x, movement.y)));
		};
		svg.addEventListener('mousemove', onmove, false);
	}

	function onmup(e: MouseEvent) {
		if (e.button != 0) return;
		stopDragging();
	}

	el.addEventListener('mousedown', onmdown, false);
}
export function v3scale(r: IXY, x: number): IXY {
	return r.map(ri => ri * x)
}
export function v3add(ra: IXY, rb: IXY): IXY {
	return ra.map((ai, i) => ai + rb[i])
}

export function solve2(vx: IXY, vy: IXY, vc: IXY): TXY {
	const m = [
		[vx[0], vy[0], vc[0]],    //  a*v1x + b*v2x = cx
		[vx[1], vy[1], vc[1]]];   //  a*v1y + b*v2y = cy
	//  a*m00 + b*m01 = m02
	//  a*m10 + b*m11 = m12
	if (m[0][0] == 0) {
		//        + b*m01 = m02
		//  a*m10 + b*m11 = m12
		const b = m[0][2] / m[0][1];
		//  a*m10         = m12 - b*m11
		const a = (m[1][2] - b * m[1][1]) / m[1][0];
		return [a, b];
	}
	m[0] = v3scale(m[0],1/m[0][0]);
	//  a     + b*m01 = m02
	//  a*m10 + b*m11 = m12
	if (m[1][0] != 0) {
		m[1] = v3add(m[1], v3scale(m[0], -m[1][0]));
	}
	//  a     + b*m01 = m02
	//          b*m11 = m12
	m[1] = v3scale(m[1], 1 / m[1][1]);
	//  a     + b*m01 = m02
	//          b     = m12
	m[0] = v3add(m[0], v3scale(m[1], -m[0][1]));
	//  a             = m02
	//          b     = m12
	return [m[0][2], m[1][2]];
}
type Line = [number, number, number]; ///< [f,g,h] such as:   f*x + g*y = h.
/**
 * @returns line from two points,
 * possible forms: [1,g,h], [f,1,h], [1,0,h], [0,1,h]
 */
export function line(a: IXY, b: IXY): Line {
	let [ax, ay] = a, [bx, by] = b;
	// f*x + g*y = h
	let dx = bx - ax, dy = by - ay;
	if (dx == 0) return [1, 0, ax]; // ax == bx  ->  x(y) = ax
	if (dy == 0) return [0, 1, ay]; // ay == by  ->  y(x) = ay
	// f*dx + g*dy = 0
	// ^ underedfined so we take either of
	//   dx + g*dy = 0  or
	// f*dx +   dy = 0
	if (Math.abs(dx)>Math.abs(dy)) {
		let g = -dx / dy;
		//    x +  g*y = h
		return [1, g, ax + g * ay];
	} else {
		let f = -dy/dx;
		// f*x + y = h
		return [f, 1, f * ax + ay];
	}
}

/**
 * Пересечение прямой (a1;a2) и (b1;b2)
 */
export function v22intersect(a1: IXY, a2: IXY, b1: IXY, b2: IXY): TXY {
	let line1 = line(a1, a2);
	let line2 = line(b1, b2);
	let [f1, g1, h1] = line1;
	let [f2, g2, h2] = line2;
	return solve2([f1, f2], [g1, g2], [h1, h2]);
}

/**
 * Проекция точки 'p' на линию 'ab'.
 */
export function ptproj(a: IXY, b: IXY, p: IXY): TXY {
	let ab = vsub(b,a);
	let ab1 = vunit(ab);
	let ap = vsub(p,a);
	let n = vdot(ap,ab1);
	return vadd(a,vscale(n,ab1));
}