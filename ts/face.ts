import dom = require('./dom');
import {CEAStyle, CreateElementAttrs} from "./dom";
import {tinycolor_ex as TCX, TinyColorEx} from "./tinycolor-ex";
import {IXY} from "./svg";
import svg = require("./svg");
import _ = require("underscore");
type Dictionary<T> = _.Dictionary<T>;

export namespace Colors {
    export namespace Skin {
        export const Pale   = "hsl(28,44%,88%)";
        export const Light  = "hsl(27,47%,76%)";
        export const Fair   = "hsl(26,49%,64%)";
        export const Bronze = "hsl(25,50%,52%)";
        export const Dark   = "hsl(24,46%,40%)";
        export const Ebony  = "hsl(23,43%,28%)";
    }
    export namespace Eye {
        export const Blue   = "hsl(200,48%,60%)";
        export const Aqua   = "hsl(180,24%,70%)";
        export const Green  = "hsl(120,24%,40%)";
        export const Amber  = "hsl( 25,48%,60%)";
        export const Hazel  = "hsl( 25,36%,50%)";
        export const Brown  = "hsl( 25,24%,40%)";
        export const Dark   = "hsl( 25,12%,30%)";

        export const Red    = "hsl(360,40%,50%)";
        export const Pink   = "hsl(300,24%,80%)";
        export const Purple = "hsl(270,24%,50%)";
        export const Steel  = "hsl(  0, 0%,50%)";
        export const Gray   = "hsl(  0, 0%,60%)";
        export const Silver = "hsl(  0, 0%,70%)";
        export const Golden = "hsl( 40,48%,50%)";

        export const Crystal= "hsl(180,10%,90%)";
        export const Ruby   = "hsl(360,72%,60%)";
        export const Topaz  = "hsl( 60,64%,60%)";
        export const Malachyte="hsl(120,40%,50%)";
        export const Emerald= "hsl(120,64%,60%)";
        export const Beryll = "hsl(150,48%,60%)";
        export const Sapphire="hsl(230,60%,60%)";
        export const Amethyst="hsl(270,56%,70%)";
    }
}
export interface Appearance {
    cheekSz: number;
    noseSz: number;
    
    skinColor?: string;
    eyeColor?: string;
    hairColor?: string;

    matchLash?: boolean;
    matchBrow?: boolean;

