package bdss.evolution.speciation;
import beast.base.core.Input;
import beast.base.inference.parameter.RealParameter;
import bdmm.evolution.speciation.BirthDeathMigrationModelUncoloured;
import java.util.ArrayList;
import java.util.Arrays;

public class BirthDeathSuperSpreader extends BirthDeathMigrationModelUncoloured{
	
	static {
        System.err.println(">>> BirthDeathSuperSpreader class loaded.");
    }
	
	public Input<RealParameter> relative_transmissibility =
				new Input<>("relative_transmissibility", "relative transmissibility", (RealParameter) null);

	public Input<RealParameter> total_r0 =
				new Input<>("total_r0", "total R0", (RealParameter) null);	

	public Input<RealParameter> superspreader_fraction =
				new Input<>("superspreader_fraction", "superspreader fraction", (RealParameter) null);	
				
	@Override
	public void initAndValidate() {
		System.out.println();
		var starting_val_R0 = new RealParameter(new Double[]{0.0,0.0});
		var starting_val_R0AmongDemes = new RealParameter(new Double[]{0.0,0.0});
		R0 = new Input<RealParameter>("R0", "The basic reproduction number", starting_val_R0);
		R0AmongDemes = new Input<RealParameter>("R0AmongDemes", "The basic reproduction number", starting_val_R0AmongDemes);
		transform();
		super.initAndValidate();
		/*
		System.out.println("Among demes");
		for(var r0 : R0AmongDemes.get().getValues()){
			System.out.println(r0);
		}
		System.out.println("R0s");	
		for(var r0 : R0.get().getValues()){
			System.out.println(r0);
		}*/
	}
	private void transform(){
		var c = superspreader_fraction.get().getValue();
	 	var rho = relative_transmissibility.get().getValue();
		var r = total_r0.get().getValue();
		var denominator = 1. - (1. - rho) * (1. - c);
		var r11 = c * r / denominator;
		var r12 = (1. - c) * r / denominator;
		var r21 = rho * r11;
		var r22 = rho * r12;
		R0.get().setValue(0, r11);
		R0.get().setValue(1, r22);
		R0AmongDemes.get().setValue(0, r12);
		R0AmongDemes.get().setValue(1, r21);
		var left = c * (r11 + r12) + (1. - c) * (r21 + r22);
		var right = r11 + r22;
		System.out.println(String.format("left %f", left));
		System.out.println(String.format("right %f", right));
		System.out.println(String.format("rtotal %f", r));
		System.out.println(String.format("superspreader_fraction %f", c));
		System.out.println(String.format("relative_transmissibility %f", rho));
		System.out.println(String.format("r11 %f", r11));
		System.out.println(String.format("r12 %f", r12));
		System.out.println(String.format("r21 %f", r21));
		System.out.println(String.format("r22 %f", r22));
	}
	@Override
	protected Double updateRates(){
		transform();
		var res = super.updateRates();
		/*System.out.print("bij ");
		for(var value : b_ij){
			System.out.print(String.format("%f ", value));
		}
		System.out.println();*/
		return res;
	}
}