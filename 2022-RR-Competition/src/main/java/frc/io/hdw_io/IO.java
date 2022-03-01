package frc.io.hdw_io;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.motorcontrol.Victor;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.util.*;
import edu.wpi.first.wpilibj.PneumaticsModuleType;

// import com.ctre.phoenix.motorcontrol.*;
// import com.ctre.phoenix.motorcontrol.can.*;
import com.playingwithfusion.CANVenom;
import com.playingwithfusion.CANVenom.BrakeCoastMode;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ColorSensorV3;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.I2C;


public class IO {
    // navX
    public static NavX navX = new NavX(SPI.Port.kMXP);

    // PDP
    public static PowerDistribution pdp = new PowerDistribution(0,ModuleType.kCTRE);

    // Air
    public static Compressor compressor1 = new Compressor(1,PneumaticsModuleType.CTREPCM);
    public static Compressor compressor2 = new Compressor(2,PneumaticsModuleType.CTREPCM);
    public static Relay compressorRelay = new Relay(0);

    // Drive
    public static CANVenom drvLead_L = new CANVenom(11); // Cmds left wheels. Includes encoders
    public static CANVenom drvLead_R = new CANVenom(12); // Cmds right wheels. Includes encoders
    public static CANVenom drvFollower_L = new CANVenom(15); // Resrvd 3 & 4 maybe
    public static CANVenom drvFollower_R = new CANVenom(16); // Resrvd 7 & 8 maybe
    //As of 2022 DifferentialDrive no longer inverts the right motor.  Do this in the motor controller.
    public static DifferentialDrive diffDrv_M = new DifferentialDrive(IO.drvLead_L, IO.drvLead_R);

    public static final double drvLeadTPF_L = 368.4;  // 1024 t/r (0.5' * 3.14)/r 9:60 gr = 385.4  calibrated= 364.63
    public static final double drvLeadTPF_R = -368.4; // 1024 t/r (0.5' * 3.14)/r 9:60 gr = 385.4  calibrated= 364.63
    public static Encoder_Pwf drvEnc_L = new Encoder_Pwf(drvLead_L, drvLeadTPF_L);  //Interface for feet, ticks, reset
    public static Encoder_Pwf drvEnc_R = new Encoder_Pwf(drvLead_R, drvLeadTPF_R);

    public static CoorSys coorXY = new CoorSys(navX, drvEnc_L, drvEnc_R);   //CoorXY & drvFeet

    // Snorfler
    public static Victor snorfFeed_Mtr = new Victor(6);     //Feed motor on snorfler
    public static Victor snorfElv_Mtrs = new Victor(7);    //Lower elevator motor
    public static ISolenoid snorflerExt_SV = new InvertibleSolenoid(1, PneumaticsModuleType.CTREPCM, 0, false); // Extends both feeders

    public static ColorSensorV3 ballColorSensor = new ColorSensorV3(I2C.Port.kOnboard);

    // Shooter
    public static ISolenoid select_low_SV = new InvertibleSolenoid(1, PneumaticsModuleType.CTREPCM, 1);    // Defaults hi prs; true for lo prs
    public static ISolenoid catapult_L_SV = new InvertibleSolenoid(1, PneumaticsModuleType.CTREPCM, 2); // Left catapult trigger. 
    public static ISolenoid catapult_R_SV = new InvertibleSolenoid(1, PneumaticsModuleType.CTREPCM, 3);// Right catapult trigger.

