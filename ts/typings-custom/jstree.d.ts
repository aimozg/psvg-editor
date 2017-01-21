type JSTreeNodeInit = AbstractJSTreeNode;
interface AbstractJSTreeNode {
	id?: string;
	text: string;
	icon?: string|boolean;
	state?: {
		opened?: boolean  // is the node open
		disabled?: boolean  // is the node disabled
		selected?: boolean  // is the node selected
	};
	li_attr?: {[index: string]: any;};
	a_attr?: {[index: string]: any;};
	children?: (string|JSTreeNodeInit)[];
	parent?: string;
}
interface JSTreeNode extends AbstractJSTreeNode {
	id: string;
	text: string;
	icon: string|boolean;
	state: {
		opened: boolean;
		disabled: boolean;
		selected: boolean;
	};
	li_attr: {[index: string]: any;}
	a_attr: {[index: string]: any;}
	parent: string;
	parents: string[];
	children: string[];
}
type TJSTreeOp = 'create_node'|'rename_node'|'delete_node'|'move_node'|'copy_node';
type TJSTreeCheckCallback = (operation:TJSTreeOp, node:JSTreeNode, node_parent:JSTreeNode, node_position:number, ...more:any[])=>boolean;
interface JSTreeOptions {
	core: {
		data?: false|JQueryAjaxSettings|JSTreeNodeInit;

		check_callback?: boolean| TJSTreeCheckCallback;
	}
}
type TNodeRef = string|JQuery|JSTreeNode;
interface JSTreeNodeEvent {
	event?: JQueryEventObject;
	node: JSTreeNode;
	selected: JSTreeNode[];
}

interface JSTree {
	destroy();
	get_container():JQuery;

	get_node(obj: string|Element): JSTreeNode;
	get_node(obj: string|JQuery, as_dom: true): JQuery;
	// load_node
	open_node(obj: TNodeRef); // callback, animation
	// close_node(node[,animation])
	toggle_node(obj: TNodeRef);
	enable_node(obj: TNodeRef);
	hide_node(obj: TNodeRef);
	show_node(obj: TNodeRef);
	select_node(obj: TNodeRef, supress_event?:boolean, prevent_open?:boolean);
	deselect_node(obj: TNodeRef); // suppress_event
	select_all(suppress_event?: boolean);
	deselect_all(suppress_event?: boolean);
	refresh_node(obj: TNodeRef);
	create_node(par: TNodeRef, node: string|JSTreeNodeInit, pos?: number|'first'|'last', callback?: Function): string;
	rename_node(obj: TNodeRef|TNodeRef[], name: string): boolean;
	delete_node(obj: TNodeRef|TNodeRef[], name: string): boolean;
	move_node(obj: TNodeRef, par: TNodeRef, pos?: number|'first'|'last'|'before'|'after', callback?: Function);
}
interface JQuery {
	jstree(options: JSTreeOptions): JQuery;
	jstree(): JSTree;
}
