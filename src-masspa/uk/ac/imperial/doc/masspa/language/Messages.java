package uk.ac.imperial.doc.masspa.language;

/***
 * This class defines all MASSPA compiler messages.
 * 
 * @author Chris Guenther
 */
public class Messages
{	
	// Compiler
	public static final String s_COMPILER_KEYWORD_MULTISERVER_SYNC = "multiServerSync";
	public static final String s_COMPILER_KEYWORD_MASSACTION_ASYNC = "massActionAsync";
	public static final String s_COMPILER_CONST_INVALID_LOCATION = "Constant '%s' has an invalid location '%s'.";
	public static final String s_COMPILER_CONST_UNDEFINED = "Constant '%s' is undefined.";
	public static final String s_COMPILER_VAR_INVALID_LOCATION = "Variable '%s' has an invalid location '%s'.";
	public static final String s_COMPILER_VAR_UNDEFINED = "Variable '%s' is undefined.";
	public static final String s_COMPILER_FUN_UNDEFINED = "Unable to find definition for function '%s'|'%s'";
	public static final String s_COMPILER_FUN_INVALID_ARG = "Argument '%s' in function definition of function '%s' must be constant Expression";
	public static final String s_COMPILER_AGENT_DEFINITION_ANALYSIS = "Compiling MASSPA agent definitions";
	public static final String s_COMPILER_AGENT_STATE_MISSING_DEFINITION = "Missing MASSPA agent state definition for '%s'.";
	public static final String s_COMPILER_AGENT_STATE_INVALID_DEFINITION = "Invalid MASSPA agent state definition for '%s'.";
	public static final String s_COMPILER_AGENT_STATE_INVALID_DEFINITION2 = "Invalid MASSPA agent state definition '%s' = '%s' in line %d.";
	public static final String s_COMPILER_AGENT_STATE_REDEFINITION = "Redefinition of MASSPA agent state '%s' = '%s' in line %d - IGNORING REDEFINITION!!!";
	public static final String s_COMPILER_AGENT_STATE_DEFINITION_NOT_ALLOWED = "The definition '%s' = '%s' is not allowed.";
	public static final String s_COMPILER_AGENT_STATE_NAME_INVALID = "'%s' is not the name of an agent state. Check that no other component with the same name has been defined.\n Maybe you were using a placeholder name (e.g. \"stop\", \"_\").\n Naming error in line %d.";
	public static final String s_COMPILER_AGENT_STATE_UNDEFINED = "Agent state '%s' is undefined or has an invalid definition.";
	public static final String s_COMPILER_AGENT_STATE_INVALID_SCOPE = "Agent state '%s' already belongs to scope '%s' and can't be referenced or defined in scope '%s' in line %d.";
	public static final String s_COMPILER_MASSPA_COMPONENT_NULL_NAME = "Cannot instantiate MASSPAComponent with <null> name.";
	public static final String s_COMPILER_CHOICE_COMPONENT_NULL_CHOICES = "Cannot instantiate ChoiceComponent with <null> choices.";
	public static final String s_COMPILER_PREFIX_NULL_ACTION = "Cannot instantiate Prefix with <null> action name.";
	public static final String s_COMPILER_PREFIX_NULL_RATE = "Cannot instantiate Prefix with <null> transition rate.";
	public static final String s_COMPILER_PREFIX_NULL_CONTINUATION = "Cannot instantiate Prefix with <null> continuation.";
	public static final String s_COMPILER_MESSAGE_PREFIX_NULL_MSG = "Cannot instantiate MessagePrefix with <null> message.";
	public static final String s_COMPILER_SEND_PREFIX_NULL_NOF_MSG_SENT = "Cannot instantiate SendPrefix with <null> number of messages sent.";
	public static final String s_COMPILER_RECEIVE_PREFIX_NULL_ACC_PROB = "Cannot instantiate SendPrefix with <null> acceptance probability.";
	public static final String s_COMPILER_MASSPA_MESSAGE_NULL_MSG_NAME = "Cannot instantiate MASSPAMessage with <null> message name.";
	public static final String s_COMPILER_MODEL_DEFINITION_ANALYSIS = "Compiling MASSPA model definition";
	public static final String s_COMPILER_MODEL_NULL_COMPONENTS = "Cannot instantiate MASSPAModel with <null> component factory.";
	public static final String s_COMPILER_LOCATION_DUPLICATE_DEFINITION = "Duplicate definition of location coordinate '%s' in line %d - IGNORING REDEFINITION!!!";
	public static final String s_COMPILER_LOCATION_NULL_DEFINITION = "Cannot create location with <null> coordinates in line %d.";
	public static final String s_COMPILER_LOCATION_NULL_COORDS = "Cannot have location with <null> coordinates.";
	public static final String s_COMPILER_LOCATION_DIM_MISMATCH = "Location dimension mismatch";
	public static final String s_COMPILER_LOCATION_INVALID_ALLLOCATION_USE = "%s is invalid. Constants, Variables and Functions can only have location @(x) or @(INT(,INT)+)";
	public static final String s_COMPILER_AGENTPOP_INVALID = "Agent population '%s' refers to an undefined agent state or location.";
	public static final String s_COMPILER_AGENTPOP_DUPLICATE_DISTRIBUTION_DEFINITION = "Duplicate definition of initial distribution for agent population '%s' in line %d - IGNORING REDEFINITION!!!";	
	public static final String s_COMPILER_AGENTPOP_INVALID_DEFINITION = "Invalid agent population definition %s%s in line %d.";
	public static final String s_COMPILER_AGENTPOP_MISSING_COMPONENT_DEFINITION = "Missing component definition for '%s' in agent population definition '%s%s' in line %d.";
	public static final String s_COMPILER_AGENTPOP_MISSING_LOCATION_DEFINITION = "Missing location definition for '%s' in agent population definition '%s%s' in line %d.";
	public static final String s_COMPILER_AGENTPOP_UNKNOWN = "Agent population '%s' referenced in line %d is unknown.";
	public static final String s_COMPILER_AGENTPOP_NULL_COMPONENT = "Cannot instantiate agent population with <null> component.";
	public static final String s_COMPILER_AGENTPOP_NULL_LOCATION = "Cannot instantiate agent population with <null> location.";
	public static final String s_COMPILER_AGENTPOP_VARLOCATION_INIT_DISTRIBUTION = "Cannot define initial distribution agent population '%s' in line %d - IGNORING DEFINITION.\nHint: Check if you are trying to assign an initial value to a variable location.";
	public static final String s_COMPILER_ACTIONCOUNT_VARALLLOCATION_INITVAL = "Cannot define initial value for action count '%s' in line %d - IGNORING DEFINITION.\nHint: Check if you are trying to assign an initial value to a variable/global location.";
	public static final String s_COMPILER_ACTIONCOUNT_INVALID = "Action count '%s' refers to an undefined action or location.";
	public static final String s_COMPILER_ACTIONCOUNT_INITVAL_DEFINITION_FAILED = "Failed to set initial value of action count '%s' in line %d.";	;
	public static final String s_COMPILER_ACTIONCOUNT_NULL_NAME = "Cannot instantiate action count with <null> name.";
	public static final String s_COMPILER_ACTIONCOUNT_NULL_INITVAL = "Cannot assign initial value of action count '%s' to <null>.";
	public static final String s_COMPILER_ACTIONCOUNT_MISSING_LOCATION_DEFINITION = "Missing location definition for %s in action count '%s%s' in line %d.";
	public static final String s_COMPILER_ACTIONCOUNT_MISSING_ACTION = "Action '%s' used in the action count initial value assignment '%s%s' in line %d never occurs.";
	public static final String s_COMPILER_ACTIONCOUNT_INVALID_DEFINITION = "Invalid action count initial value assignment definition '%s%s' in line %d.";
	public static final String s_COMPILER_INVALID_CHANNELTYPE = "'%s' in line %d is an invalid channeltype. Try '"+s_COMPILER_KEYWORD_MULTISERVER_SYNC+"' or '"+s_COMPILER_KEYWORD_MASSACTION_ASYNC+"' instead.";
	public static final String s_COMPILER_CHANNEL_DUPLICATE_DEFINTION = "Duplicate definition of channel '%s' in line %d - IGNORING REDEFINITION!!!";
	public static final String s_COMPILER_CHANNEL_NULL_SENDER = "Cannot instantiate channel with <null> sender.";
	public static final String s_COMPILER_CHANNEL_NULL_RECEIVER = "Cannot instantiate channel with <null> receiver.";
	public static final String s_COMPILER_CHANNEL_NULL_MESSAGE = "Cannot instantiate channel with <null> message.";
	public static final String s_COMPILER_CHANNEL_NULL_INTENSITY = "Cannot instantiate channel with <null> intensity.";
	public static final String s_COMPILER_CHANNEL_MESSAGE_UNKNOWN = "Message '%s' used in channel from '%s' to '%s' defined in line %d is neither sent or received by any agent - IGNORING Channel definition.";
	public static final String s_COMPILER_MOVEMENT_DUPLICATE_DEFINTION = "Duplicate definition of movement %s in line %d - IGNORING REDEFINITION!!!";
	public static final String s_COMPILER_MOVEMENT_NULL_LEAVE_ACTION = "Cannot instantiate movement with <null> leave action.";
	public static final String s_COMPILER_MOVEMENT_NULL_FROM = "Cannot instantiate movement with <null> <from>-population.";
	public static final String s_COMPILER_MOVEMENT_NULL_ENTER_ACTION = "Cannot instantiate movement with <null> enter action.";
	public static final String s_COMPILER_MOVEMENT_NULL_TO = "Cannot instantiate movement with <null> <to>-population.";
	public static final String s_COMPILER_MOVEMENT_NULL_RATE = "Cannot instantiate movement with <null> rate.";
	public static final String s_COMPILER_BIRTH_DUPLICATE_DEFINTION = "Duplicate definition of birth process '%s' in line %d - IGNORING REDEFINITION!!!";
	public static final String s_COMPILER_BIRTH_NULL_ACTION = "Cannot instantiate birth process with <null> action.";
	public static final String s_COMPILER_BIRTH_NULL_POP = "Cannot instantiate birth process with <null> population.";
	public static final String s_COMPILER_BIRTH_NULL_RATE = "Cannot instantiate birth process with <null> rate.";
	public static final String s_COMPILER_MESSAGES_HAVE_NO_RECEIVER = "The following messages are sent but have no matching receiver '%s'.";
	public static final String s_COMPILER_MESSAGES_HAVE_NO_SENDER = "The following messages can be received by agents but have no matching sender %s.";
	public static final String s_COMPILER_MASSPA_MATCHER_INCOMPATIBLE_WITH_PCTMC = "MASSPA matcher not compatible with the PCTMC!";
	public static final String s_COMPILER_MASSPA_EVO_EVT_NULL_INCREASING = "Cannot create MASSPAEvolutionEvent with <null> increasing list.";
	public static final String s_COMPILER_MASSPA_EVO_EVT_NULL_DECREASING = "Cannot create MASSPAEvolutionEvent with <null> decreasing list.";
	public static final String s_COMPILER_MASSPA_EVO_EVT_NULL_RATE = "Cannot create MASSPAEvolutionEvent with <null> rate.";
	public static final String s_COMPILER_MASSPA_PCTMC_NULL_EVO_EVTS  = "Cannot create MASSPA PCTMC with <null> evolution event list.";
	public static final String s_COMPILER_INTERNAL_UNSUPPORTED_EXPR_VISIT = "Unsupported visit to expression '%s'.";
	
