package mapRunner.runner;

import java.util.Iterator;

import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mapRunner.common.HandlePendingMessageBehaviour;
import mapRunner.map.Point;
import mapRunner.map.RunnerLocation;
import mapRunner.map.navigation.Navigation;
import mapRunner.map.navigation.NavigationCommand;
import mapRunner.map.navigation.NavigationCommandType;
import mapRunner.map.navigation.NavigationToTarget;
import mapRunner.map.structure.MapStructure;
import mapRunner.ontology.MapRunnerOntology;
import mapRunner.ontology.Vocabulary;

public class RunnerAgent extends Agent {
	private static final long serialVersionUID = -4794511877491259752L;

	private Runner runner;

	private boolean isBusy;

	private ACLMessage targetRequest;

	private NavigationToTarget navigationToTarget;

	private Navigation navigation;

	private RunnerLocation location;

	private MapCreator mapCreator;

	@Override
	protected void setup() {
		setupRunner();

		addBehaviour(new WaitForTargetBehaviour());
		addBehaviour(new WaitForPathBehaviour());
		addBehaviour(new HandlePendingMessageBehaviour());

		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(MapRunnerOntology.getInstance());
	}

	private void setupRunner() {
		String runnerType = (String) getArguments()[0];
		String runnerStart = (String) getArguments()[1];
		if (runnerType.contentEquals("debug")) {
			runner = new DebugRunner();
		} else {
			runner = new LegoRunner();
		}
		location = new RunnerLocation();
		location.setRunner(this.getLocalName());
		// robot starts at "5"
		location.point.setName(runnerStart);
		location.setDirection(0);
		// "3" for A and B
	}

	private void respondCancel(ACLMessage msg) {
		ACLMessage reply = msg.createReply();
		reply.setPerformative(ACLMessage.CANCEL);
		reply.setContent("busy");
		send(reply);
	}

	private void respondAgree(ACLMessage msg) {
		ACLMessage reply = msg.createReply();
		reply.setPerformative(ACLMessage.AGREE);
		send(reply);
	}

	private void respondInform(ACLMessage msg) {
		ACLMessage reply = msg.createReply();
		reply.setPerformative(ACLMessage.INFORM);
		send(reply);
	}

	public void sendMapToRunner(MapStructure map) {
		SendMapBehaviour b = new SendMapBehaviour();
		b.map = map;
		addBehaviour(b);
	}

	class SendMapBehaviour extends OneShotBehaviour {

		private static final long serialVersionUID = -3289831867204027539L;

		MapStructure map;

		@Override
		public void action() {
			System.out.println("Sended");
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(new AID(("map"), AID.ISLOCALNAME));
			msg.setConversationId(Vocabulary.CONVERSATION_ID_MAP);
			msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
			msg.setOntology(MapRunnerOntology.NAME);
			msg.setContent("map");

			try {
				getContentManager().fillContent(msg, map);
			} catch (CodecException | OntologyException e) {
				e.printStackTrace();
				return;
			}

			send(msg);
		}
	}

	class WaitForTargetBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = -7837531286759971777L;

		MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(Vocabulary.CONVERSATION_ID_TARGET);

		@Override
		public void action() {
			ACLMessage msg = myAgent.receive(messageTemplate);
			if (msg != null) {
				if (isBusy) {
					respondCancel(msg);
				} else {
					targetRequest = msg;
					respondAgree(targetRequest);
					navigationToTarget = new NavigationToTarget();
					navigationToTarget.getTarget().getDestination().setName(msg.getContent());
					navigationToTarget.getTarget().setLocation(location.getPoint());
					requestPath();
				}
			} else {
				block();
			}
		}

