package src.entity.field;

import src.bsim.BSim;
import src.entity.Molecule;

public class SignalField extends MoleculeField {
    public SignalField(BSim bsim, Molecule molecule, int[] boxes, double diffusivity) {
        super(bsim, molecule, boxes, diffusivity);
        this.molecule.setSignalField(this);
    }

    public DecomposerField getDecomposerField() {
        return this.getMolecule().getDecomposerField();
    }
}