	// Parser
	public static final String s_PARSER_MODEL_DEFINITION_REQUIRED = "Model definition missing or incorrect.";
	public static final String s_PARSER_AGENT_DEFINITION_REQUIRED = "At least one agent definition is required.";
	public static final String s_PARSER_INVALID_AGENT_SCOPE = "Invalid definition of agent scope '%s'";
	public static final String s_PARSER_INVALID_AGENT_MISSING_LBRACE = "Invalid definition of agent '%s' - missing '{'";
	public static final String s_PARSER_INVALID_AGENT_MISSING_RBRACE = "Invalid definition of agent '%s' - missing '}'";
	public static final String s_PARSER_INVALID_AGENT_MISSING_SEMI = "Invalid definition of agent '%s' - missing ';'";
	public static final String s_PARSER_MISSING_AGENT_STATE_DEFINITION = "Define at least one state for agent '%s'. Hint: Make sure agent state names start with upper-case letters.";
	public static final String s_PARSER_INVALID_AGENT_STATE_DEFINITION = "Invalid definition of agent state '%s' - ';'";
	public static final String s_PARSER_INVALID_AGENT_STATE_MISSING_SEMI = "Invalid definition of agent state '%s' - missing ';'";
	public static final String s_PARSER_CHOICE_INVALID_PREFIX = "Require proper prefix, e.g. (a, ra).C";
	public static final String s_PARSER_INVALID_TAU_PREFIX_DEFINITION = "Silent prefix must have form (RateExpr).C";
	public static final String s_PARSER_INVALID_PREFIX_DEFINITION = "prefix must have form (actionName, RateExpr).C";
	public static final String s_PARSER_INVALID_TAU_SEND_PREFIX_DEFINITION = "Silent Send prefix must have form !(RateExpr,MsgType,#MsgsSentExpr).C";
	public static final String s_PARSER_INVALID_SEND_PREFIX_DEFINITION = "Send prefix must have form !(actionName, RateExpr,MsgType,#MsgsSentExpr).C";
	public static final String s_PARSER_INVALID_TAU_RECV_PREFIX_DEFINITION = "Silent Receive prefix must have form ?(MsgType,MsgAcceptProbability).C";
	public static final String s_PARSER_INVALID_RECV_PREFIX_DEFINITION = "Receive prefix must have form ?(actionName,MsgType,MsgAcceptProbability).C";
	public static final String s_PARSER_MISSING_LOCATION_DEF = "Missing location definition 'Locations = {Loc,Loc,...};'";
	public static final String s_PARSER_INVALID_LOCATION_IN_CONST_NAME = "Constants names must have locations of type '@(x)' or '@(INT(,INT)+)'";
	public static final String s_PARSER_INVALID_LOCATION_IN_VAR_NAME = "Variable names must have locations of type '@(x)' or '@(INT(,INT)+)'";
	public static final String s_PARSER_INVALID_LOCATION_DEF_MISSING_LBRACE = "Invalid definition of locations - missing '{'";
	public static final String s_PARSER_INVALID_LOCATION_DEF_INVALID_LOC = "Invalid location in locations definition - location list must be (INT(,INT)+),(INT(,INT)+),...";
	public static final String s_PARSER_INVALID_LOCATION_DEF_MISSING_RBRACE = "Invalid definition of locations - missing '}'";
	public static final String s_PARSER_INVALID_LOCATION_DEF_MISSING_SEMI = "Invalid definition of locations - missing ';'";
	public static final String s_PARSER_MISSING_INITIAL_VALUES = "Missing initial agent population/action count definitions.";
	public static final String s_PARSER_INVALID_AGENT_POPULATION_LOCATION = "Agent population '%s' must have location of type '@(INT(,INT)+)";
	public static final String s_PARSER_INVALID_ACTION_COUNT_LOCATION = "Action count '%s' must have location of type '@(A)' or '@(INT(,INT)+)";
	public static final String s_PARSER_INVALID_AGENT_POPULATION_DEF = "Agent population definition must have form 'agentPop = Expr;'";
	public static final String s_PARSER_INVALID_ACTION_COUNT_DEF = "Action count definition must have form '#actionCount = Expr;'";
	public static final String s_PARSER_CHANNELTYPE_MISSING_LBRACK  = "Invalid channeltype statement - missing '['";
	public static final String s_PARSER_CHANNELTYPE_INCORRECT_TYPE  = "Invalid channeltype statement - invalid type. Try '"+s_COMPILER_KEYWORD_MULTISERVER_SYNC+"' or '"+s_COMPILER_KEYWORD_MASSACTION_ASYNC + "'.";
	public static final String s_PARSER_CHANNELTYPE_MISSING_RBRACK  = "Invalid channeltype statement - missing ']'";
	public static final String s_PARSER_CHANNELTYPE_MISSING_SEMI  = "Invalid channeltype statement - missing ';'";
	public static final String s_PARSER_CHANNEL_MISSING_LPAR = "Invalid channel definition - missing '('";
	public static final String s_PARSER_CHANNEL_INVALID_SENDER = "Invalid channel definition - try (SENDERAGENTPOP,receiverAgentPop,msgName).";
	public static final String s_PARSER_CHANNEL_INVALID_RECEIVER = "Invalid channel definition - try (senderAgentPop,RECEIVERAGENTPOP,msgName).";
	public static final String s_PARSER_CHANNEL_INVALID_MSG = "Invalid channel definition - try (senderAgentPop,receiverAgentPop,MSGNAME).";
	public static final String s_PARSER_CHANNEL_MISSING_RPAR = "Invalid channel definition - missing ')'";
	public static final String s_PARSER_CHANNEL_MISSING_EXPR = "Invalid channel definition - missing intensity expression.";
	public static final String s_PARSER_CHANNEL_MISSING_SEMI = "Invalid channel definition - missing ';'";
	
