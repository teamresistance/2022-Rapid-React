package frc.robot.testing;

import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;

public class DriveTest {

    public static void init(){
        // // -------- Configure Lead drive motors ---------
        // // drvLead_L.configFactoryDefault();    //No equivalent
        // IO.drvLead_L.setInverted(true); // Inverts motor direction and encoder if attached
        // IO.drvLead_L.setBrakeCoastMode(BrakeCoastMode.Brake);
        // // drvLead_L.setSensorPhase(false); // Adjust this to correct phasing with motor

        // // drvLead_R.configFactoryDefault();
        // IO.drvLead_R.setInverted(true); // Inverts motor direction and encoder if attached
        // IO.drvLead_R.setBrakeCoastMode(BrakeCoastMode.Brake);
        // // drvLead_R.setSensorPhase(false); // Adjust this to correct phasing with motor

        // // ----- Tells left and right second drive motors to follow the Lead -----
        // // drvFollower_L.configFactoryDefault();
        // IO.drvFollower_L.setInverted(false);
        // IO.drvFollower_L.setBrakeCoastMode(BrakeCoastMode.Brake);
        // // drvFollower_R.configFactoryDefault();
        // IO.drvFollower_R.setInverted(true);
        // IO.drvFollower_R.setBrakeCoastMode(BrakeCoastMode.Brake);

        // // drvLead_L.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
        // // drvLead_R.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);

    }

    public static void update(){
        //--------- !!! DISABLE FOLLOWER FOR FIRST TESTS !!! -----------
        IO.drvLead_L.set(-JS_IO.axLeftY.get());
        IO.drvLead_R.set(-JS_IO.axRightY.get());

        // IO.drvFollower_L.set(-JS_IO.axLeftY.get());
        // IO.drvFollower_R.set(-JS_IO.axRightY.get());
    }
    
}
