package uk.ac.imperial.doc.masspa.language;

public class Labels
{
	// Other
	public static final String s_ALL = "<ALL>";
	public static final String s_ANY = "_";
	public static final String s_RECEIVING_AGENT_POP = "recvAgentPop";
	public static final String s_STOP = "stop";
	public static final String s_AGENT_TYPE = "AgentType_";
	
	// Main menu
	public static final String s_MENU_FILE = "File";
	public static final String s_MENU_FILE_NEW = "New";
	public static final String s_MENU_FILE_OPEN = "Open";
	public static final String s_MENU_FILE_SAVE = "Save";
	public static final String s_MENU_FILE_SAVE_AS = "Save As...";
	public static final String s_MENU_FILE_PRINT = "Print...";
	public static final String s_MENU_FILE_EXIT = "Exit";
	public static final String s_MENU_EDIT = "Edit";
	public static final String s_MENU_EDIT_COPY = "Copy";
	public static final String s_MENU_EDIT_CUT = "Cut";
	public static final String s_MENU_EDIT_PASTE = "Paste";
	public static final String s_MENU_EDIT_DELETE = "Delete";
	public static final String s_MENU_EDIT_UNDO = "Undo";
	public static final String s_MENU_EDIT_REDO = "Redo";
	public static final String s_MENU_EDIT_FIND = "Find";
	public static final String s_MENU_EDIT_REPLACE = "Replace";
	
	// Main window pane
	public static final String s_WINDOW_NAME = "MASSPA-Modeller";
	public static final String s_WINDOW_NAME_FILE = "MASSPA-Modeller - %s";
	public static final String s_NEW_MODEL = "New Model";
	public static final String s_TAB_AGENT_EDITOR_CAPTON = "Agents & Variables";
	public static final String s_TAB_LOCATION_EDITOR_CAPTION = "Locations";
	public static final String s_TAB_CHANNEL_EDITOR_CAPTION = "Channels";
	public static final String s_CONSOLE_LABEL = "Console";
	
	// Agent editor labels
	public static final String s_AGENT_EDITOR_DEF_LABEL = "Define your constants, variables and MASSPA agents below";
	public static final String s_AGENT_EDITOR_TREE_LABEL = "Detected Agent(s)";
	public static final String s_AGENT_EDITOR_COMPILE = "Compile agent definition(s)";
	public static final Object s_AGENT_EDITOR_AGENTS = "Agents";
	
	// Location editor labels
	public static final String s_LOCATION_EDITOR_CHOOSE_TOPOLOGY_GENERATOR = "Topology Generator:";
	public static final String s_LOCATION_EDITOR_GEN_TOPOLOGY = "Generate Topology";
	public static final String s_LOCATION_EDITOR_NOF_RINGS = "#Rings:";
	public static final String s_LOCATION_EDITOR_NOF_LOCS_PER_RING = "#Locations per ring:";
	public static final String s_LOCATION_EDITOR_LOCS_X = "#Locations X-axis:";
	public static final String s_LOCATION_EDITOR_LOCS_Y = "#Locations Y-axis:";
	public static final String s_LOCATION_EDITOR_NO_OPS = "No Options available";
	public static final String s_LOCATION_EDITOR_ZOOM = "Zoom";
	public static final String s_LOCATION_EDITOR_DISABLE_LOCS = "Disable Location(s)";
	public static final String s_LOCATION_EDITOR_DISABLE_LOCS_TIP = "Disable Location(s) selected in Topology editor.";
	public static final String s_LOCATION_EDITOR_DISABLED_LOCS = "Disabled Locations:";
	public static final String s_LOCATION_EDITOR_ENABLE_LOCS_TIP = "Enable Location(s) by deleting them from the list.";
	public static final String s_LOCATION_EDITOR_LOCATIONS  = "Locations";
	public static final String s_LOCATION_EDITOR_POPULATIONS  = "Populations";
	public static final String s_LOCATION_EDITOR_POPULATIONS_LIST_LABEL  = "Populations:";
	public static final String s_LOCATION_EDITOR_ADD_POPULATIONS = "Add/Change Populations in selected Locations";
	public static final String s_LOCATION_EDITOR_ADD_POPULATIONS_TIP = "Add Populations for Location(s) selected in Topology editor";
	public static final String s_LOCATION_EDITOR_REM_POPULATIONS_TIP = "Remove population(s) by deleting them from the list.";
	public static final String s_LOCATION_EDITOR_OPTIONS = "Options";
	public static final String s_LOCATION_EDITOR_STATE = "State:";
	public static final String s_LOCATION_EDITOR_POPULATIONS_EXPR = "Population Expression";
	public static final String s_LOCATION_EDITOR_POPULATIONS_EXPR_TIP = "Enter an agent population expression";
	public static final String s_LOCATION_EDITOR_ENABLE_DISABLE_TIP = "<html>To enable/disable locations:<br>1) Select locations. For multiselection hold &lt;CTRL&gt;.<br>2) Press &lt;e&gt;/&lt;d&gt; to enable/disable location(s)</html>"; 
	
