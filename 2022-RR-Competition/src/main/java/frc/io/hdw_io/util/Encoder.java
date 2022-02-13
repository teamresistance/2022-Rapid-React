package frc.io.hdw_io.util;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.math.util.Units;

public class Encoder {

    private WPI_TalonSRX talonCtlr;
    private double tpf;

    /**Interface to Talon encoders */
    public Encoder(WPI_TalonSRX escPort, double _tpf){
        talonCtlr = escPort;
        tpf = _tpf;
    }
  
    public double ticks(){
        return talonCtlr.getSelectedSensorPosition();
    }

    public double feet(){
        return tpf == 0.0 ? 0.0 : talonCtlr.getSelectedSensorPosition() / tpf;
    }

    public double meters() {
        return Units.feetToMeters(feet());
    }

    public void reset(){
        talonCtlr.setSelectedSensorPosition(0, 0, 0);
    }

    public double tpf() {
        return tpf;
    }
}
