import {ModelLoader} from "./api";
import {NODE_SMOOTH_LOADER} from "./nsmooth";
import {NODE_FLOW1_LOADER} from "./nflow";
import {POINT_FIXED_LOADER} from "./ptfixed";
import {POINT_AT_INTERSECTION_LOADER} from "./ptintersect";
import {POINT_FROM_NORMAL_LOADER} from "./ptnorm";
import {POINT_AT_PROJECTION_LOADER} from "./ptproj";
import {NODE_CUSP_LOADER} from "./ncusp";
import {POINT_REF_LOADER} from "./ptref";
export const ALL_LOADERS:ModelLoader[] = [
	NODE_SMOOTH_LOADER,
	NODE_CUSP_LOADER,
	NODE_FLOW1_LOADER,
	POINT_FIXED_LOADER,
	POINT_REF_LOADER,
	POINT_FROM_NORMAL_LOADER,
	POINT_AT_INTERSECTION_LOADER,
	POINT_AT_PROJECTION_LOADER
];