    mouthColor?: string;
    lashColor?: string;
    browColor?: string;
}
const defaultFace:Appearance = getDefaultAppearance();
export function getDefaultAppearance():Appearance {
    return {
        cheekSz: 0.5,
        noseSz: 0.5,
        skinColor: Colors.Skin.Light,
        eyeColor: Colors.Eye.Brown,
        hairColor: '#4477aa',
        mouthColor: '#fa8d8d',
        lashColor: '#201710',
        browColor: '#201710'
    };
}
type TStyColor = string|TinyColorEx|null;
function sty(fill:TStyColor,stroke:TStyColor,other?:dom.CEAStyle):CEAStyle {
    let s = {fill:fill||'none',stroke:stroke||'none'};
    if (other) dom.merge1d(s,other);
    return s;
}
type TSAItem = [string[], TStyColor, TStyColor, CEAStyle|undefined];
class Dependencies<T> {
    public requires:Dictionary<boolean> = {};
    depend(key:string): ((key:Dictionary<T>)=>T) {
        this.requires[key]=true;
        return d=>d[key];
    }
    constructor() {
        
    }
}
interface Face {
    s:Dictionary<CEAStyle>;
    p:Dictionary<IXY>;
}
interface FacePart {
    styles:Dependencies<CEAStyle>;
    points:Dependencies<IXY>;
    create:(Face)=>CreateElementAttrs[];
}
function path(style:CEAStyle,clazz:string,d:string):dom.CreateElementAttrs {
    return {tag:'path','class':clazz,style:style||{},d};
}
const _path=path;
function g(clazz:string,...elements:dom.CreateElementAttrs[]):dom.CreateElementAttrs {
    return {tag:'g','class':clazz,items:elements};
}
function g2(clazz:string,elements:dom.CreateElementAttrs[],transform?:string):dom.CreateElementAttrs {
    return {tag:'g','class':clazz,style:{transform:transform||''},items:elements};
}
export namespace Head {
    export const skinElements:FacePart = (()=>{
        let styles = new Dependencies<CEAStyle>();
        let points = new Dependencies<IXY>();
        let skinstyle = styles.depend('skin');
        let CrownR = points.depend('CrownR'), CrownL = points.depend('CrownL'),
            TempR = points.depend('TempR'), TempL = points.depend('TempL'),
            JawR = points.depend('JawR'), JawL = points.depend('JawL'),
            ChinR = points.depend('ChinR'), ChinL = points.depend('ChinL');
        return {
            styles,points,
            create: f => [
                path(skinstyle(f.s),'skin',
                    svg.smoothd([
                        CrownR(f.p), TempR(f.p), JawR(f.p), ChinR(f.p), 
                        ChinL(f.p), JawL(f.p), TempL(f.p), CrownL(f.p)
                    ]))
                ]
        };
    })();
    export const noseElements:FacePart = (()=>{
        let styles = new Dependencies<CEAStyle>();
        let points = new Dependencies<IXY>();
        let noseline = styles.depend('nose-line');
        let noseshadow = styles.depend('nose-shadow');
        let NoseTop = points.depend('NoseTop'), NoseTip = points.depend('NoseTip'),
            NostL = points.depend('NostL'), NostR = points.depend('NostR');
            
        return {
            styles, points,
            create: f => {
                let top = NoseTop(f.p);
                let right = NostR(f.p);
                let tip = NoseTip(f.p);
                let left = NostL(f.p);
                return [
                    path(noseshadow(f.s), 'nose-shadow', 
                        svg.dtostr([
                            {c:'M',p:[top]},
                            {c:'C',p:[
                                svg.vlcomb([top,right],[11/8,1/2]), //a
                                svg.vlcomb([top,right],[1/4,1]),right]}, //b
                            {c:'C',p:[
                                svg.vlcomb([right,tip],[1,1/7]), //c
                                svg.vlcomb([right,tip],[1/4,23/28]),tip]}, //d
                            {c:'C',p:[
                                svg.vlcomb([tip,left],[15/28,1/4]),top,top]}, //e
                            {c:'Z',p:[]}
                            ])),
                        //'M[0,-4] C[2,-3 4,4 4,5][4,6 1,7 0,7][-1,5 0,-4 0,-4] Z'),
                        //   top     a    b right  c   d   tip  e    top   top
                    path(noseline(f.s), 'nose-line',
                        svg.dtostr([
                            {c:'M',p:[top]},
                            {c:'L',p:[tip]},
                            {c:'C',p:[
                                svg.vlcomb([left,tip],[1/4,23/28]),
                                svg.vlcomb([left,tip],[3/4,9/28]) // h
                                ,left]}
                            ]))
                        //'M 0,-4 C -2,-3 -4,4 -4,5 -3,6 -1,7 0,7 -1,5 0,-4 0,-4 Z')
                        //   top    f      g   left h    i    tip  e   top  top
                        // M 0,-4 C -2, 8 -4,4 -4,5 -3,6 -1,7 0,7 -1,5 0,-4 0,-4 Z
                        // -6-5/2 = (-4)f[0]
                    ];
            }
        };
    })();
}
export function renderFace(appr:Appearance):SVGElement {
    let skinColor = appr.skinColor||defaultFace.skinColor;
    let hairColor = appr.hairColor||defaultFace.hairColor;
    let eyeColor = appr.eyeColor||defaultFace.eyeColor;

    let mouthColor = appr.mouthColor||defaultFace.mouthColor;
    let eyeWhiteColor = '#fff';
    let browColor = appr.browColor||(appr.matchLash?appr.hairColor:defaultFace.browColor);
    let eyeLashColor = appr.lashColor||(appr.matchLash?hairColor:defaultFace.lashColor);
    let stylesArray = [
        [['skin', 'ear-base'], skinColor, TCX(skinColor).darkenTo(16)],
        [['eye-skin-mask'], skinColor, null, {'fill-rule': 'evenodd'}],
        [['nose-line'], null, TCX(skinColor).darkenTo(16), {'stroke-width': 0.5}],
        [['nose-shadow', 'lip-shadow'], TCX(skinColor).setA(0.5).mulL(0.5), null],
    
        [['eye-lid-top', 'mouth-shut'], null, TCX(skinColor).darkenTo(16), {'stroke-width': 0.5}],
        [['eye-lash-top', 'eye-lash-top-extra'], eyeLashColor, TCX(eyeLashColor).darkenTo(16), {'stroke-width': 0.25}],
        [['brow-main'], browColor, TCX(browColor).darkenTo(16),{'stroke-width':0.5}],
        
        [['mouth-open'], TCX(mouthColor), TCX(mouthColor).darkenTo(16)],
        [['eye-white'], eyeWhiteColor, null],
        [['iris-dark'], TCX(eyeColor).mulL(0.25), TCX(eyeColor).darkenTo(16), {'stroke-width': 0.5}],
        [['iris-light'], TCX(eyeColor), null],
        [['iris-pupil'], TCX(eyeColor).mulL(0.5), TCX(eyeColor).darkenTo(16), {'stroke-width': 0.5}],
    ] as TSAItem[];
    let styles: Dictionary<CEAStyle> = {};
    for (let ss of stylesArray) for (let s of ss[0]) styles[s] = sty(ss[1], ss[2], ss[3]);
    
    let path = (c:string,d:string)=>_path(styles[c],c,d);

    // 'skin' points
    let JawR=svg.vlint(appr.cheekSz,[45,0],[40,30]),JawL=[-JawR[0],JawR[1]];
    let ChinW=20,ChinY=44,ChinR=[ChinW/2,ChinY],ChinL=[-ChinW/2,ChinY];
    let TempX=50,TempY=-22,TempR=[TempX,TempY],TempL=[-TempX,TempY];
    let CrownX=35,CrownY=-70,CrownR=[CrownX,CrownY],CrownL=[-CrownX,CrownY];
    // 'nose' points
    let noseFactor = 1+appr.noseSz;
    let NoseTop=[0, -4*noseFactor], NoseTip=[0, 7*noseFactor], 
        NostY=5*noseFactor, NostW=4*noseFactor, 
        NostL=[-NostW,NostY], NostR=[NostW,NostY];
    let face:Face = {
        s:styles,
        p:{
            JawL,JawR,ChinL,ChinR,TempL,TempR,CrownL,CrownR,
            NoseTop,NoseTip,NostL,NostR
        }
    };

    function eyeElements():dom.CreateElementAttrs[] {
        return [
                path('eye-white', 'm -17,-9 41,0 0,22 -41,0 z'),
                g2('eye-iris', [
                    path('iris-dark', 'M 0,-6 C 2.8284271,-6 6,-2.8284271 6,0 6,2.8284271 2.8284271,6 0,6 -2.8284271,6 -6,2.8284271 -6,0 -6,-2.8284271 -2.8284271,-6 0,-6 Z'),
                    path('iris-light', 'M 0,-2 C 2,-2 5,-1 6,0 6,3 3,6 0,6 -2.828427,6 -6,3 -6,0 -5,-1 -2,-2 0,-2 Z'),
                    path('iris-pupil', 'M 0,-3 C 1.20185,-3 2,-1.20185 2,0 2,1.20185 1.20185,3 0,3 -1.20185,3 -2,1.20185 -2,0 -2,-1.20185 -1.20185,-3 0,-3 Z')
                    ]),
                path('eye-skin-mask', 'M -3,10 C 0,10 5,8 7,7 9,6 9,6 8,4 7,2 5,0 4,-1 3,-2 -8,-3 -10,-2 c -2,1 -5,2 -2,6 3,4 6,6 9,6 z m -15,4 43,0 0,-24 -43,0 z'),
                path('eye-lid-top', 'm -10,-4 c 2,-1 13,1 14,2 1,1 3,4 4,6'),
                path('eye-lash-top', 'm -12,4 c -2,-1 -4,-4 -4,-4 l 6,-3 c 2,-1 13,0 14,1 1,1 3,4 4,6 L 8,4 C 7,2 5,0 4,-1 3,-2 -8,-3 -10,-2 c -2,1 -5,2 -2,6 z'),
                path('eye-lash-top-extra', 'm -10,-3 -6,-1 6,0 C -8,-5 7,-4 9,-2 9,-2 6,-3 4,-3 6,-2 8,1 8,1 8,1 6,-1 4,-2 3,-3 -8,-4 -10,-3 Z')
            ]
    }
    function earElements():dom.CreateElementAttrs[] {
        return [
            path('ear-base', 'M -4,-20 C -6,-24 -10,-24 -12,-20 -14,-16 -16,-10 -13,-4 -10,2 -2,8 4,7')
        ]
    }
    function hairElements():dom.CreateElementAttrs[] {
        // lg 
        /* return [
            path('hair-base hair-base-lg', 'M 45,25 C 45,25 65,5 55,-25 45,-45 25,-55 0,-55 c -25,0 -45,10 -55,30 -10,30 10,50 10,50 l 1,-20 c 10,-20 19,-24 44,-24 25,0 34,4 44,24 z')
        ]; /**/
        // md
        /*return [
            path('hair-base hair-base-md', 'm 45,15 c 0,0 10,-20 0,-40 -5,-10 -20,-20 -45,-20 -25,0 -40,10 -45,20 -10,20 0,40 0,40 l 1,-10 c 10,-20 19,-24 44,-24 25,0 34,4 44,24 z')
        ]; /**/
        // sm
        /* return [
            path('hair-base hair-base-sm', 'M 45,10 C 25,10 0,20 0,50 11,30 20,26 45,26 70,26 80,30 90,50 90,30 75,10 45,10 Z')
        ]; /**/
        return [];
    }
    function browElements():dom.CreateElementAttrs[] {
        return [
            path('brow-main','M 15,2 12,3 C 11,1 4,-2.5 0,-3 c -7,-1 -19,-4 -21,-10 3,4 14,7 21,8 4,0.5 14,5 15,7 z')
        ];
    }
    function mouthElements():dom.CreateElementAttrs[] {
        return [
            path('mouth-shut','M -9,2 C -9,1 -3,0 0,0 3,0 9,1 9,2'),
            path('lip-shadow','M -5,5 C -5,4 -2,3 0,3 2,3 5,4 5,5 5,6 2,6 0,6 -2,6 -5,6 -5,5 Z')
        ];
    }
    return dom.SVGItem({
        tag:'g',
        'class':'face',
        items:[
            // skin
            g2('ear left',earElements(),'translate(-45,0)'),
            g2('ear right',earElements(),'matrix(-1,0,0,1,45,0)'),
            g2('skin',Head.skinElements.create(face)),
            // eyes
            g2('eye left',eyeElements(),'translate(-25,-15)'),
            g2('eye right',eyeElements(),'matrix(-1,0,0,1,25,-15)'),
            // other stuff
            g2('nose', Head.noseElements.create(face), 'translate(0,'+5.5*noseFactor+')'),
            g2('mouth',mouthElements(),'translate(0,26)'),
            // hair
            g2('hair',hairElements()),
            // overlay
            g2('brow left',browElements(),'translate(-25,-15)'),
            g2('brow right',browElements(),'matrix(-1,0,0,1,25-15)')/*,
            path('',{fill:'none',stroke:'#f0f'},'M -5,0 5,0 0,5 0,-5 m 65,60 -130,0 0,-140 130,0 z')/**/
        ]
    });
}