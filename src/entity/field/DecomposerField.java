package src.entity.field;

import src.bsim.BSim;
import src.entity.Molecule;

public class DecomposerField extends MoleculeField {

    public DecomposerField(BSim sim, Molecule molecule, int[] boxes, double diffusivity) {
        super(sim, molecule, boxes, diffusivity);
        this.molecule.setDecomposerField(this);
    }

    public SignalField getSignalField() {
        return this.getMolecule().getSignalField();
    }

    @Override
    public void update() {
        diffuse();
        for (int i = 0; i < boxes[0]; i++) {
            for (int j = 0; j < boxes[1]; j++) {
                for (int k = 0; k < boxes[2]; k++) {
                    double decayedConcentration = getConc(i, j, k) * Math.random() * 0.025;
                    double totalConc = 0.0;
                    totalConc += (getSignalField() != null) ? getSignalField().getConc(i, j, k) : 0;
                    this.decay(i, j, k, Math.min(decayedConcentration, totalConc));
                    if (getSignalField() != null) {
                        if (decayedConcentration < totalConc) {
                            getSignalField().decay(i, j, k,
                                    decayedConcentration * getSignalField().getConc(i, j, k) / totalConc);
                        } else {
                            getSignalField().decay(i, j, k, getSignalField().getConc(i, j, k));
                        }
                    }
                }
            }
        }
    }
}
