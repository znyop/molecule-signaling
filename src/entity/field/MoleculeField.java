package src.entity.field;

import javax.vecmath.Vector3d;

import src.bsim.BSim;
import src.entity.Molecule;

public abstract class MoleculeField {
    private BSim sim;
    protected Molecule molecule;
    protected int[] boxes = new int[3]; // Number of boxes in three dimensions
    private double[] box = new double[3]; // x,y,z length of a box
    private double[][][] quantity;
    private double diffusivity;

    public MoleculeField(BSim sim, Molecule molecule, int[] boxes, double diffusivity) {
        this.sim = sim;
        this.molecule = molecule;
        this.boxes = boxes;
        this.box[0] = this.sim.getBound().x / (double) this.boxes[0];
        this.box[1] = this.sim.getBound().y / (double) this.boxes[1];
        this.box[2] = this.sim.getBound().z / (double) this.boxes[2];
        this.quantity = new double[boxes[0]][boxes[1]][boxes[2]];
        this.diffusivity = diffusivity;
    }

    public Molecule getMolecule() {
        return this.molecule;
    }

    public int[] getBoxes() {
        return this.boxes;
    }

    public double[] getBox() {
        return this.box;
    }

    public double getConc(int i, int j, int k) {
        return quantity[i][j][k] / (this.box[0] * this.box[1] * this.box[2]);
    }

    public double getConc(Vector3d v) {
        int[] b = boxCoords(v);
        int x = b[0];
        int y = b[1];
        int z = b[2];
        return quantity[x][y][z] / (this.box[0] * this.box[1] * this.box[2]);
    }

    public int[] boxCoords(Vector3d v) {
        /* Check the bounds are valid */
        int x = (int) (v.x / box[0]);
        int y = (int) (v.y / box[1]);
        int z = (int) (v.z / box[2]);
        x = (x >= boxes[0] ? boxes[0] - 1 : x);
        y = (y >= boxes[1] ? boxes[1] - 1 : y);
        z = (z >= boxes[2] ? boxes[2] - 1 : z);
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        if (z < 0)
            z = 0;
        return new int[] { x, y, z };
    }

    public void addQuantity(Vector3d v, double q) {
        int[] b = boxCoords(v);
        int x = b[0];
        int y = b[1];
        int z = b[2];
        this.quantity[x][y][z] += q;
        if (this.quantity[x][y][z] < 0)
            this.quantity[x][y][z] = 0;
    }

    public void decay(int i, int j, int k, double decayedConcentration) {
        this.quantity[i][j][k] -= decayedConcentration * (this.box[0] * this.box[1] * this.box[2]);
        if (this.quantity[i][j][k] < 0)
            this.quantity[i][j][k] = 0;
    }

