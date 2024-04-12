package src.entity.bacteria;

import javax.vecmath.Vector3d;

import src.bsim.BSim;
import src.entity.controller.TransmitterController;
import src.entity.field.SignalField;

public class TransmitterBacterium extends Bacterium {
    private TransmitterController controller;
    private boolean emittingBit;
    private SignalField currentSignalField;

    public TransmitterBacterium(BSim sim, Vector3d position, TransmitterController controller) {
        super(sim, position, 1);
        this.controller = controller;
        this.controller.setBacterium(this);
        this.emittingBit = this.controller.getEmittingBit();
        this.currentSignalField = this.controller.getCurrentSignal();
    }

    public TransmitterController getController() {
        return this.controller;
    }

    public SignalField getCurrentSignal() {
        return this.controller.getCurrentSignal();
    }

    private boolean signalHasChanged() {
        SignalField currentControllerSignal = this.controller.getCurrentSignal();
        boolean hasChanged = this.currentSignalField != currentControllerSignal;
        if (hasChanged) {
            this.currentSignalField = currentControllerSignal;
        }
        return hasChanged;
    }

    @Override
    public void vesiculateSignal(int quantity) {
        this.controller.getCurrentSignal().addQuantity(getPosition(), quantity);
    }

    @Override
    public void vesiculateDecomposer(int quantity) {
        this.controller.getCurrentSignal().getDecomposerField().addQuantity(getPosition(), quantity);
    }

    @Override
    public void action() {
        if (this.emittingBit) {
            this.controller.action();
            vesiculateSignal(100);
            vesiculateDecomposer(100); // decomposer first otherwise it will directly kill signal
        } else {
            if (this.controller.action()) {
                if (signalHasChanged()) {
                    vesiculateSignal(100 * 80);
                }
            } else {
                vesiculateDecomposer(100 * 2);
            }
        }
    }

}