		private void requestPath() {
//			addBehaviour(new NotifyAboutLocationChangeBehaviour(location.getPoint()));
			addBehaviour(new RequestPathBehaviour());
		}
	}

	class RequestPathBehaviour extends OneShotBehaviour {
		private static final long serialVersionUID = -6620687276496598269L;

		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(new AID(("map"), AID.ISLOCALNAME));
			msg.setConversationId(Vocabulary.CONVERSATION_ID_PATH);
			msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
			msg.setOntology(MapRunnerOntology.NAME);
			try {
				getContentManager().fillContent(msg, navigationToTarget);
			} catch (CodecException | OntologyException e) {
				e.printStackTrace();
				return;
			}
			send(msg);
		}
	}

	class WaitForPathBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 6651524509534077272L;

		MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(Vocabulary.CONVERSATION_ID_PATH);

		@Override
		public void action() {
			ACLMessage msg = myAgent.receive(messageTemplate);
			if (msg != null) {
				if (isBusy) {
					respondCancel(msg);
				} else {
					ACLMessage reply = msg.createReply();
					ContentManager cm = getContentManager();
					ContentElement ce = null;
					try {
						ce = cm.extractContent(msg);
					} catch (CodecException | OntologyException e) {
						e.printStackTrace();
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent(e.getMessage());
						send(reply);
						return;
					}
					if (!(ce instanceof NavigationToTarget)) {
						reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
						send(reply);
						return;
					}
					navigationToTarget = (NavigationToTarget) ce;
					navigation = navigationToTarget.getNavigation();
					if (navigation.commands.size() == 1 && ((NavigationCommand) navigation.commands.get(0)).type == 3
							&& ((NavigationCommand) navigation.commands.get(0)).getPoint().name.equals("0")) {
						System.out.println("new");
						mapCreator = new MapCreator();
						addBehaviour(new CreateMapBehaviour());
					} else {
						addBehaviour(new MoveOnPathBehaviour());
					}
				}
			} else {
				block();
			}
		}
	}

	class CreateMapBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = -9019504440001330522L;

		private BehaviourState state = BehaviourState.initial;

