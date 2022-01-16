package frc.io.hdw_io;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;

public class InvertibleSolenoid extends Solenoid implements ISolenoid {

    private final boolean isInverted;

    // Default Solenoid Constructor, not inverted
    public InvertibleSolenoid(PneumaticsModuleType module, int channel) {
        this(module, channel, false);
    }
    // Constructor to Make Solenoid Object
    public InvertibleSolenoid(PneumaticsModuleType module, int channel, boolean isInverted) {
        super(module, channel);
        this.isInverted = isInverted;
    }

    //This activates the Solenoid
    @Override
    public void set(boolean state) {
        if (isInverted) {
            super.set(!state);
        } else {
            super.set(state);
        }
    }

    public boolean get(){
        return (isInverted ? !super.get() : super.get());
    }

}
