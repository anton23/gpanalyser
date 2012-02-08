package uk.ac.imperial.doc.gpanalyser.probes.testing.parser;

import com.google.common.collect.Lists;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.junit.Test;
import uk.ac.imperial.doc.gpa.fsm.ITransition;
import uk.ac.imperial.doc.gpa.fsm.Transition;
import uk.ac.imperial.doc.gpa.syntax.GPACompiler;
import uk.ac.imperial.doc.gpa.syntax.GPALexer;
import uk.ac.imperial.doc.gpa.syntax.GPAParser;
import uk.ac.imperial.doc.gpepa.representation.components.*;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class TestProbesCreation
{
    private final static String probeInputsPath = "test-probes/inputs/probes/";
    private Set<ITransition> allActions, alphabet;

    public TestProbesCreation ()
    {
        alphabet = new HashSet<ITransition> ();
        allActions = new HashSet<ITransition> ();
        allActions.add (new Transition ("fetch"));
        allActions.add (new Transition ("fail"));
        allActions.add (new Transition ("recover"));
        allActions.add (new Transition ("begin"));
        allActions.add (new Transition ("end"));
    }

    private String readFileContents(String fileName) throws IOException
    {
        FileInputStream stream = new FileInputStream (new File (fileName));
        try
        {
            FileChannel fc = stream.getChannel ();
            MappedByteBuffer bb = fc.map
                (FileChannel.MapMode.READ_ONLY, 0, fc.size ());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset ().decode (bb).toString ();
        }
        finally
        {
            stream.close ();
        }
    }

    private GPAParser getParser (TokenStream tokens)
    {
        GPAParser parser = new GPAParser (tokens);
        parser.setErrorReporter (new ErrorReporter());
        return parser;
    }

    private Map<String, PEPAComponent> parseProbe (String probeFileName)
    {
        Map<String, PEPAComponent> parsedModel = null;
        try
        {
            String probeFile
                = readFileContents (probeInputsPath + probeFileName);
            GPALexer lex = new GPALexer (new ANTLRStringStream (probeFile));
            CommonTokenStream tokens = new CommonTokenStream (lex);
            GPAParser parser = getParser (tokens);
            GPAParser.probel_return probel = parser.probel ();
            CommonTreeNodeStream nodes
                = new CommonTreeNodeStream (probel.getTree ());
            GPACompiler compiler = new GPACompiler (nodes);
            GPACompiler.probel_return probel_return
                = compiler.probel ("LProbe", allActions, alphabet,
                    false, getParser (null));
            parsedModel = probel_return.probeComponents;
        }
        catch (RecognitionException e)
        {
            e.printStackTrace ();
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }
        return parsedModel;
    }

    @Test
    public void testSimpleProbe ()
    {
        Map<String, PEPAComponent> probeGiven = parseProbe ("simple");
        Map<String, PEPAComponent> probeExpected
            = new HashMap<String, PEPAComponent> ();

        probeExpected.put ("LProbe", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("end", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix("end", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new ImmediatePrefix("begin",
                            DoubleExpression.ONE, new ComponentId ("LProbe2"))
        )));

        probeExpected.put ("LProbe2", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("end", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe3", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new ImmediatePrefix ("end",
                            DoubleExpression.ONE, new ComponentId ("LProbe4")),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                             new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe4", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                    new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                    new PassivePrefix ("end", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                    new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                    new ArrayList<ImmediatePrefix> ())
        )));

        assertEquals (probeGiven, probeExpected);
    }

    @Test
    public void testSequenceProbe ()
    {
        Map<String, PEPAComponent> probeGiven = parseProbe ("sequence");
        Map<String, PEPAComponent> probeExpected
            = new HashMap<String, PEPAComponent> ();

        probeExpected.put ("LProbe", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe2", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe3", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new ImmediatePrefix ("begin",
                            DoubleExpression.ONE, new ComponentId ("LProbe4"))
        )));

        probeExpected.put ("LProbe4", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        assertEquals (probeGiven, probeExpected);
    }

    @Test
    public void testBothProbe ()
    {
        Map<String, PEPAComponent> probeGiven = parseProbe ("both");
        Map<String, PEPAComponent> probeExpected
            = new HashMap<String, PEPAComponent> ();

        probeExpected.put ("LProbe", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe2", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe3", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new ImmediatePrefix ("begin",
                            DoubleExpression.ONE, new ComponentId ("LProbe4"))
        )));

        probeExpected.put ("LProbe4", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        assertEquals (probeGiven, probeExpected);
    }

    @Test
    public void testChoiceProbe ()
    {
        Map<String, PEPAComponent> probeGiven = parseProbe ("choice");
        Map<String, PEPAComponent> probeExpected
            = new HashMap<String, PEPAComponent> ();

        probeExpected.put ("LProbe", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new ImmediatePrefix ("begin",
                            DoubleExpression.ONE, new ComponentId ("LProbe2"))
        )));

        probeExpected.put ("LProbe2", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        assertEquals (probeGiven, probeExpected);
    }

    @Test
    public void testIteratingProbe ()
    {
        Map<String, PEPAComponent> probeGiven = parseProbe ("iterate");
        Map<String, PEPAComponent> probeExpected
            = new HashMap<String, PEPAComponent> ();

        probeExpected.put ("LProbe", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe2", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe3", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe4", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe4", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new ImmediatePrefix ("begin",
                            DoubleExpression.ONE, new ComponentId ("LProbe5"))
        )));

        probeExpected.put ("LProbe5", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe5"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe5"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe5"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        assertEquals (probeGiven, probeExpected);
    }

    @Test
    public void testZeroOneProbe ()
    {
        Map<String, PEPAComponent> probeGiven = parseProbe ("zeroone");
        Map<String, PEPAComponent> probeExpected
            = new HashMap<String, PEPAComponent> ();

        probeExpected.put ("LProbe", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe2", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new ImmediatePrefix ("begin",
                            DoubleExpression.ONE, new ComponentId ("LProbe3"))
        )));

        probeExpected.put ("LProbe3", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        assertEquals (probeGiven, probeExpected);
    }

    @Test
    public void testZeroMoreProbe ()
    {
        Map<String, PEPAComponent> probeGiven = parseProbe ("zeromore");
        Map<String, PEPAComponent> probeExpected
            = new HashMap<String, PEPAComponent> ();

        probeExpected.put ("LProbe", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new ImmediatePrefix ("begin",
                            DoubleExpression.ONE, new ComponentId ("LProbe2"))
        )));

        probeExpected.put ("LProbe2", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                             new ArrayList<ImmediatePrefix> ())
        )));

        assertEquals (probeGiven, probeExpected);
    }

    @Test
    public void testOneMoreProbe ()
    {
        Map<String, PEPAComponent> probeGiven = parseProbe ("onemore");
        Map<String, PEPAComponent> probeExpected
            = new HashMap<String, PEPAComponent> ();

        probeExpected.put ("LProbe", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe2", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new ImmediatePrefix ("begin",
                            DoubleExpression.ONE, new ComponentId ("LProbe3"))
        )));

        probeExpected.put ("LProbe3", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        assertEquals (probeGiven, probeExpected);
    }

    @Test
    public void testAnyProbe ()
    {
        Map<String, PEPAComponent> probeGiven = parseProbe ("any");
        Map<String, PEPAComponent> probeExpected
            = new HashMap<String, PEPAComponent> ();

        probeExpected.put ("LProbe", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("end", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                             new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("end", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                             new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe2", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("end", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                             new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe3", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("end", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new ImmediatePrefix ("begin",
                            DoubleExpression.ONE, new ComponentId ("LProbe4"))
        )));

        probeExpected.put ("LProbe4", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("end", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                             new ArrayList<ImmediatePrefix> ())
        )));

        assertEquals (probeGiven, probeExpected);
    }

    @Test
    public void testEventualProbe ()
    {
        Map<String, PEPAComponent> probeGiven = parseProbe ("eventual");
        Map<String, PEPAComponent> probeExpected
            = new HashMap<String, PEPAComponent> ();

        probeExpected.put ("LProbe", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe"),
                             new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                             new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe2", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                             new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe3", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe3"),
                            new ArrayList<ImmediatePrefix> ()),
                     new ImmediatePrefix ("begin",
                            DoubleExpression.ONE, new ComponentId ("LProbe4"))
        )));

        probeExpected.put ("LProbe4", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe4"),
                             new ArrayList<ImmediatePrefix> ())
        )));

        assertEquals (probeGiven, probeExpected);
    }

    @Test
    public void testEmptyProbe()
    {
        Map<String, PEPAComponent> probeGiven = parseProbe ("empty");
        Map<String, PEPAComponent> probeExpected
            = new HashMap<String, PEPAComponent> ();

        probeExpected.put ("LProbe", new Choice
                (Lists.<AbstractPrefix>newArrayList
                     (new ImmediatePrefix ("begin",
                            DoubleExpression.ONE, new ComponentId ("LProbe1"))
        )));

        probeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ())
        )));

        assertEquals (probeGiven, probeExpected);
    }

    @Test
    public void testNotProbe()
    {
        Map<String, PEPAComponent> probeGiven = parseProbe ("not");
        Map<String, PEPAComponent> probeExpected
            = new HashMap<String, PEPAComponent> ();

        probeExpected.put ("LProbe", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("end", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                             new ArrayList<ImmediatePrefix> ())
        )));

        probeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("end", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe1"),
                            new ArrayList<ImmediatePrefix> ()),
                     new ImmediatePrefix ("begin",
                            DoubleExpression.ONE, new ComponentId ("LProbe2"))
        )));

        probeExpected.put ("LProbe2", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fetch", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("fail", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("recover", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("end", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                            new ArrayList<ImmediatePrefix> ()),
                     new PassivePrefix ("begin", null,
                            DoubleExpression.ONE, new ComponentId ("LProbe2"),
                             new ArrayList<ImmediatePrefix> ())
        )));

        assertEquals (probeGiven, probeExpected);
    }
}
