import {createElement} from "../dom";
import {Value, ModelContext} from "./api";
export class ValueFloat extends Value<number> {
	private input: HTMLInputElement|null = null;
	constructor(name:string,
				ctx:ModelContext,
				private value:number) {
		super(name,ctx);
	}

	public get(): number {
		return this.value;
	}
	public set(value:number, suppressEvent: boolean = false){
		this.value = value;
		if (this.input) this.input.value = ''+value;
		if (!suppressEvent) this.update('*');
	}

	public save(): number|string {
		return this.get();
	}

	public editorElement(): HTMLElement {
		let id = 'valuefloat_'+this.id;
		const handler = (e:Event)=>{
			const input = (e.target as HTMLInputElement);
			const value = +input.value;
			if (isFinite(value)) {
				this.set(value);
				input.classList.remove('-error');
			} else {
				input.classList.add('-error');
			}
		};
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
					placeholder: '1.234',
					id: id,
					value: this.get(),
					callback: (e:Element) => this.input = e as HTMLInputElement,
					onchange: handler,
					oninput: handler
				}
			]
		});
	}
	public static load(name:string,ctx:ModelContext,json:any,def?:number): ValueFloat {
		let x:number;
		if (def !== undefined && (json === null || json === undefined)) {
			x = def;
		} else if (typeof json == 'number') {
			x = json;
		} else if (typeof json == 'string') {
			x = +json;
			if (!isFinite(x)) throw JSON.stringify(json);
		} else throw JSON.stringify(json);
		return new ValueFloat(name,ctx,x);
	}

}