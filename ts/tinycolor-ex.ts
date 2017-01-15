import tinycolor2 = require('tinycolor2');

export class TinyColorEx {
    public c:tinycolorInstance;
    constructor(c:tinycolorInstance|string) {
        if (typeof c == 'string') this.c = tinycolor2(c);
        else if (c instanceof TinyColorEx) this.c = c.c.clone();
        else this.c = c;
    }
    darken(by:number):this {
        this.c.darken(by);
        return this;
    }
    lighten(by:number):this {
        this.c.lighten(by);
        return this;
    }
    setA(a:number):this {
        this.c.setAlpha(a);
        return this;
    }
    mulL(l:number):this {
        var hsl = this.c.toHsl();
        hsl.l *= l;
        this.c = tinycolor2(hsl);
        return this;
    }
    darkenTo(max:number):this {
        var hsl = this.c.toHsl();
        hsl.l = Math.max(hsl.l,max);
        this.c = tinycolor2(hsl);
        return this;
    }
    toString():string {
        return this.c.toString();
    }
}
export type ColorFormatAny = ColorFormats.RGB|ColorFormats.RGBA|ColorFormats.HSL|
    ColorFormats.HSLA|ColorFormats.HSV|ColorFormats.HSVA;
export function tinycolor_ex(color: string): TinyColorEx;
export function tinycolor_ex(color: ColorFormats.RGB): TinyColorEx;
export function tinycolor_ex(color: ColorFormats.RGBA): TinyColorEx;
export function tinycolor_ex(color: ColorFormats.HSL): TinyColorEx;
export function tinycolor_ex(color: ColorFormats.HSLA): TinyColorEx;
export function tinycolor_ex(color: ColorFormats.HSV): TinyColorEx;
export function tinycolor_ex(color: ColorFormats.HSVA): TinyColorEx;
export function tinycolor_ex(color: string|ColorFormatAny):TinyColorEx {
    return new TinyColorEx(tinycolor2(color as any as string));
}
