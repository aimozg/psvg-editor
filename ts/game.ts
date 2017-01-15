import dom = require('./dom');
require('jquery');
import * as face from "./face";
import {SetOf} from "./set";

export interface PassageAction {
    name: string;
    disabled?: boolean;
    action?: ()=>TPassage;
}
export type TPassageAction = PassageAction|[string,FPassage|undefined,string]|[string,FPassage|undefined];
export interface Passage {
    body: HTMLElement|string;
    actions: TPassageAction[];
}
export type TPassage = Passage|[string,TPassageAction[]];
export type FPassage = ()=>TPassage;

var jPassage:JQuery;
var jActions:JQuery; 
export function displayPassage(passage:TPassage){
    var p:Passage = typeof passage[0] == 'string' ? 
        {body:passage[0], actions:passage[1]} : passage as Passage;
    jPassage.html('').append(typeof p.body == 'string' ? 
        dom.createElement({tag:'div',text:p.body}) : p.body);
    jActions.html('').append(p.actions.map(a=>{
        let text = a[0]||(a as PassageAction).name
        let options = SetOf((a[2]||'').split(''));
        let disabled = options['d']||(a as PassageAction).disabled
        let action = a[1]||(a as PassageAction).action;
        return $(dom.createElement({
            tag:'button',
            text, disabled
        })).click(()=>displayPassage(action()))
    }));
}
export function displayFace(app:face.Appearance){
    $('#svgCanvas').html('').append($(face.renderFace(app)).
        attr('transform','matrix(2,0,0,2,130,170)'));
}

$(()=>{
    jPassage = $("[data-role=passage]");
    jActions = $("[data-role=actions]");
});
