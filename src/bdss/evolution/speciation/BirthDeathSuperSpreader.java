package bdss.evolution.speciation;

import beast.base.core.Input;
import beast.base.inference.parameter.RealParameter;
import bdmm.evolution.speciation.BirthDeathMigrationModelUncoloured;
import java.util.ArrayList;
import java.util.Arrays;
import beast.base.evolution.tree.TreeInterface;
import beast.base.evolution.tree.Node;

public class BirthDeathSuperSpreader extends BirthDeathMigrationModelUncoloured {

    public Input<RealParameter> relativeTransmissibility =
            new Input<>("relativeTransmissibility", "Relative transmissibility", (RealParameter) null);

    public Input<RealParameter> totalR0 =
            new Input<>("totalR0", "Total R0", (RealParameter) null);

    public Input<RealParameter> superspreaderFraction =
            new Input<>("superspreaderFraction", "Superspreader fraction", (RealParameter) null);

    @Override
    public void initAndValidate() {

        // Initialize derived parameters if missing
        if (R0.get() == null) {
            System.out.println("Initializing R0...");
            R0.setValue(new RealParameter(new Double[]{0.0, 0.0}), this);
        }

        if (R0AmongDemes.get() == null) {
            System.out.println("Initializing R0AmongDemes...");
            R0AmongDemes.setValue(new RealParameter(new Double[]{0.0, 0.0}), this);
        }

        if (migrationMatrix.get() == null) {
            System.out.println("Initializing migrationMatrix...");
            migrationMatrix.setValue(new RealParameter(new Double[]{0.0, 0.0}), this);
        } else {
            // If already initialized, zero it out safely
            for (int i = 0; i < migrationMatrix.get().getDimension(); i++) {
                migrationMatrix.get().setValue(i, 0.0);
            }
        }
        

        // Auto-generate empty tiptypes if missing
        if (tiptypes.get() != null) {
            if (tiptypes.get().traitsInput.get() == null || tiptypes.get().traitsInput.get().trim().isEmpty()) {
                if (treeInput.get() != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Node node : treeInput.get().getExternalNodes()) {
                        if (node.getID() != null) {
                            sb.append(node.getID()).append("=NOT_SET,"); // assign default type 0
                        } else {
                            System.err.println("Warning: Node without ID detected!");
                        }
                    }
                    if (sb.length() > 0) {
                        sb.setLength(sb.length() - 1); // remove trailing comma
                    }
                    tiptypes.get().traitsInput.setValue(sb.toString(), tiptypes.get());
                    tiptypes.get().initAndValidate();
                } else {
                    System.err.println("Warning: Tree is null when setting tiptypes.");
                }
            }
        }
        

        // Only transform if all required inputs are present
        if (superspreaderFraction.get() != null &&
            relativeTransmissibility.get() != null &&
            totalR0.get() != null) {
            transform();
        }

        super.initAndValidate();
    }

    private void transform() {
        try {
            double c = superspreaderFraction.get().getValue();
            double rho = relativeTransmissibility.get().getValue();
            double r = totalR0.get().getValue();

            double denominator = 1.0 - (1.0 - rho) * (1.0 - c);
            double r11 = c * r / denominator;
            double r12 = (1.0 - c) * r / denominator;
            double r21 = rho * r11;
            double r22 = rho * r12;

            R0.get().setValue(0, r11);
            R0.get().setValue(1, r22);
            R0AmongDemes.get().setValue(0, r12);
            R0AmongDemes.get().setValue(1, r21);

            double left = c * (r11 + r12) + (1.0 - c) * (r21 + r22);
            double right = r11 + r22;

            /*System.out.printf("left %f%n", left);
            System.out.printf("right %f%n", right);
            System.out.printf("rtotal %f%n", r);
            System.out.printf("superspreaderFraction %f%n", c);
            System.out.printf("relativeTransmissibility %f%n", rho);
            System.out.printf("r11 %f%n", r11);
            System.out.printf("r12 %f%n", r12);
            System.out.printf("r21 %f%n", r21);
            System.out.printf("r22 %f%n", r22);
			Double[] r0vals = R0.get().getValues();
			System.out.print("R0: ");
			for (double v : r0vals) {
			System.out.printf("%f ", v);
			}
			System.out.println();
			Double[] r0offvals = R0AmongDemes.get().getValues();
			System.out.print("R0AmongDemes: ");
			for (double v : r0offvals) {
			System.out.printf("%f ", v);
			}
			System.out.println();*/
        } catch (Exception e) {
            System.err.println("BirthDeathSuperSpreader transform error: " + e.getMessage());
        }
    }

    @Override
    protected Double updateRates() {
        transform(); // Safe because guarded above
        return super.updateRates();
    }
}
