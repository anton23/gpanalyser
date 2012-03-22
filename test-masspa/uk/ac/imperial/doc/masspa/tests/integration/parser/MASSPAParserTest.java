package uk.ac.imperial.doc.masspa.tests.integration.parser;

import java.util.LinkedList;

import org.junit.Test;

import com.google.common.collect.Lists;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.testing.BaseTestPCTMCParser;

public class MASSPAParserTest extends BaseTestPCTMCParser
{
	public MASSPAParserTest()
	{
		interpreter = GPAPMain.createMASSPAInterpreter();
	}
	
	@Test
	public void testParseSimpleModel1() throws ParseException
	{				
		testReportsMoreParseErrors(
				"// Constants\n" + 
				"mu@(x)=0.1;\n" + 
				"pop@(0)=300.0;\n" + 
				"pop@(1)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"// Agent definitions\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);\n" + 
				"\n" + 
				"ChannelType[massActionAsync];\n" + 
				"Channel(On@(2),Off@(0),M) = 1/pop@(0);\n" + 
				"Channel(On@(0),Off@(1),M) = 1/pop@(1);\n" + 
				"Channel(On@(2),Off@(1),M) = 1/pop@(1);\n" + 
				"Channel(On@(1),Off@(2),M) = 1/pop@(2);"
				, new LinkedList<String>());
	}
	
	@Test
	public void testParseSimpleModel2() throws ParseException
	{				
		testReportsMoreParseErrors(
				"// Constants\n" + 
				"mu@(x)=0.1;\n" + 
				"pop@(x)=200.0;\n" + 
				"pop@(0)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"// Agent definitions\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);\n" + 
				"\n" + 
				"ChannelType[multiServerSync];\n" + 
				"Channel(On@(2),Off@(0),M) = 1/pop@(0);\n" + 
				"Channel(On@(0),Off@(1),M) = 1/pop@(1);\n" + 
				"Channel(On@(2),Off@(1),M) = 1/pop@(1);\n" + 
				"Channel(On@(1),Off@(2),M) = 1/pop@(2);"
				, new LinkedList<String>());
	}
	
	@Test
	public void testConstLocInvalid()
	{
		testReportsMoreParseErrors(
				"mu@(xasd)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);",
				Lists.newArrayList(
				"[line 1:3] no viable alternative at '(' ("+String.format(Messages.s_PARSER_INVALID_LOCATION_IN_CONST_NAME)+")",
				"[line 1:8] mismatched input ')' expecting @ ("+String.format(Messages.s_PARSER_INVALID_LOCATION_IN_CONST_NAME, "Off")+")"));
	}
	
