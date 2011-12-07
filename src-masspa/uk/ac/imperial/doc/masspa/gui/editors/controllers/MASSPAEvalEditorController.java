package uk.ac.imperial.doc.masspa.gui.editors.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.google.common.collect.Multimap;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITabController;
import uk.ac.imperial.doc.masspa.gui.menus.ICopyPasteDeleteHandler;
import uk.ac.imperial.doc.masspa.gui.menus.IFindReplaceHandler;
import uk.ac.imperial.doc.masspa.gui.menus.IUndoRedoHandler;
import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.models.ObservableDocument;
import uk.ac.imperial.doc.masspa.gui.models.ObservableTopology;
import uk.ac.imperial.doc.masspa.gui.models.channels.ChannelComponent;
import uk.ac.imperial.doc.masspa.gui.models.channels.Channels;
import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;
import uk.ac.imperial.doc.masspa.gui.models.topologies.Topology;
import uk.ac.imperial.doc.masspa.gui.util.CompoundUndoManager;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.representation.components.AllComponent;
import uk.ac.imperial.doc.masspa.representation.components.AllMessage;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAAgents;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;
import uk.ac.imperial.doc.masspa.representation.model.Location;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAAgentPop;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAChannel;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;

/**
 * This class controls the MASSPAEvaluator tab.
 * 
 * @author Chris Guenther
 */
public class MASSPAEvalEditorController implements ITabController, IUndoRedoHandler, IFindReplaceHandler, ICopyPasteDeleteHandler
{
	private final ObservableDocument	m_agentDefDoc;
	private final ObservableAgents		m_agents;
	private final ObservableTopology	m_topology;
	private final ObservableDocument	m_generatedMASSPADoc;
	private final ObservableDocument	m_evaluationDefDoc;
	private CompoundUndoManager 		m_undoRedoMgrMASSPADef;
	private CompoundUndoManager 		m_undoRedoMgrEvalDef;
	
	public MASSPAEvalEditorController(ObservableDocument _agentDefDoc,
									  ObservableAgents _agents,
									  ObservableTopology _topology,
									  ObservableDocument _generatedMASSPADoc,
									  ObservableDocument _evaluationDefDoc,
									  JButton _masspaDefGenBtn,
									  JButton _evalBtn,
									  CompoundUndoManager _undoRedoMgrMASSPADef,
									  CompoundUndoManager _undoRedoMgrEvalDef)
	{
		m_agentDefDoc = _agentDefDoc;
		m_topology = _topology;
		m_agents = _agents;
		m_generatedMASSPADoc = _generatedMASSPADoc;
		m_evaluationDefDoc = _evaluationDefDoc;
		m_undoRedoMgrMASSPADef = _undoRedoMgrMASSPADef;
		m_undoRedoMgrEvalDef = _undoRedoMgrEvalDef;
		
		// Register as Listener
		_masspaDefGenBtn.addActionListener(new MASSPAGenActionListener());
		_evalBtn.addActionListener(new EvalActionListener());
	}

	//**************************************************
	// Implement IUndoRedoHandler interface
	//**************************************************
	@Override
	public void undo() throws CannotUndoException
	{
		if (m_undoRedoMgrMASSPADef.isComponentFocused())
		{
			m_undoRedoMgrMASSPADef.undo();
		}
		if (m_undoRedoMgrEvalDef.isComponentFocused())
		{
			m_undoRedoMgrEvalDef.undo();
		}
	}

	@Override
	public boolean canUndo()
	{
		if (m_undoRedoMgrMASSPADef.isComponentFocused())
		{
			return m_undoRedoMgrMASSPADef.canUndo();
		}
		if (m_undoRedoMgrEvalDef.isComponentFocused())
		{
			return m_undoRedoMgrEvalDef.canUndo();
		}
		return false;
	}

	@Override
	public void redo() throws CannotRedoException
	{
		if (m_undoRedoMgrMASSPADef.isComponentFocused())
		{
			m_undoRedoMgrMASSPADef.redo();
		}
		if (m_undoRedoMgrEvalDef.isComponentFocused())
		{
			m_undoRedoMgrEvalDef.redo();
		}
	}

	@Override
	public boolean canRedo()
	{
		if (m_undoRedoMgrMASSPADef.isComponentFocused())
		{
			return m_undoRedoMgrMASSPADef.canRedo();
		}
		if (m_undoRedoMgrEvalDef.isComponentFocused())
		{
			return m_undoRedoMgrEvalDef.canRedo();
		}
		return false;
	}

