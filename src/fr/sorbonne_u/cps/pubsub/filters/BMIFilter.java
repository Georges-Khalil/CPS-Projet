package fr.sorbonne_u.cps.pubsub.filters;

import java.io.Serializable;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.MultiValuesFilterI;

public class BMIFilter implements MultiValuesFilterI {
    private static final long serialVersionUID = 1L;
    
    private final double threshold;
    private final boolean acceptAbove;
    
    public BMIFilter(double threshold, boolean acceptAbove) {
        this.threshold = threshold;
        this.acceptAbove = acceptAbove;
    }
    
    @Override
    public String[] getNames() {
        return new String[] {"weight", "height"};
    }
    
    @Override
    public boolean match(Serializable... values) {
        assert values != null && values.length == 2;
        try {
            double weight = ((Number) values[0]).doubleValue();
            double height = ((Number) values[1]).doubleValue();
            double bmi = weight / (height * height);
            if (acceptAbove) {
                return bmi >= threshold;
            } else {
                return bmi < threshold;
            }
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }
}