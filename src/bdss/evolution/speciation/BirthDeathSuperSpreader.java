package bdss.evolution.speciation;

import beast.base.core.Input;
import beast.base.inference.parameter.RealParameter;
import bdmm.evolution.speciation.BirthDeathMigrationModelUncoloured;
import java.util.ArrayList;
import java.util.Arrays;
import beast.base.evolution.tree.TreeInterface;
import beast.base.evolution.tree.Node;

public class BirthDeathSuperSpreader extends BirthDeathMigrationModelUncoloured {

    public Input<RealParameter> relative_transmissibility =
            new Input<>("relative_transmissibility", "Relative transmissibility", (RealParameter) null);

    public Input<RealParameter> total_r0 =
            new Input<>("total_r0", "Total R0", (RealParameter) null);

    public Input<RealParameter> superspreader_fraction =
            new Input<>("superspreader_fraction", "Superspreader fraction", (RealParameter) null);

    @Override
    public void initAndValidate() {
		// !!! Apparently I can't overwrite inherited inputs - it breaks BEAUti (but not BEAST) //
        // Initialize inherited fields that must not be null
        /*R0 = new Input<>("R0", "The basic reproduction number", new RealParameter(new Double[]{0.0, 0.0}));
        R0AmongDemes = new Input<>("R0AmongDemes", "Among-deme R0", new RealParameter(new Double[]{0.0, 0.0}));
        migrationMatrix = new Input<>("migrationMatrix", "Migration matrix", new RealParameter(new Double[]{0.0, 0.0}));

        // Auto-generate empty tiptypes if missing
        if (tiptypes.get() != null && (tiptypes.get().traitsInput.get() == null || tiptypes.get().traitsInput.get().trim().isEmpty())) {
            StringBuilder sb = new StringBuilder();
            TreeInterface tree = treeInput.get();
            for (Node node : tree.getExternalNodes()) {
                sb.append(node.getID()).append("=NOT_SET,");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
            tiptypes.get().traitsInput.setValue(sb.toString(), tiptypes.get());
            tiptypes.get().initAndValidate();
        }*/

        // Only transform if all required inputs are present
        if (superspreader_fraction.get() != null &&
            relative_transmissibility.get() != null &&
            total_r0.get() != null) {
            transform();
        }

        super.initAndValidate();
    }

    private void transform() {
        try {
            double c = superspreader_fraction.get().getValue();
            double rho = relative_transmissibility.get().getValue();
            double r = total_r0.get().getValue();

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

            System.out.printf("left %f%n", left);
            System.out.printf("right %f%n", right);
            System.out.printf("rtotal %f%n", r);
            System.out.printf("superspreader_fraction %f%n", c);
            System.out.printf("relative_transmissibility %f%n", rho);
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
			System.out.println();
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
