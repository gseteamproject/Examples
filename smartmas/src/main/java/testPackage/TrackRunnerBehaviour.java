package testPackage;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.RegulatedMotor;


public class TrackRunnerBehaviour extends Behaviour {
	
	final static float MaxReflectedLight = 0.2f;
	final static int ForwardSpeed = 300;
	final static int RotationSpeed = 100;
	
	private Brick _brick;
	private GraphicsLCD _lcd;
	private RegulatedMotor _leftMotor;
	private RegulatedMotor _rightMotor;
	//private RMIRegulatedMotor _rightMotor;
	private SensorMode _redSensorMode;

	private EV3ColorSensor colorSensor;
	
	
	public TrackRunnerBehaviour(Agent agent) {
		super(agent);
		_brick = BrickFinder.getLocal();
		
		_lcd = _brick.getGraphicsLCD();
		
		_leftMotor = new EV3LargeRegulatedMotor(_brick.getPort("D"));
		_rightMotor =new EV3LargeRegulatedMotor(_brick.getPort("A"));
		
		//_rightMotor = _brick.createRegulatedMotor("A", 'L');
		_leftMotor.setSpeed(ForwardSpeed);
		_rightMotor.setSpeed(ForwardSpeed);
		
		
		colorSensor = new EV3ColorSensor(_brick.getPort("S1"));
		_redSensorMode = colorSensor.getRedMode();
	}
	
	
	public void printData( float[] data){
		_lcd.setFont(Font.getSmallFont());
		
		_lcd.clear();
		for (int i = 0; i < data.length; ++i){
			_lcd.drawString(String.format("%.2f", data[i]), _lcd.getWidth()/2, i*10, GraphicsLCD.HCENTER);
		}
		_lcd.refresh();
	}
	
	public void moveForward() {
		_rightMotor.setSpeed(ForwardSpeed);
		_leftMotor.setSpeed(ForwardSpeed);
		_leftMotor.forward();
		_rightMotor.forward();
	}
	
	public void stopMoving() {
		_leftMotor.stop(true);
		_rightMotor.stop(true);
	}
	
	public void turnDirection(Direction direction) {
		_rightMotor.setSpeed(RotationSpeed);
		_leftMotor.setSpeed(RotationSpeed);
		if (direction == Direction.LEFT){
			_rightMotor.forward();
			_leftMotor.backward();
		}
		else{
			_leftMotor.forward();
			_rightMotor.backward();
		}
	}
	
	private enum Direction { LEFT, RIGHT }

	
	public void action() {
		
		Direction lastDirection = Direction.RIGHT;	
		int turningTime = 1000;
		
		boolean turningMode = false;
		long startTime = 0;
		
		while(!_brick.getKey("Escape").isDown()) {
			long currentTime = System.currentTimeMillis();
			
			float[] data = new float[_redSensorMode.sampleSize()];
			_redSensorMode.fetchSample(data, 0);
			
			printData(data);
			
			float reflectedLight = data[0];
			
			if (reflectedLight < MaxReflectedLight){
				if (turningMode){
					stopMoving();
				}
				moveForward();
				turningTime = 200;
				turningMode = false;
				startTime = currentTime;
			}
			else{
				if (currentTime - startTime > turningTime){					
					stopMoving();
					
					if (turningMode){
						if (lastDirection == Direction.RIGHT){
							lastDirection = Direction.LEFT;
						} else {
							lastDirection = Direction.RIGHT;
						}
					} 
					else {
						turningMode = true;
					}
					
					startTime = currentTime;
					turningTime += 300;
					turnDirection(lastDirection);
					turningMode = true;
				}
			}
		}
		
		myAgent.doDelete();
	}


	@Override
	public boolean done() {
		return false;
	}
	
	private static final long serialVersionUID = 4073862365806953815L;
}
