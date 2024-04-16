package src.entity.bacteria;

import javax.vecmath.Vector3d;

import src.bsim.BSim;
import src.entity.Utils.Gate;
import src.entity.controller.ReceiverController;
import src.entity.field.SignalField;

public class ReceiverBacterium extends Bacterium {
    private ReceiverController controller;
    private Gate gate;
    private boolean is_init;

    public ReceiverBacterium(BSim sim, Vector3d position, ReceiverController controller, Gate gate) {
        super(sim, position, 1);
        this.controller = controller;
        this.controller.setBacterium(this);
        this.gate = gate;
        this.is_init = true; // set if we want the receiver to only init when detecting both input signals
    }

    public ReceiverController getController() {
        return this.controller;
    }

    public SignalField getCurrentOutputSignal() {
        return this.controller.getCurrentOutputSignal();
    }

    @Override
    public void vesiculateSignal(int quantity) {
        this.controller.getCurrentOutputSignal().addQuantity(getPosition(), quantity);
    }

    @Override
    public void vesiculateDecomposer(int quantity) {
        this.controller.getCurrentOutputSignal().getDecomposerField().addQuantity(getPosition(), quantity);
    }

    @Override
    public void action() {
        if (!this.is_init) {
            if (this.controller.getConcentration(this.controller.getCurrentInput1Signal()) > this.controller
                    .getLbThreshold()
                    && this.controller.getConcentration(this.controller.getCurrentInput2Signal()) > this.controller
                            .getLbThreshold()) {
                this.is_init = true;
            } else {
                return;
            }
        } else {
            this.controller.action();
        }
        switch (this.gate) {
            case OR:
                double max = Math.max(this.controller.getConcentration(this.controller.getCurrentInput1Signal()),
                        this.controller.getConcentration(this.controller.getCurrentInput2Signal()));
                if (max > this.controller.getLbThreshold()) {
                    double[] box = this.controller.getCurrentOutputSignal().getBox();
                    int quantity = (int) (max * box[0] * box[1] * box[2]);
                    vesiculateSignal(quantity);
                    // Can understand as a chemical reaction
                    this.controller.getCurrentInput1Signal().addQuantity(getPosition(), -max);
                    this.controller.getCurrentInput2Signal().addQuantity(getPosition(), -max);
                    vesiculateDecomposer(quantity);
                }
                break;
            case AND:
                double min = Math.min(this.controller.getConcentration(this.controller.getCurrentInput1Signal()),
                        this.controller.getConcentration(this.controller.getCurrentInput2Signal()));
                if (min > this.controller.getLbThreshold()) {
                    double[] box = this.controller.getCurrentOutputSignal().getBox();
                    int quantity = (int) (min * box[0] * box[1] * box[2]);
                    vesiculateSignal(quantity);
                    // Can understand as chemical reaction
                    this.controller.getCurrentInput1Signal().addQuantity(getPosition(), -min);
                    this.controller.getCurrentInput2Signal().addQuantity(getPosition(), -min);
                    vesiculateDecomposer(quantity);
                }
                break;
            default:
                assert false : this.gate;
        }
    }
}
