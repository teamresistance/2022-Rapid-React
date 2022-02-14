package frc.io.hdw_io.util;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.math.util.Units;

public class Encoder_Tln {

    private WPI_TalonSRX talonCtlr;
    private double tpf;

    /**Interface to Talon encoders */
    public Encoder_Tln(WPI_TalonSRX escPort, double _tpf){
        talonCtlr = escPort;
        tpf = _tpf;
    }

    /**@return Encoder ticks. */
    public double ticks(){
        return talonCtlr.getSelectedSensorPosition();
    }

    /**@return calculated feet from ticks. */
    public double feet(){
        return tpf == 0.0 ? 0.0 : talonCtlr.getSelectedSensorPosition() / tpf;
    }

    /**@return calcuate meters from feet. */
    public double meters() {
        return Units.feetToMeters(feet());
    }

    /** Reset encoder count to zero. */
    public void reset(){
        talonCtlr.setSelectedSensorPosition(0, 0, 0);
    }

    /**@return the existiing ticks per foot, tpf. */
    public double getTPF() { return tpf; }

    /**@param tpf - Set ticks per foot.  */
    public void setTPF( double tpf) { this.tpf = tpf; }
}