	@Test
	public void testVarLocInvalid()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"\n" + 
				"$var@(as) = 0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);",
				"[line 4:5] no viable alternative at '(' ("+String.format(Messages.s_PARSER_INVALID_LOCATION_IN_VAR_NAME)+")");
	}
	
	@Test
	public void testNoAgentDef()
	{
		testReportsOneParseError(
			"mu@(x)=0.1;\n" + 
			"pop@(x)=300.0;\n" + 
			"pop@(2)=450.0;",
			"[line 3:14] required list did not match anything at input the end of file ("+String.format(Messages.s_PARSER_AGENT_DEFINITION_REQUIRED)+")");
	}
	
	@Test
	public void testAgentDefInvalidScope()
	{
		testReportsOneParseError(
			"mu@(x)=0.1;\n" + 
			"pop@(x)=300.0;\n" + 
			"pop@(2)=450.0;\n" + 
			"\n" + 
			"Agent aOnOff\n" + 
			"{\n" + 
			"  On = !(mu@(x),M,1).Off;\n" + 
			"  Off = ?(M,1).On + (0.00001).On;\n" + 
			"};\n" + 
			"\n" + 
			"Locations = {(0),(1),(2)};\n" + 
			"\n" + 
			"On@(0)  = pop@(0);\n" + 
			"Off@(1) = pop@(1);\n" + 
			"Off@(2) = pop@(2);",
			"[line 5:6] mismatched input 'aOnOff' expecting UPPERCASENAME ("+String.format(Messages.s_PARSER_INVALID_AGENT_SCOPE,"null")+")");
	}
	
	@Test
	public void testAgentDefMissingLBrace()
	{
		testReportsOneParseError(
			"mu@(x)=0.1;\n" + 
			"pop@(x)=300.0;\n" + 
			"pop@(2)=450.0;\n" + 
			"\n" + 
			"Agent OnOff\n" + 
			"  On = !(mu@(x),M,1).Off;\n" + 
			"  Off = ?(M,1).On + (0.00001).On;\n" + 
			"};\n" + 
			"\n" + 
			"Locations = {(0),(1),(2)};\n" + 
			"\n" + 
			"On@(0)  = pop@(0);\n" + 
			"Off@(1) = pop@(1);\n" + 
			"Off@(2) = pop@(2);",
			"[line 6:2] missing '{' at 'On' ("+String.format(Messages.s_PARSER_INVALID_AGENT_MISSING_LBRACE,"OnOff")+")");
	}
	
	@Test
	public void testAgentDefMissingRBrace()
	{
		testReportsOneParseError(
			"mu@(x)=0.1;\n" + 
			"pop@(x)=300.0;\n" + 
			"pop@(2)=450.0;\n" + 
			"\n" + 
			"Agent OnOff\n" + 
			"{\n" + 
			"  On = !(mu@(x),M,1).Off;\n" + 
			"  Off = ?(M,1).On + (0.00001).On;\n" + 
			";\n" + 
			"\n" + 
			"Locations = {(0),(1),(2)};\n" + 
			"\n" + 
			"On@(0)  = pop@(0);\n" + 
			"Off@(1) = pop@(1);\n" + 
			"Off@(2) = pop@(2);",
			"[line 9:0] missing '}' at ';' ("+String.format(Messages.s_PARSER_INVALID_AGENT_MISSING_RBRACE,"OnOff")+")");
	}
	
	@Test
	public void testAgentDefSemiMissing()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);",
				"[line 9:0] extraneous input '}' expecting ';' ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_MISSING_SEMI, "Off")+")");
	}
	
	@Test
	public void testAgentStateMissing()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  aOn = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);",
				"[line 7:2] required list did not match anything at input 'aOn' ("+String.format(Messages.s_PARSER_MISSING_AGENT_STATE_DEFINITION, "OnOff")+")");
	}
	
	@Test
	public void testAgentStateInvalid()
	{
		/*
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = A + !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);",
				"[line 7:7] no viable alternative at 'A' ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "On")+")");
	*/
	}
	
	@Test
	public void testAgentStateMissingSemi()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"}\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);",
				"[line 11:0] missing ';' at 'Locations' ("+String.format(Messages.s_PARSER_INVALID_AGENT_MISSING_SEMI, "OnOff")+")");
	}
	
	@Test
	public void testPrefixInvalid()
	{
		testReportsMoreParseErrors(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = (wer).C + !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);",Lists.newArrayList(
				"[line 7:7] no viable alternative at '(' ("+String.format(Messages.s_PARSER_CHOICE_INVALID_PREFIX)+")",
				"[line 7:15] mismatched input '+' expecting = ("+String.format(Messages.s_PARSER_MISSING_AGENT_STATE_DEFINITION, "OnOff")+")",
				"[line 7:27] mismatched input ',' expecting = ("+String.format(Messages.s_PARSER_MISSING_AGENT_STATE_DEFINITION, "OnOff")+")",
				"[line 7:34] mismatched input ';' expecting = ("+String.format(Messages.s_PARSER_MISSING_AGENT_STATE_DEFINITION, "OnOff")+")"));
	}
	
	@Test
	public void testSilentPrefixInvalid()
	{
		testReportsMoreParseErrors(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = (12,121).Off + !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);",Lists.newArrayList(
				"[line 7:10] mismatched input ',' expecting ) ("+String.format(Messages.s_PARSER_INVALID_TAU_PREFIX_DEFINITION)+")",
				"[line 7:20] mismatched input '+' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "On")+")",
				"[line 7:32] mismatched input ',' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "On")+")",
				"[line 7:39] mismatched input ';' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "On")+")"));
	}
	
	@Test
	public void testActionPrefixInvalid()
	{
		testReportsMoreParseErrors(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = (asdf,12,1).Off + !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);",Lists.newArrayList(
				"[line 7:15] mismatched input ',' expecting ) ("+String.format(Messages.s_PARSER_INVALID_PREFIX_DEFINITION)+")",
				"[line 7:23] mismatched input '+' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "On")+")",
				"[line 7:35] mismatched input ',' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "On")+")",
				"[line 7:42] mismatched input ';' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "On")+")"));
	}
	
	@Test
	public void testSilentSendPrefixInvalid()
	{
		testReportsMoreParseErrors(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(1,mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);",Lists.newArrayList(
				"[line 7:11] mismatched input 'mu' expecting UPPERCASENAME ("+String.format(Messages.s_PARSER_INVALID_TAU_SEND_PREFIX_DEFINITION)+")",
				"[line 7:19] mismatched input ',' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "On")+")",
				"[line 7:26] mismatched input ';' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "On")+")"));
	}
	
	@Test
	public void testSendPrefixInvalid()
	{
		testReportsMoreParseErrors(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(as,s,mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);",Lists.newArrayList(
				"[line 7:14] mismatched input 'mu' expecting UPPERCASENAME ("+String.format(Messages.s_PARSER_INVALID_SEND_PREFIX_DEFINITION)+")",
				"[line 7:22] mismatched input ',' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "On")+")",
				"[line 7:29] mismatched input ';' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "On")+")"));
	}
	
	@Test
	public void testSilentRecvPrefixInvalid()
	{
		testReportsMoreParseErrors(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1,a).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);",Lists.newArrayList(
				"[line 8:13] mismatched input ',' expecting ) ("+String.format(Messages.s_PARSER_INVALID_TAU_RECV_PREFIX_DEFINITION)+")",
				"[line 8:20] mismatched input '+' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "Off")+")",
				"[line 8:34] mismatched input ';' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "Off")+")"));
	}
	
	@Test
	public void testRecvPrefixInvalid()
	{
		testReportsMoreParseErrors(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(action,M,1,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);",Lists.newArrayList(
				"[line 8:20] mismatched input ',' expecting ) ("+String.format(Messages.s_PARSER_INVALID_RECV_PREFIX_DEFINITION)+")",
				"[line 8:27] mismatched input '+' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "Off")+")",
				"[line 8:41] mismatched input ';' expecting = ("+String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION, "Off")+")"));
	}
	
	@Test
	public void testLocDefMissing()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"//Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);",
				"[line 12:0] mismatched input 'On' expecting LOCS ("+String.format(Messages.s_PARSER_MISSING_LOCATION_DEF)+")");
	}
	
	@Test
	public void testLocDefMissingLBRACE()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = (0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);",
				"[line 9:12] missing '{' at '(' ("+String.format(Messages.s_PARSER_INVALID_LOCATION_DEF_MISSING_LBRACE)+")");
	}
	
	@Test
	public void testLocDefInvalidLocation()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2s)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);",
				"[line 9:23] extraneous input 's' expecting ) ("+String.format(Messages.s_PARSER_INVALID_LOCATION_DEF_INVALID_LOC)+")");
	}
	
	@Test
	public void testLocDefMissingRBRACE()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2);\n" + 
				"\n" + 
				"On@(0)  = pop@(0);",
				"[line 9:24] missing '}' at ';' ("+String.format(Messages.s_PARSER_INVALID_LOCATION_DEF_MISSING_RBRACE)+")");
	}
	
	@Test
	public void testLocDefMissingSEMI()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)}\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n",
				"[line 11:0] missing ';' at 'On' ("+String.format(Messages.s_PARSER_INVALID_LOCATION_DEF_MISSING_SEMI)+")");
	}
	
	@Test
	public void testMissingInitAgentPops() {
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};",
				"[line 9:26] required list did not match anything at input the end of file ("+String.format(Messages.s_PARSER_MISSING_INITIAL_VALUES)+")");
	}
	
	@Test
	public void testAgentPopInvalidLoc() {
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0s)  = pop@(0);",
				"[line 11:5] extraneous input 's' expecting ) ("+String.format(Messages.s_PARSER_INVALID_AGENT_POPULATION_LOCATION,"On")+")");
	}
	
	@Test
	public void testCountPopInvalidLoc() {
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(act,mu@(x),M,1).Off;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"#act@(0s)  = pop@(0);",
				"[line 11:7] extraneous input 's' expecting ) ("+String.format(Messages.s_PARSER_INVALID_ACTION_COUNT_LOCATION,"act")+")");
	}
	
	@Test
	public void testAgentPopInvalidInitPop() {
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(act,mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = %a + 12;",
				"[line 12:11] no viable alternative at 'a' ("+String.format(Messages.s_PARSER_INVALID_AGENT_POPULATION_DEF,"On")+")");
	}
	
	@Test
	public void testCountPopInvalidInitPop() {
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(act,mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"#act@(0)  = %a + 12;",
				"[line 12:13] no viable alternative at 'a' ("+String.format(Messages.s_PARSER_INVALID_ACTION_COUNT_DEF,"On")+")");
	}
	
	@Test
	public void testChannelTypeMissingLBRACK()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);\n" + 
				"\n" + 
				"ChannelType(massActionAsync];",
				"[line 17:11] mismatched input '(' expecting '[' ("+String.format(Messages.s_PARSER_CHANNELTYPE_MISSING_LBRACK)+")");
	}
	
	@Test
	public void testChannelTypeInvalid()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);\n" + 
				"\n" + 
				"ChannelType[MassActionAsync];",
				"[line 17:12] mismatched input 'MassActionAsync' expecting LOWERCASENAME ("+String.format(Messages.s_PARSER_CHANNELTYPE_INCORRECT_TYPE)+")");
	}
	
	@Test
	public void testChannelTypeMissingRBRACK()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);\n" + 
				"\n" + 
				"ChannelType[massActionAsync);",
				"[line 17:27] mismatched input ')' expecting ']' ("+String.format(Messages.s_PARSER_CHANNELTYPE_MISSING_RBRACK)+")");
	}
	
	@Test
	public void testChannelTypeMissingSEMI()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);\n" + 
				"\n" + 
				"ChannelType[massActionAsync]",
				"[line 17:28] mismatched input the end of file expecting ';' ("+String.format(Messages.s_PARSER_CHANNELTYPE_MISSING_SEMI)+")");
	}
	
	@Test
	public void testChannelMissingLPAR()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);\n" + 
				"\n" + 
				"Channel[On@(2),Off@(0),M) = 1/pop@(0);",
				"[line 17:7] mismatched input '[' expecting ( ("+String.format(Messages.s_PARSER_CHANNEL_MISSING_LPAR)+")");
	}
	
	
	@Test
	public void testChannelInvalidSender()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);\n" + 
				"\n" + 
				"Channel(aOn@(2),Off@(0),M) = 1/pop@(0);",
				"[line 17:8] mismatched input 'aOn' expecting UPPERCASENAME ("+String.format(Messages.s_PARSER_CHANNEL_INVALID_SENDER)+")");
	}
	
	@Test
	public void testChannelInvalidReceiver()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);\n" + 
				"\n" + 
				"Channel(On@(2),aOff@(0),M) = 1/pop@(0);",
				"[line 17:15] mismatched input 'aOff' expecting UPPERCASENAME ("+String.format(Messages.s_PARSER_CHANNEL_INVALID_RECEIVER)+")");
	}
	
	@Test
	public void testChannelInvalidMsg()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);\n" + 
				"\n" + 
				"Channel(On@(2),Off@(0),aM) = 1/pop@(0);",
				"[line 17:23] mismatched input 'aM' expecting UPPERCASENAME ("+String.format(Messages.s_PARSER_CHANNEL_INVALID_MSG)+")");
	}
	
	@Test
	public void testChannelInvalidIntensity()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);\n" + 
				"\n" + 
				"Channel(On@(2),Off@(0),M) = % 1/pop@(0);",
				"[line 17:30] no viable alternative at '1' ("+String.format(Messages.s_PARSER_CHANNEL_MISSING_EXPR)+")");
	}

	@Test
	public void testChannelMissingRPAR()
	{
		testReportsOneParseError(
				"mu@(x)=0.1;\n" + 
				"pop@(x)=300.0;\n" + 
				"pop@(2)=450.0;\n" + 
				"\n" + 
				"Agent OnOff\n" + 
				"{\n" + 
				"  On = !(mu@(x),M,1).Off;\n" + 
				"  Off = ?(M,1).On + (0.00001).On;\n" + 
				"};\n" + 
				"\n" + 
				"Locations = {(0),(1),(2)};\n" + 
				"\n" + 
				"On@(0)  = pop@(0);\n" + 
				"Off@(1) = pop@(1);\n" + 
				"Off@(2) = pop@(2);\n" + 
				"\n" + 
				"Channel(On@(2),Off@(0),M] = 1/pop@(0);",
				"[line 17:24] mismatched input ']' expecting ) ("+String.format(Messages.s_PARSER_CHANNEL_MISSING_RPAR)+")");
	}
}