	// Console
	public static final String s_WARNING = "WARNING: %s";
	public static final String s_NUM_WARNINGS = "#WARNINGS: %d";
	public static final String s_ERROR = "ERROR: %s";
	public static final String s_NUM_ERRORS = "#ERRORS: %d";
	public static final String s_FATAL_ERROR = "FATAL ERROR: %s";
	public static final String s_NUM_FATAL_ERRORS = "#FATAL ERRORS: %d";
	
	//////// GUI
	public static final String s_FAILED_TO_LOAD_MODEL = "Failed to load model from file %s.";
	public static final String s_FAILED_TO_SAVE_MODEL = "Failed to save model to file %s.";
	public static final String s_FILE_LOADED = "Successfully loaded model from file %s.";
	public static final String s_FILE_SAVED = "Successfully saved model to file %s.";
	
	// MODEL
	public static final String s_OBSERVABLE_DOCUMENT_DESERIALIZATION_FAILED = "Failed to deserialize observable document.";
	public static final String s_OBSERVABLE_DOCUMENT_SERIALIZATION_FAILED = "Failed to serialize observable document.";
	public static final String s_OBSERVABLE_DOCUMENT_COPY_ERROR = "Failed to copy a string into the document";
	public static final String s_OBSERVABLE_AGENTS_DESERIALIZATION_FAILED = "Failed to deserialize observable agents.";
	public static final String s_OBSERVABLE_AGENTS_SERIALIZATION_FAILED = "Failed to serialize observable agents.";
	public static final String s_OBSERVABLE_TOPOLOGY_DESERIALIZATION_FAILED = "Failed to deserialize observable topology.";
	public static final String s_OBSERVABLE_TOPOLOGY_SERIALIZATION_FAILED = "Failed to serialize observable topology.";
	