    public void diffuse() {
        double[][][] before = quantity;
        /*
         * Index of the box in positive (negative) .. direction, taking account of the
         * boundary conditions
         */
        int xAbove, xBelow, yAbove, yBelow, zAbove, zBelow;
        /*
         * Quantity of chemical leaving the box in the positive (negative) .. direction
         */
        double qxAbove, qxBelow, qyAbove, qyBelow, qzAbove, qzBelow;
        /* Flags for leakiness at borders */
        boolean leaky[] = sim.getLeaky();
        double leakyRate[] = sim.getLeakyRate();
        /*
         * Flux of molecules crossing in the positive x-direction (Fick's law)
         * J = -D(dC/dx) = -D*(C(x+dx)-C(x))/dx = -D*(N(x+dx)-N(x))/((dx)^2*dy*dz)
         * molecules/(micron)^2/sec
         * Number of molecules transferred in the positive x-direction over dt
         * xAbove = J*(dy*dz)*dt = -((D*dt)/(dx)^2)*(N(x+dx)-N(x)) = -kX*(N(x+dx)-N(x))
         * where kX = (D*dt)/(dx)^2 is a dimensionless constant
         */
        double normX = sim.getDt() / Math.pow(box[0], 2);
        double normY = sim.getDt() / Math.pow(box[1], 2);
        double normZ = sim.getDt() / Math.pow(box[2], 2);
        double kX = diffusivity * normX;
        double kY = diffusivity * normY;
        double kZ = diffusivity * normZ;
        for (int i = 0; i < boxes[0]; i++)
            for (int j = 0; j < boxes[1]; j++)
                for (int k = 0; k < boxes[2]; k++) {
                    /*
                     * Is this box the last box?
                     * If so, is the boundary solid? If so, there is no box above, else, the box
                     * 'above' is the first box
                     * Else, the box above is the next box
                     */
                    xAbove = (i == boxes[0] - 1 ? (sim.getSolid()[0] ? -1 : 0) : i + 1);
                    xBelow = (i == 0 ? (sim.getSolid()[0] ? -1 : boxes[0] - 1) : i - 1);
                    yAbove = (j == boxes[1] - 1 ? (sim.getSolid()[1] ? -1 : 0) : j + 1);
                    yBelow = (j == 0 ? (sim.getSolid()[1] ? -1 : boxes[1] - 1) : j - 1);
                    zAbove = (k == boxes[2] - 1 ? (sim.getSolid()[2] ? -1 : 0) : k + 1);
                    zBelow = (k == 0 ? (sim.getSolid()[2] ? -1 : boxes[2] - 1) : k - 1);

                    if (xAbove != -1) {
                        /* Calculate the quantity of chemical leaving the box in this direction */
                        qxAbove = -kX * (before[xAbove][j][k] - before[i][j][k]);
                        /* Add that quantity to the box above */
                        quantity[xAbove][j][k] += qxAbove;
                        /* Remove it from this box */
                        quantity[i][j][k] -= qxAbove;
                    } else {
                        if (leaky[0]) {
                            /*
                             * Calculate the quantity of chemical leaving the box in this direction (0
                             * outside box if leaky)
                             */
                            qxAbove = normX * leakyRate[0] * before[i][j][k];
                            /* Remove it from this box */
                            quantity[i][j][k] -= qxAbove;
                        }
                    }

                    if (xBelow != -1) {
                        qxBelow = -kX * (before[xBelow][j][k] - before[i][j][k]);
                        quantity[xBelow][j][k] += qxBelow;
                        quantity[i][j][k] -= qxBelow;
                    } else {
                        if (leaky[1]) {
                            qxBelow = normX * leakyRate[1] * before[i][j][k];
                            quantity[i][j][k] -= qxBelow;
                        }
                    }

                    if (yAbove != -1) {
                        qyAbove = -kY * (before[i][yAbove][k] - before[i][j][k]);
                        quantity[i][yAbove][k] += qyAbove;
                        quantity[i][j][k] -= qyAbove;
                    } else {
                        if (leaky[2]) {
                            qyAbove = normY * leakyRate[2] * before[i][j][k];
                            quantity[i][j][k] -= qyAbove;
                        }
                    }

                    if (yBelow != -1) {
                        qyBelow = -kY * (before[i][yBelow][k] - before[i][j][k]);
                        quantity[i][yBelow][k] += qyBelow;
                        quantity[i][j][k] -= qyBelow;
                    } else {
                        if (leaky[3]) {
                            qyBelow = normY * leakyRate[3] * before[i][j][k];
                            quantity[i][j][k] -= qyBelow;
                        }
                    }

                    if (zAbove != -1) {
                        qzAbove = -kZ * (before[i][j][zAbove] - before[i][j][k]);
                        quantity[i][j][zAbove] += qzAbove;
                        quantity[i][j][k] -= qzAbove;
                    } else {
                        if (leaky[4]) {
                            qzAbove = normZ * leakyRate[4] * before[i][j][k];
                            quantity[i][j][k] -= qzAbove;
                        }
                    }

                    if (zBelow != -1) {
                        qzBelow = -kZ * (before[i][j][zBelow] - before[i][j][k]);
                        quantity[i][j][zBelow] += qzBelow;
                        quantity[i][j][k] -= qzBelow;
                    } else {
                        if (leaky[3]) {
                            qzBelow = normZ * leakyRate[5] * before[i][j][k];
                            quantity[i][j][k] -= qzBelow;
                        }
                    }

                }
    }

    public void update() {
        this.diffuse();
    }
}
