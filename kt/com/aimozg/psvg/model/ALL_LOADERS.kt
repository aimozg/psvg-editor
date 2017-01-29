package com.aimozg.psvg.model

import com.aimozg.psvg.model.segment.LineTo
import com.aimozg.psvg.model.segment.MoveTo
import com.aimozg.psvg.model.segment.SegmentedPath


@Suppress("unused")
val ALL_LOADERS: Collection<PartLoader> = listOf(
		CuspNode.NODE_CUSP_LOADER,
		Flow1Node.NODE_FLOW1_LOADER,
		SmoothNode.NODE_SMOOTH_LOADER,

		Parameter.PARAM_LOADER,

		NodePath.PATH_LOADER,
		SegmentedPath.PATH_SEGMENTED_LOADER,

		MoveTo.SEGMENT_M_LOADER,
		LineTo.SEGMENT_L_LOADER,

		FixedPoint.POINT_FIXED_LOADER,
		PointRef.POINT_REF_LOADER,
		PointFromNormal.POINT_FROM_NORMAL_LOADER,
		PointAtIntersection.POINT_AT_INTERSECTION_LOADER,
		PointAtProjection.POINT_AT_PROJECTION_LOADER,

		ValueFloat.VALUEFLOAT_LOADER
)