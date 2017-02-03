package com.aimozg.psvg.model

import com.aimozg.psvg.model.node.CuspNode
import com.aimozg.psvg.model.node.Flow1Node
import com.aimozg.psvg.model.node.NodePath
import com.aimozg.psvg.model.node.SmoothNode
import com.aimozg.psvg.model.point.*
import com.aimozg.psvg.model.segment.*
import com.aimozg.psvg.model.values.FixedColor
import com.aimozg.psvg.model.values.FixedFloat


@Suppress("unused")
val ALL_LOADERS: Collection<PartLoader> = listOf(
		Group.GROUP_LOADER,
		FixedFloat.FIXEDFLOAT_LOADER,

		FixedColor.FIXEDCOLOR_LOADER,

		Style.STYLE_LOADER,

		PointAtProjection.POINT_AT_PROJECTION_LOADER,

		NodePath.PATH_LOADER,
		SegmentedPath.PATH_SEGMENTED_LOADER,

		MoveTo.SEGMENT_M_LOADER,
		LineTo.SEGMENT_L_LOADER,
		CubicTo.SEGMENT_C_LOADER,
		ZSegment.SEGMENT_Z_LOADER,

		AbsoluteHandle.HANDLE_ABS_LOADER,
		RelativeHandle.HANDLE_REL_LOADER,
		FlowHandle.HANDLE_FLOW_LOADER,
		SmoothHandle.HANDLE_SMOOTH_LOADER,

		FixedPoint.POINT_FIXED_LOADER,
		PointRef.POINT_REF_LOADER,
		PointFromNormal.POINT_FROM_NORMAL_LOADER,
		PointAtIntersection.POINT_AT_INTERSECTION_LOADER,

		CuspNode.NODE_CUSP_LOADER,
		Flow1Node.NODE_FLOW1_LOADER,
		SmoothNode.NODE_SMOOTH_LOADER
)