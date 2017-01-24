import {createElement} from "../dom";
import {Value, EPartCategory} from "./api";
import {ModelContext} from "./_ctx";

export class ValueFloat extends Value<number> {
	private input: HTMLInputElement|null = null;

	public get category(): EPartCategory {
		return "ValueFloat";
	}

	constructor(ctx:ModelContext,
				name:string|undefined,
				private value:number,
				public readonly def:number|undefined,
				public readonly min:number,
				public readonly max:number
	) {
		super(ctx,name);
		if (def<min || def>max || min>max) throw `Illegal bounds for ${name}: def=${def} min=${min} max=${max}`
	}

	public get(): number {
		return this.value;
	}
	public set(value:number, suppressEvent: boolean = false){
		if (!this.validate(value)) return;
		this.value = value;
		if (this.input) this.input.value = ''+value;
		if (!suppressEvent) this.update('*');
	}

	public save(): number|string {
		return this.get();
	}

	public validate(x:number):boolean {
		return isFinite(x) && x>=this.min && x<=this.max;
	}
	public editorElement(): HTMLElement {
		let id = 'valuefloat_'+this.id;
		const handler = (e:Event)=>{
			const input = (e.target as HTMLInputElement);
			const s = input.value.trim();
			const value = (s === '' && this.def !== undefined) ? this.def : +s;
			if (this.validate(value)) {
				this.set(value);
				input.classList.remove('-error');
			} else {
				input.classList.add('-error');
			}
		};
		const value = this.get();
		return createElement('div',{
			'class':'Value ValueFloat',
			items: [
				{
					tag: 'label',
					'for': id,
					text: this.name
				},{
					tag:'input',
					type: 'text',
					placeholder: this.def,
					id: id,
					value: value === this.def ? '' : value,
					callback: (e:Element) => this.input = e as HTMLInputElement,
					onchange: handler,
					oninput: handler
				}
			]
		});
	}
	public static load(name:string,
					   ctx:ModelContext,
					   json:any,
					   def:number|undefined,
					   min:number,
					   max:number): ValueFloat {
		let x:number;
		if (def !== undefined && (json === null || json === undefined)) {
			x = def;
		} else if (typeof json == 'number') {
			x = json;
		} else if (typeof json == 'string') {
			x = +json;
			if (!isFinite(x)) throw JSON.stringify(json);
		} else throw JSON.stringify(json);
		return new ValueFloat(ctx,name,x,def,min,max);
	}

}