	// CONSOLE
	public static final String s_CONSOLE_STATS = "Console statistics:";
	
	// AGENT EDITOR TAB
	public static final String s_AGENT_EDITOR_IO_ERROR = "An I/O error occurred while reading agent definitions.";
	public static final String s_AGENT_EDITOR_COMPILER_ERROR = "Failed to compile the agent definitions";
	public static final String s_AGENT_EDITOR_COMPILER_SUCCESS = "MASSPA Agent definition(s) compiled successfully!";
	
	// LOCATION EDITOR TAB
	public static final String s_LOCATION_EDITOR_TOPOLOGY_GEN_NOT_FOUND = "Topology generator class %s does not exist.";
	public static final String s_LOCATION_EDITOR_TOPOLOGY_GEN_INSTANTIATION_FAILED = "Instantiation of topology generator class %s failed.";
	public static final String s_LOCATION_EDITOR_TOPOLOGY_GEN_ACCESS_FAILED = "Topology generator class %s has no visible constructor.";
	public static final String s_LOCATION_EDITOR_LINE = "Line %s to %s";
	public static final String s_LOCATION_EDITOR_RING = "Ring centre at %s, radius %f";
	public static final String s_LOCATION_EDITOR_MISSING_GRID_COMPONENT_VISUALIZER = "Failed to draw GridComponent of class %s.";
	public static final String s_LOCATION_EDITOR_MISSING_LOCATION_COMPONENT_VISUALIZER = "Failed to draw LocationComponent of class %s.";
	public static final String s_LOCATION_EDITOR_DISABLED_LOCATIONS = "Disabled selected locations.";
	public static final String s_LOCATION_EDITOR_CANNOT_HAVE_TWO_TYPES_IN_SELECTION = "Cannot have %s classes and %s classes in the same selection.";
	public static final String s_LOCATION_EDITOR_NULL_STATE_POPULATION = "It is not possible to define populations for a <NULL> MASSPA state!";
	public static final String s_LOCATION_EDITOR_ALL_STATE_POPULATION = "It is not possible to define populations for all states!";
	public static final String s_LOCATION_EDITOR_INVALID_POPULATION_EXPRESSION = "The expression %s is not valid.";
	
	// Channel EDITOR TAB
	public static final String s_CHANNEL_EDITOR_CHANNEL_GEN_NOT_FOUND = "Channel generator class %s does not exist.";
	public static final String s_CHANNEL_EDITOR_CHANNEL_GEN_INSTANTIATION_FAILED = "Instantiation of channel generator class %s failed.";
	public static final String s_CHANNEL_EDITOR_CHANNEL_GEN_ACCESS_FAILED = "Channel generator class %s has no visible constructor.";
	public static final String s_CHANNEL_EDITOR_INVALID_INTENSITY_EXPRESSION = "The expression %s is not valid.";
	public static final String s_CHANNEL_EDITOR_NO_TOPOLOGY = "Please create a topology before generating channels.";
	public static final String s_CHANNEL_EDITOR_NO_SINK = "Please select a sink location.";
	public static final String s_CHANNEL_EDITOR_INVALID_POPULATION_EXPRESSION = "The expression %s is not valid.";
}
