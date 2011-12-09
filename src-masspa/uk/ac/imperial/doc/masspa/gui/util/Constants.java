package uk.ac.imperial.doc.masspa.gui.util;

import java.awt.Color;

public class Constants
{
	// Main window
	public static final int s_MAIN_TOP = 0;
	public static final int s_MAIN_LEFT = 0;
	public static final int s_MAIN_RIGHT = 1200;
	public static final int s_MAIN_BOTTOM = 800;
	public static final int s_MAIN_BORDER = 5;
	public static final double s_MAIN_EDITORS_SIZE_VERT = 0.7;
	
	// Agent editor
	public static final double s_AGENT_EDITOR_DEF_SIZE_HORI = 0.8;
		
	// Location editor
	public static final double s_LOCATION_EDITOR_SIZE_VERT_TOP = 0.0;
	public static final double s_LOCATION_EDITOR_SIZE_HORI = 0.8;
	public static final int s_LOCATION_EDITOR_FLOW_LAYOUT_GAP_H = 10;
	public static final int s_LOCATION_EDITOR_FLOW_LAYOUT_GAP_V = 5;
	public static final int s_LOCATION_EDITOR_LOCS_WIDTH_DEFAULT = 3;
	public static final int s_LOCATION_EDITOR_NOF_RING_WIDTH_DEFAULT = 3;
	public static final String s_LOCATION_EDITOR_NOF_RINGS_DEFAULT = "5";
	public static final String s_LOCATION_EDITOR_NOF_LOCS_PER_RING_DEFAULT = "5";
	public static final int s_LOCATION_EDITOR_NOF_LOCS_PER_RING_WIDTH_DEFAULT = 3;
	public static final String s_LOCATION_EDITOR_LOCS_X_DEFAULT = "5";
	public static final String s_LOCATION_EDITOR_LOCS_Y_DEFAULT = "5";
	public static final int  s_LOCATION_VIEWER_BRUSH_WIDTH = 3;
	public static final int  s_LOCATION_VIEWER_BRUSH_WIDTH_THIN = 2;
	public static final double s_LOCATION_VIEWER_SCALE_DEFAULT = 100;
	public static final int s_LOCATION_VIEWER_CANVAS_WIDTH = 2000;
	public static final int s_LOCATION_VIEWER_CANVAS_HEIGHT = 2000;
	public static final double s_LOCATION_VIEWER_SIZE_VIEWER = 1;
	public static final int s_LOCATION_VIEWER_ZOOM_DEFAULT = 100;
	public static final int s_LOCATION_VIEWER_ZOOM_MAX = 200;
	public static final double s_LOCATION_VIEWER_LOC_RADIUS_DEFAULT = 0.25;
	public static final Color s_LOCATION_VIEWER_LOC_COMP_FILL_DEFAULT = Color.WHITE;
	public static final Color s_LOCATION_VIEWER_LOC_COMP_BORDER_DEFAULT = Color.BLACK;
	public static final Color s_LOCATION_VIEWER_LOC_COMP_FILL_MOUSE_OVER = Color.WHITE;
	public static final Color s_LOCATION_VIEWER_LOC_COMP_BORDER_MOUSE_OVER = Color.GREEN;
	public static final Color s_LOCATION_VIEWER_LOC_COMP_FILL_SELECTED = Color.GREEN;
	public static final Color s_LOCATION_VIEWER_LOC_COMP_BORDER_SELECTED = Color.GREEN;
	public static final Color s_LOCATION_VIEWER_LOC_COMP_FILL_INACTIVE = Color.GRAY;
	public static final Color s_LOCATION_VIEWER_LOC_COMP_BORDER_INACTIVE = Color.BLACK;
	public static final Color s_LOCATION_VIEWER_LOC_COMP_FILL_HIGHLIGHT = Color.BLUE;
	public static final Color s_LOCATION_VIEWER_LOC_COMP_BORDER_HIGHLIGHT = Color.BLACK;
	public static final Color s_LOCATION_VIEWER_CHANNEL_ARROW_DEFAULT = Color.BLACK;
	public static final Color s_LOCATION_VIEWER_CHANNEL_ARROW_SELECTED = Color.GREEN;
	public static final Color s_LOCATION_VIEWER_CHANNEL_ARROW_HIGHLIGHT = Color.BLUE;
	
	// Channel editor
	public static final double s_CHANNEL_EDITOR_SIZE_VERT_TOP = 0.3;
	public static final double s_CHANNEL_EDITOR_SIZE_HORI = 0.8;
	public static final int s_CHANNEL_EDITOR_FLOW_LAYOUT_GAP_H = 0;
	public static final int s_CHANNEL_EDITOR_FLOW_LAYOUT_GAP_V = 5;
	public static final double s_CHANNEL_EDITOR_MAX_HOP_LEN_DEFAULT = 2;
	public static final int s_CHANNEL_EDITOR_MAX_HOP_LEN_COLS = 10;
	public static final int s_CHANNEL_EDITOR_INTENSITY_COLS = 10;
	public static final boolean s_CHANNEL_EDITOR_PROP_RATE = true;
	public static final String s_CHANNEL_EDITOR_INTENSITY_DEFAULT = "1";
	public static final Color s_LOCATION_VIEWER_CHANNEL_COMP_BORDER_DEFAULT = Color.BLACK;
	public static final Color s_LOCATION_VIEWER_CHANNEL_COMP_FILL_DEFAULT = Color.BLACK;
	public static final Color s_LOCATION_VIEWER_CHANNEL_COMP_BORDER_SELECTED = Color.GREEN;
	public static final Color s_LOCATION_VIEWER_CHANNEL_COMP_FILL_SELECTED = Color.GREEN;
	public static final int s_CHANNEL_EDITOR_GENERATOR_BAR_OPTIONS_HEIGHT = 60;
	
	// Eval editor
	public static final double s_MASSPA_EVAL_EDITOR_DEF_SIZE_VERT = 0.7;
}
