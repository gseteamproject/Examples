package knowledge;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class NotUnderstoodBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -6792131929326360534L;

	private ACLMessage message;

	public NotUnderstoodBehaviour(ACLMessage message) {
		this.message = message;
	}

	@Override
	public void action() {
		KnowledgeAgent myKnowledgeAgent = (KnowledgeAgent) myAgent;
		myKnowledgeAgent.trace("not understood (" + message.getContent() + ")");
		ACLMessage reply = message.createReply();
		reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
		myKnowledgeAgent.send(reply);
	}

}
