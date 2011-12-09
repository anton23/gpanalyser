package uk.ac.imperial.doc.masspa.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import uk.ac.imperial.doc.masspa.gui.components.JConsoleTextPane;
import uk.ac.imperial.doc.masspa.gui.editors.AgentEditor;
import uk.ac.imperial.doc.masspa.gui.editors.ChannelEditor;
import uk.ac.imperial.doc.masspa.gui.editors.LocationEditor;
import uk.ac.imperial.doc.masspa.gui.editors.MASSPAEvalEditor;
import uk.ac.imperial.doc.masspa.gui.menus.IFileMenuHandler;
import uk.ac.imperial.doc.masspa.gui.menus.JEditMenu;
import uk.ac.imperial.doc.masspa.gui.menus.JFileMenu;
import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.models.ObservableDocument;
import uk.ac.imperial.doc.masspa.gui.models.ObservableTopology;
import uk.ac.imperial.doc.masspa.gui.models.VisualMASSPAModel;
import uk.ac.imperial.doc.masspa.gui.util.CompoundUndoManager;
import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.gui.util.KeyStateManager;
import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JMenuBar;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.strategy.Strategy;

/**
 * This class specifies the main window of
 * the MASSPA editor.
 * 
 * @author Chris Guenther
 */
public class MASSPAEditorMain extends JFrame implements IFileMenuHandler
{
	private static final long serialVersionUID = 5895779944800938392L;

	// Console components
	private final JPanel				m_consolePane;
	private final JLabel 				m_consoleLabel;
	private final JConsoleTextPane  	m_consoleText;
	private final JScrollPane			m_consoleScroll;
	
	// Menu
	private final JFileMenu				m_mnFile;
	private final JEditMenu				m_mnEdit;
	private final JMenuBar 				m_menuBar;

	// Editors
	private final AgentEditor			m_agentEditor;
	private final LocationEditor		m_locationEditor;
	private final ChannelEditor 		m_channelEditor;
	private final MASSPAEvalEditor 		m_evalEditor;
	
	// Main frame
	private final JPanel				m_mainPane;
	private final JTabbedPane			m_editorTabs;
	private final JSplitPane			m_vertSplit;

	// The models
	private final ObservableDocument		m_agentDefDoc;
	private final ObservableAgents			m_agents;
	private final ObservableTopology		m_topology;
	private final ObservableDocument		m_generatedMASSPADoc;
	private final ObservableDocument		m_evaluationDefDoc;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					// Initialise
					MASSPAEditorMain frame = new MASSPAEditorMain();
					frame.setVisible(true);