//		private Iterator<?> iterator = null;
		
		private NavigationCommand command = new NavigationCommand();

		@Override
		public void action() {
			switch (state) {
			case initial:
				initialization();
				break;
			case active:
//				if (iterator.hasNext()) {
//					activity();
				if (command != null) {
					activity();
				} else {
					state = BehaviourState.beforeEnd;
				}
				break;
			case beforeEnd:
				shutdown();
				state = BehaviourState.finished;
				break;
			default:
				state = BehaviourState.finished;
				break;
			}
		}

		private void initialization() {
			isBusy = true;
//			iterator = navigation.commands.iterator();
			state = BehaviourState.active;
			runner.start();
		}

		private void activity() {

//			NavigationCommand command = (NavigationCommand) iterator.next();
			command = (NavigationCommand) navigation.commands.get(navigation.commands.size() - 1);

			switch (command.type) {
			case NavigationCommandType.FORWARD:
				runner.move(command.quantity);
				// TODO: enhance algorithm to create map without these corrective rotations
//				switch (location.getDirection()) {
//				case 1:
//					runner.rotate(1, null);
//					break;
//				case 2:
//					runner.rotate(2, null);
//					break;
//				case 3:
//					runner.rotate(-1, null);
//					break;
//				}
//				location.setDirection(0);
				if (!mapCreator.isMapCompleted) {
					navigation.addNavigationCommand(NavigationCommandType.ROTATE_180_DEGREE, 2, "0");
				}
				break;
			case NavigationCommandType.ROTATE_180_DEGREE:
				mapCreator.startDirection = location.direction;
				
				runner.rotate(2 * command.quantity, mapCreator);
				System.out.println("mapCreator.checkedPoints " + mapCreator.checkedPoints);
				if (mapCreator.currentPointY > 0 && !mapCreator.checkedPoints
						.contains(mapCreator.pointGrid[mapCreator.currentPointY - 1][mapCreator.currentPointX])) {
					System.out.println("top " + location.direction);
					switch (location.direction) {
					case 1:
						runner.rotate(1, null);
						break;
					case 2:
						runner.rotate(2, null);
						break;
					case 3:
						runner.rotate(-1, null);
						break;
					}
					location.setDirection(0);
					navigation.addNavigationCommand(NavigationCommandType.FORWARD, 1, "0");
					mapCreator.currentPointY -= 1;
				} else if (mapCreator.currentPointX < mapCreator.widthOfMap - 1 && !mapCreator.checkedPoints
						.contains(mapCreator.pointGrid[mapCreator.currentPointY][mapCreator.currentPointX + 1])) {
					System.out.println("right " + location.direction);
					switch (location.direction) {
					case 0:
						runner.rotate(-1, null);
						break;
					case 2:
						runner.rotate(1, null);
						break;
					case 3:
						runner.rotate(2, null);
						break;
					}
					location.setDirection(1);
					navigation.addNavigationCommand(NavigationCommandType.FORWARD, 1, "0");
					mapCreator.currentPointX += 1;
				} else if (mapCreator.currentPointY < mapCreator.heightOfMap - 1 && !mapCreator.checkedPoints
						.contains(mapCreator.pointGrid[mapCreator.currentPointY + 1][mapCreator.currentPointX])) {
					System.out.println("bottom " + location.direction);
					switch (location.direction) {
					case 0:
						runner.rotate(2, null);
						break;
					case 1:
						runner.rotate(-1, null);
						break;
					case 3:
						runner.rotate(1, null);
						break;
					}
					location.setDirection(2);
					navigation.addNavigationCommand(NavigationCommandType.FORWARD, 1, "0");
					mapCreator.currentPointY += 1;
				} else if (mapCreator.currentPointX > 0 && !mapCreator.checkedPoints
						.contains(mapCreator.pointGrid[mapCreator.currentPointY][mapCreator.currentPointX - 1])) {
					System.out.println("left " + location.direction);
					switch (location.direction) {
					case 0:
						runner.rotate(1, null);
						break;
					case 1:
						runner.rotate(2, null);
						break;
					case 2:
						runner.rotate(-1, null);
						break;
					}
					location.setDirection(3);
					navigation.addNavigationCommand(NavigationCommandType.FORWARD, 1, "0");
					mapCreator.currentPointX -= 1;
				}

				break;
			}

//			if (!mapCreator.isMapCompleted) {
//				iterator = navigation.commands.iterator();
//				for (int i = 1; i < navigation.commands.size(); i++) {
//					iterator.next();
//				}
//			} else {
			if (mapCreator.isMapCompleted) {
				command = null;
				System.out.println(navigation.commands.toString());
				MapStructure map = mapCreator.makeAMap();
				sendMapToRunner(map);
			}
			if (command != null) {
				Point newPoint = command.point;
				newPoint.setName(
						Integer.toString(mapCreator.pointGrid[mapCreator.currentPointY][mapCreator.currentPointX]));
				addBehaviour(new NotifyAboutLocationChangeBehaviour(newPoint));				
			}
		}

		private void shutdown() {
			runner.stop();
			respondInform(targetRequest);
			isBusy = false;
		}

		@Override
		public boolean done() {
			return state == BehaviourState.finished;
		}

	}

	class MoveOnPathBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 5468774161513653773L;

		private BehaviourState state = BehaviourState.initial;

		private Iterator<?> iterator = null;

		@Override
		public void action() {
			switch (state) {
			case initial:
				initialization();
				break;
			case active:
				if (iterator.hasNext()) {
					activity();
				} else {
					state = BehaviourState.beforeEnd;
				}
				break;
			case beforeEnd:
				shutdown();
				state = BehaviourState.finished;
				break;
			default:
				state = BehaviourState.finished;
				break;
			}
		}

		private void initialization() {
			isBusy = true;
			iterator = navigation.commands.iterator();
			state = BehaviourState.active;
			runner.start();
		}

		private void activity() {
			NavigationCommand command = (NavigationCommand) iterator.next();

			switch (command.type) {
			case NavigationCommandType.FORWARD:
				runner.move(command.quantity);
				break;
			case NavigationCommandType.ROTATE_LEFT_90_DEGREE:
				runner.rotate(1 * command.quantity, null);
				switch (location.direction) {
				case 0:
					location.direction = 3;
					break;
				case 1:
				case 2:
				case 3:
					location.direction -= 1;
					break;
				}
				break;
			case NavigationCommandType.ROTATE_RIGHT_90_DEGREE:
				runner.rotate(-1 * command.quantity, null);
				switch (location.direction) {
				case 0:
				case 1:
				case 2:
					location.direction += 1;
					break;
				case 3:
					location.direction = 0;
					break;
				}
				break;
			case NavigationCommandType.ROTATE_180_DEGREE:
				runner.rotate(2 * command.quantity, null);
				switch (location.direction) {
				case 0:
				case 1:
					location.direction += 2;
					break;
				case 2:
					location.direction = 0;
					break;
				case 3:
					location.direction = 1;
					break;
				}
				break;
			}
//			location.setPoint(command.point);
			Point newPoint = command.point;
			addBehaviour(new NotifyAboutLocationChangeBehaviour(newPoint));
		}

		private void shutdown() {
			runner.stop();
			respondInform(targetRequest);
			isBusy = false;
		}

		@Override
		public boolean done() {
			return state == BehaviourState.finished;
		}
	}

	private enum BehaviourState {
		initial, active, finished, beforeEnd
	};

	class NotifyAboutLocationChangeBehaviour extends OneShotBehaviour {
		private static final long serialVersionUID = 7084813117098820135L;
		private Point currentPoint;

		public NotifyAboutLocationChangeBehaviour(Point newPoint) {
			currentPoint = newPoint;
		}

		@Override
		public void action() {
			location.setPoint(currentPoint);
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(new AID(("map"), AID.ISLOCALNAME));
			msg.setConversationId(Vocabulary.CONVERSATION_ID_LOCATION);
			msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
			msg.setOntology(MapRunnerOntology.NAME);
			try {
				getContentManager().fillContent(msg, location);

			} catch (CodecException | OntologyException e) {
				e.printStackTrace();
				return;
			}
			send(msg);
		}
	}
}