    // Climb
    public static CANSparkMax climbMotor       = new CANSparkMax(6, MotorType.kBrushless);
    public static CANSparkMax climbMotorFollow = new CANSparkMax(7, MotorType.kBrushless);
    public static ISolenoid climbBrakeRel_SV   = new InvertibleSolenoid(2,PneumaticsModuleType.CTREPCM, 0);
    public static ISolenoid lockPinAExt_SV = new InvertibleSolenoid(2,PneumaticsModuleType.CTREPCM, 1);
    public static ISolenoid lockPinARet_SV = new InvertibleSolenoid(2,PneumaticsModuleType.CTREPCM, 2);
    public static ISolenoid lockPinBExt_SV = new InvertibleSolenoid(2,PneumaticsModuleType.CTREPCM, 3, true);
    public static ISolenoid sliderExt_SV   = new InvertibleSolenoid(2,PneumaticsModuleType.CTREPCM, 4, true);
    public static InvertibleDigitalInput lockPinAExt_L_FB = new InvertibleDigitalInput(1,true);
    public static InvertibleDigitalInput lockPinAExt_R_FB = new InvertibleDigitalInput(2,true);
    public static InvertibleDigitalInput lockPinBExt_L_FB = new InvertibleDigitalInput(3,true);
    public static InvertibleDigitalInput lockPinBExt_R_FB = new InvertibleDigitalInput(4,true);
    public static InvertibleDigitalInput sliderExt_L_FB   = new InvertibleDigitalInput(5,true);
    public static InvertibleDigitalInput sliderExt_R_FB   = new InvertibleDigitalInput(6,true);
    
    /**
     * Initialize any hardware
     */
    public static void init() {
        drvsInit();
        motorsInit();
        coorXY.reset();
        coorXY.drvFeetRst();
    }

    /**
     * Initialize drive configuration setup.
     */
    private static void drvsInit() {
        // -------- Configure Lead drive motors ---------
        // drvLead_L.configFactoryDefault();    //No equivalent
        drvLead_L.setInverted(true); // Inverts motor direction and encoder if attached
        drvLead_L.setBrakeCoastMode(BrakeCoastMode.Brake);
        // drvLead_L.setSensorPhase(false); // Adjust this to correct phasing with motor

        // drvLead_R.configFactoryDefault();
        drvLead_R.setInverted(true); // Inverts motor direction and encoder if attached
        drvLead_R.setBrakeCoastMode(BrakeCoastMode.Brake);
        // drvLead_R.setSensorPhase(false); // Adjust this to correct phasing with motor

        // ----- Tells left and right second drive motors to follow the Lead -----
        // drvFollower_L.configFactoryDefault();
        drvFollower_L.setInverted(false);
        drvFollower_L.setBrakeCoastMode(BrakeCoastMode.Brake);
        // drvFollower_R.configFactoryDefault();
        drvFollower_R.setInverted(true);
        drvFollower_R.setBrakeCoastMode(BrakeCoastMode.Brake);

        // drvLead_L.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
        // drvLead_R.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
    }

    /**
     * Initialize other motors besides the drive motors.
     */
    private static void motorsInit() {
        snorfFeed_Mtr.setInverted(false);
        snorfElv_Mtrs.setInverted(false);

        climbMotor.restoreFactoryDefaults();
        climbMotor.setInverted(false);
        climbMotor.setIdleMode(IdleMode.kBrake);

        climbMotorFollow.restoreFactoryDefaults();
        climbMotorFollow.setInverted(false);
        climbMotorFollow.setIdleMode(IdleMode.kBrake);
        // climbMotorFollow.follow(climbMotor);     //Disabled for teesting
    }

    public static void update() {
        SmartDashboard.putNumber("Robot/Feet", coorXY.drvFeet());
        SmartDashboard.putNumber("Robot/EncTicks L", drvEnc_L.ticks());
        SmartDashboard.putNumber("Robot/EncTicks R", drvEnc_R.ticks());
        SmartDashboard.putNumber("Robot/Mtr0 Cmd", drvLead_R.get());
        SmartDashboard.putNumber("Robot/Mtr1 Cmd", drvFollower_R.get());
        SmartDashboard.putNumber("Robot/Mtr12 Cmd", drvLead_L.get());
        SmartDashboard.putNumber("Robot/Mtr11 Cmd", drvFollower_L.get());
    }
}