	// Channel editor labels
	public static final String s_CHANNEL_EDITOR_GEN_CHANNELS = "Generate Channels";
	public static final String s_CHANNEL_EDITOR_SET_SINK = "Set Sink";
	public static final String s_CHANNEL_EDITOR_CHOOSE_CHANNEL_GENERATOR =  "Channel Generator:";
	public static final String s_CHANNEL_EDITOR_NO_OPS = "No Options available - Create a topology first!";
	public static final String s_CHANNEL_EDITOR_SINKS_LABEL = "Sinks:";
	public static final String s_CHANNEL_EDITOR_SET_SINK_TIP = "Set selected location as sink";
	public static final String s_CHANNEL_EDITOR_SINKS_TIP = "Chosen sink location";
	public static final String s_CHANNEL_EDITOR_SENDER_STATE_LABEL = "Sender State:";
	public static final String s_CHANNEL_EDITOR_RECEIVER_STATE_LABEL = "Receiver State:";
	public static final String s_CHANNEL_EDITOR_MESSAGE_TYPE_LABEL = "Message Type:";
	public static final String s_CHANNEL_EDITOR_MAX_HOP_LEN = "Max Hop length:";
	public static final String s_CHANNEL_EDITOR_MAX_HOP_LEN_TIP = "Locations lie on an integer grid with spacing of 1 UNIT between two grid cells.";
	public static final String s_CHANNEL_EDITOR_POPULATION_PROPORTIONAL_RATE = "Receiver Population proportional intensity";
	public static final String s_CHANNEL_EDITOR_CHANNEL_INTENSITY = "Channel intensity:";
	public static final String s_CHANNEL_EDITOR_OPTIONS = "Options";
	public static final String s_CHANNEL_EDITOR_ADD_CHANNELS = "Add channels";
	public static final String s_CHANNEL_EDITOR_TOGGLE_ADD_CHANNELS = "ADD CHANNELS";
	public static final String s_CHANNEL_EDITOR_TOGGLE_ADD_CHANNELS_TIP = "<html>When this button is enabled, you can add channels as follows:<br>"+
																		  "1) Setup channel properties in action tab<br>"+
																		  "2) Select sender state in visual editor while holding &lt;CTRL&gt;<br>" +
																		  "3) Select receiving state while still holing &lt;CTRL&gt;</html>";
	public static final String s_CHANNEL_EDITOR_EDIT_CHANNELS = "View/Edit Channels";
	public static final String s_CHANNEL_EDITOR_CHANNELS = "Channels";
	public static final String s_CHANNEL_EDITOR_EDIT_LOCS_TIP = "<html>You have the following two edit options<br>" +
																"1) Press &lt;DEL&gt; to delete channel selected in the list<br>" +
																"2) Select channels and overwrite the intensity expression";
	public static final String s_CHANNEL_EDITOR_CHANGE_CHANNEL = "Change";
	public static final String s_CHANNEL_EDITOR_CHANGE_CHANNEL_TIP = "By clicking this button all selected channels will change their intensity to the one in the text field above the list.";
	public static final String s_CHANNEL_EDITOR_DELETE_CHANNELS = "Delete channels";
	public static final String s_CHANNEL_EDITOR_DELETE_CHANNELS_TIP = "Delete channels selected in visual editor.";
	
	// MasspaEvaluator editor labels
	public static final String s_TAB_MASSPA_EVAL_EDITOR_CAPTION = "Evaluator";
	public static final String s_MASSPA_EVAL_MASSPA_DEF_LABEL = "Generated MASSPA model:";
	public static final String s_MASSPA_EVAL_GENERATE = "Generate MASSPA definition";
	public static final String s_MASSPA_EVAL_EVAL_DEF_LABEL = "Evaluation method:";
	public static final String s_MASSPA_EVAL_EVAL = "Evaluate";
}