	@Override
	public void setUndoRedoMgr(CompoundUndoManager _mgr)
	{
	}

	/**
	 * Generate a MASSPA model
	 * 
	 * @author Chris Guenther
	 */
	private class MASSPAGenActionListener implements ActionListener
	{
		//**************************************************
		// Implement the ActionListener interface
		//**************************************************
		public void actionPerformed(ActionEvent _e)
		{
			try
			{
				// Clear masspa def
				m_generatedMASSPADoc.remove(0, m_generatedMASSPADoc.getLength());
				
				// Generate MASSPA Code
				String constAndAgents = m_agentDefDoc.getText(0, m_agentDefDoc.getLength());
				m_generatedMASSPADoc.insertString(m_generatedMASSPADoc.getLength(), constAndAgents, null);
				
				Topology topo = m_topology.getTopology();
				if (topo == null) {return;}
				
				// Find locations and populations
				String locsStr = "";
				String popsStr = "";
				for (LocationComponent l : topo.getLocations())
				{
					if (l.getActive())
					{
						locsStr += (locsStr.isEmpty()) ? "" : ",";
						locsStr += l.toString().replace("@", "");
						
						for (MASSPAAgentPop pop : l.getPopulations())
						{
							if(m_agents.getMASSPAAgents().getComponent(pop.getComponent().getName()) != null)
							{
								popsStr += pop.getNameAndInitPop() + ";\n";
							}
							else
							{
								MASSPALogging.warn(String.format(Messages.s_AGENT_STATE_MISSING_DEFINITION,pop.getComponent().getName()));
							}
						}
					}
				}
				m_generatedMASSPADoc.insertString(m_generatedMASSPADoc.getLength(), String.format("\n\n // LOC DEFINITION\nLocations={%s};",locsStr), null);
				m_generatedMASSPADoc.insertString(m_generatedMASSPADoc.getLength(), String.format("\n\n // POP DEFINITION\n%s",popsStr), null);				
			
				// Add channels
				Channels chans = topo.getChannels();
				if (chans == null) {return;}
				
				// Find all agent states
				MASSPAAgents masspaAgents = m_agents.getMASSPAAgents();
				if (masspaAgents == null) {return;}
				Multimap<String, MASSPAComponent> agents = masspaAgents.getAgents();
				Set<MASSPAComponent> states = new HashSet<MASSPAComponent>();
				for (Entry<String, MASSPAComponent> at : agents.entries())
				{
					states.add(at.getValue());
				}
				// Find all message types
				Set<MASSPAMessage> msgTypes = masspaAgents.getMessages();
				
				String chansStr = "";
				for (ChannelComponent cc : chans.getChannels())
				{
					Map<MASSPAChannel,MASSPAChannel> generatedChannelsOrigin = new HashMap<MASSPAChannel,MASSPAChannel>();
					for (MASSPAChannel chan : cc.getDataChannels())
					{
						Location senderLoc = chan.getSender().getLocation(); 
						Location receiverLoc = chan.getReceiver().getLocation();
						if (!topo.getLocation(new LocationComponent(senderLoc.getCoords())).getActive() ||
							!topo.getLocation(new LocationComponent(receiverLoc.getCoords())).getActive())
						{
							continue;
						}

						// Channel description may contain ALL patterns
						// so we need to generate channels for all
						// possible combinations. In the end the we
						// choose the channel that originates from the
						// least generic description
						MASSPAComponent senderState = chan.getSender().getComponent();
						MASSPAComponent receiverState = chan.getReceiver().getComponent();
						MASSPAMessage message = chan.getMsg();
						AbstractExpression intensity = chan.getIntensity();

						// Check sender state is defined
						if (!(senderState instanceof AllComponent))
						{
							if(m_agents.getMASSPAAgents().getComponent(senderState.getName()) == null)
							{
								MASSPALogging.warn(String.format(Messages.s_AGENT_STATE_MISSING_DEFINITION,senderState.getName()));
								continue;
							}
						}
						Set<MASSPAComponent> senders = new HashSet<MASSPAComponent>();
						senders.add(senderState);
						senders = (senderState instanceof AllComponent) ? states : senders;
						
						// Check receiver state is defined
						if (!(receiverState instanceof AllComponent))
						{
							if(m_agents.getMASSPAAgents().getComponent(receiverState.getName()) == null)
							{
								MASSPALogging.warn(String.format(Messages.s_AGENT_STATE_MISSING_DEFINITION,receiverState.getName()));
								continue;
							}
						}
						Set<MASSPAComponent> receivers = new HashSet<MASSPAComponent>();
						receivers.add(receiverState);
						receivers = (receiverState instanceof AllComponent) ? states : receivers;

						Set<MASSPAMessage> msgs = new HashSet<MASSPAMessage>();
						msgs.add(message);
						msgs = (message instanceof AllMessage) ? msgTypes : msgs;
						
						for (MASSPAComponent s : senders)
						{
							for (MASSPAComponent r : receivers)
							{
								for (MASSPAMessage m : msgs)
								{
									MASSPAAgentPop senderPop = new MASSPAAgentPop(s,senderLoc); 
									MASSPAAgentPop receiverPop = new MASSPAAgentPop(r,receiverLoc);
									MASSPAChannel ch = new MASSPAChannel(senderPop,receiverPop,m,intensity);
									
									// Check if this is the more specific channel
									if (generatedChannelsOrigin.containsKey(ch))
									{
										MASSPAChannel currentOrig = generatedChannelsOrigin.get(ch);
										MASSPAComponent senderStateOrig = currentOrig.getSender().getComponent();
										MASSPAComponent receiverStateOrig = currentOrig.getReceiver().getComponent();
										MASSPAMessage messageOrig = currentOrig.getMsg();

										// Check if the new channel is derived from a less general one than the current one
										boolean betterMatchSender=true;
										if (!(senderStateOrig instanceof AllComponent) && (senderState instanceof AllComponent))
										{
											betterMatchSender=false;
										}
										else
										{
											if (senderStateOrig.equals(senderState))
											{
												if (!(receiverStateOrig instanceof AllComponent) && (receiverState instanceof AllComponent))
												{
													betterMatchSender=false;
												}
												else
												{
													if (receiverStateOrig.equals(receiverState))
													{
														if (!(messageOrig instanceof AllMessage) && (message instanceof AllMessage))
														{
															betterMatchSender=false;
														}
													}
												}
											}
										}
										
										if (betterMatchSender)
										{
											generatedChannelsOrigin.put(ch, chan);
										}
									}
									// Check if the channel makes sense according to the PCTMC agent definition
									else
									{
										
										if (((ConstComponent) s).canSend(m) &&
											((ConstComponent) r).canReceive(m))
										{
											generatedChannelsOrigin.put(ch, chan);
										}
									}
								}
							}
						}
					}
					
					for (MASSPAChannel chan : generatedChannelsOrigin.keySet())
					{
						chansStr += chan.toString() + ";\n";
					}
				}
				m_generatedMASSPADoc.insertString(m_generatedMASSPADoc.getLength(), String.format("\n\n // CHANNEL DEFINITION\n%s",chansStr), null);	
			}
			catch(BadLocationException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Evaluate the generated model
	 * 
	 * @author Chris Guenther
	 */
	private class EvalActionListener implements ActionListener
	{
		//**************************************************
		// Implement the ActionListener interface
		//**************************************************
		public void actionPerformed(ActionEvent _e)
		{
			MASSPALogging.clearConsoles();
			try
			{
				String masspa = m_generatedMASSPADoc.getText(0, m_generatedMASSPADoc.getLength());	
				String eval = m_evaluationDefDoc.getText(0, m_evaluationDefDoc.getLength());
				
				// Write model to file
				final String file = "model.masspa";
				BufferedWriter writer = null;
				try
				{
					writer = new BufferedWriter(new FileWriter(file));
					writer.write( masspa + "\n" + eval);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				finally
				{
					try
					{
						if ( writer != null)
						{
							writer.close();
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				
				MASSPALogging.clearConsoles();
				// Run evaluator
		        new Thread()
		        {
		        	public void run()
		        	{	        		
		        		GPAPMain.main(new String[]{"--masspa","--debug", "./dbg", "--onExitGUIContinue", file});
		        		MASSPALogging.printConsoleStats();
		        	}
		        }.start();
			}
			catch (BadLocationException e)
			{
				e.printStackTrace();
			}
		}
	}	
}
