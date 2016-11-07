package pallete.tests;

import common.Test;
import common.TestException;
import common.TestUtility;
import jade.content.Concept;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Done;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import palleteRobotCommunication.PalleteOntology;
import palleteRobotCommunication.Question;
import palleteRobotCommunication.RobotRequest;
import palleteRobotCommunication.SourcePalleteReply;
import palleteRobotCommunication.State;

public class PalleteNotEmptyAnymoreTest extends Test {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AID sourcePallete;

	Codec codec = new SLCodec();
	Ontology ontology = PalleteOntology.getInstance();

	public Behaviour load(Agent a) throws TestException {
		setTimeout(2000);
		sourcePallete = TestUtility.createAgent(a, "test", "palleteRobotCommunication.SourcePalleteAgent",
				new Object[] { 5 });
		CyclicBehaviour b = new CyclicBehaviour(a) {
			private static final long serialVersionUID = -3423642459063630856L;

			public void onStart() {
				ContentManager cm = myAgent.getContentManager();
				cm.registerLanguage(codec);
				cm.registerOntology(ontology);
				Question question = new Question();
				question.setText(RobotRequest.WHAT_YOUR_STATE);

				Action a = new Action();
				a.setAction(question);
				a.setActor(sourcePallete);

				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(sourcePallete);
				msg.setOntology(PalleteOntology.NAME);
				msg.setLanguage(codec.getName());
				try {
					cm.fillContent(msg, a);
				} catch (CodecException e) {
					e.printStackTrace();
				} catch (OntologyException e) {
					e.printStackTrace();
				}
				/*
				 * ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				 * msg.addReceiver(sourcePallete);
				 * msg.setContent(RobotRequest.WHAT_YOUR_STATE);
				 */
				System.out.println("Sending message " + msg);
				myAgent.send(msg);
			}

			public void action() {
				/*
				 * ACLMessage msg = myAgent.receive(); if (msg != null &&
				 * msg.getContent().equals(SourcePalleteReply.NOT_EMPTY_ANYMORE)
				 * ) { passed("Reply correct."); }
				 */
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					try {
						Done a = (Done) myAgent.getContentManager().extractContent(msg);
						Concept c = (Concept) a.getAction();
						State state = (State) ((Action) c).getAction();
						if (state != null) {
							if (state.getDescription().equals(SourcePalleteReply.NOT_EMPTY_ANYMORE)) {
								passed("Reply correct");
							} else {
								failed("Reply incorrect");
							}
						}

					} catch (OntologyException oe) {
						oe.printStackTrace();
					} catch (jade.content.lang.Codec.CodecException e) {
						e.printStackTrace();
					}
					// passed("Reply correct.");
				}
			}
		};
		return b;
	}

	public void clean(Agent a) {
		try {
			TestUtility.killAgent(a, sourcePallete);
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
