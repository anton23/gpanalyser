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
        Map<String, PEPAComponent> simpleProbe = parseProbe ("simple");
        Map<String, PEPAComponent> simpleProbeExpected
            = new HashMap<String, PEPAComponent> ();

        simpleProbeExpected.put ("LProbe", new Choice
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

        simpleProbeExpected.put ("LProbe1", new Choice
                (Lists.<AbstractPrefix>newArrayList
                    (new PassivePrefix ("fail", null,
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

        simpleProbeExpected.put ("LProbe2", new Choice
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

        simpleProbeExpected.put ("LProbe3", new Choice
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

        simpleProbeExpected.put ("LProbe4", new Choice
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

        assertEquals (simpleProbe, simpleProbeExpected);
    }
}