					// After startup redirect STDERR to GUI console
					System.setErr((new MASSPALogging()).createErrorStream());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 */
	public MASSPAEditorMain()
	{
		// Create models
		m_agentDefDoc = new ObservableDocument();
		m_agents = new ObservableAgents(null);
		m_topology = new ObservableTopology(null);
		m_generatedMASSPADoc = new ObservableDocument();
		m_evaluationDefDoc = new ObservableDocument();
				
		// Window setup
		setTitle(String.format(Labels.s_WINDOW_NAME_FILE,Labels.s_NEW_MODEL));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(Constants.s_MAIN_LEFT, Constants.s_MAIN_TOP, Constants.s_MAIN_RIGHT, Constants.s_MAIN_BOTTOM);
		
		// Editors
		m_agentEditor = new AgentEditor(m_agentDefDoc,m_agents);
		m_locationEditor = new LocationEditor(m_agents,m_topology);
		m_channelEditor = new ChannelEditor(m_agents,m_topology);
		m_evalEditor = new MASSPAEvalEditor(m_agentDefDoc,m_agents,m_topology,m_generatedMASSPADoc,m_evaluationDefDoc);
			
		//Tabs
		m_editorTabs = new JTabbedPane(JTabbedPane.TOP);
		m_editorTabs.add(Labels.s_TAB_AGENT_EDITOR_CAPTON, m_agentEditor);
		m_editorTabs.add(Labels.s_TAB_LOCATION_EDITOR_CAPTION, m_locationEditor);
		m_editorTabs.add(Labels.s_TAB_CHANNEL_EDITOR_CAPTION, m_channelEditor);
		m_editorTabs.add(Labels.s_TAB_MASSPA_EVAL_EDITOR_CAPTION, m_evalEditor);
		
		// Main menu
		m_mnFile = new JFileMenu(this, m_editorTabs, m_agentEditor.getTabController());	
		m_mnEdit = new JEditMenu(this, m_editorTabs, m_agentEditor.getTabController());
		m_menuBar = new JMenuBar();
		m_menuBar.add(m_mnFile);
		m_menuBar.add(m_mnEdit);
		
		// Create Console
		m_consolePane = new JPanel(new BorderLayout());
		m_consoleLabel = new JLabel(Labels.s_CONSOLE_LABEL);
		m_consoleText = new JConsoleTextPane();
		m_consoleScroll = new JScrollPane(m_consoleText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		m_consolePane.add(m_consoleLabel, BorderLayout.NORTH);
		m_consolePane.add(m_consoleScroll);
		
		// Setup main panel
		m_vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, m_editorTabs, m_consolePane);
		m_vertSplit.setResizeWeight(Constants.s_MAIN_EDITORS_SIZE_VERT);
		m_vertSplit.setEnabled(false);
		m_mainPane = new JPanel(new BorderLayout());
		m_mainPane.setBorder(new EmptyBorder(Constants.s_MAIN_BORDER, Constants.s_MAIN_BORDER, Constants.s_MAIN_BORDER, Constants.s_MAIN_BORDER));
		m_mainPane.add(m_menuBar, BorderLayout.NORTH);
		m_mainPane.add(m_vertSplit);
		setContentPane(m_mainPane);
		
		// Setup the global key listener
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(KeyStateManager.getKeyStateManager());
	}
	
	//**************************************************
	// Implementation of IFileMenuHandler
	//**************************************************
	@Override
	public void newModel()
	{
		VisualMASSPAModel model = new VisualMASSPAModel(new ObservableDocument(),new ObservableTopology(null),new ObservableDocument(),new ObservableDocument());
		model.copyIntoObservableModels(m_agentDefDoc, m_agents, m_topology, m_generatedMASSPADoc, m_evaluationDefDoc);
		CompoundUndoManager.resetAllUndoManagers();
		setTitle(String.format(Labels.s_WINDOW_NAME_FILE,Labels.s_NEW_MODEL));
	}
	
	@Override
	public boolean load(File _file)
	{
		if (_file == null) {return false;}
		try
		{
			Strategy strategy = new CycleStrategy("id", "ref");
			Serializer serializer = new Persister(strategy);
			VisualMASSPAModel model = serializer.read(VisualMASSPAModel.class, _file);
			model.copyIntoObservableModels(m_agentDefDoc, m_agents, m_topology, m_generatedMASSPADoc, m_evaluationDefDoc);
			CompoundUndoManager.resetAllUndoManagers();
			setTitle(String.format(Labels.s_WINDOW_NAME_FILE,_file));
			MASSPALogging.ok(String.format(Messages.s_FILE_LOADED,_file.getAbsolutePath()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			MASSPALogging.clearConsoles();
			MASSPALogging.fatalError(String.format(Messages.s_FAILED_TO_LOAD_MODEL, _file.getAbsolutePath()));
			return false;
		}
		return true;
	}
	
	@Override
	public boolean save(File _file)
	{
		if (_file == null) {return false;}
		try
		{
			_file = new File(_file.getAbsolutePath());
			Strategy strategy = new CycleStrategy("id", "ref");
			Serializer serializer = new Persister(strategy);
			VisualMASSPAModel model = new VisualMASSPAModel(m_agentDefDoc, m_topology, m_generatedMASSPADoc, m_evaluationDefDoc);
			serializer.write(model, _file);
			setTitle(String.format(Labels.s_WINDOW_NAME_FILE,_file));
			MASSPALogging.ok(String.format(Messages.s_FILE_SAVED,_file.getAbsolutePath()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			MASSPALogging.clearConsoles();
			MASSPALogging.fatalError(String.format(Messages.s_FAILED_TO_SAVE_MODEL, _file.getAbsolutePath()));
			return false;
		}
		return true;
	}

	@Override
	public void exit()
	{
		System.exit(0);
	}
}
