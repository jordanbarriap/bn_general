// ObservationInfo.java

package smile;

import java.util.Comparator;

public class ObservationInfo {
    public ObservationInfo(int node, double entropy, double cost, double infoGain, double[] observationPriors, double[] faultPosteriors) {
        this.node = node;
        this.entropy = entropy;
        this.cost = cost;
        this.infoGain = infoGain;
        this.observationPriors = observationPriors;
        this.faultPosteriors = faultPosteriors;
    }
	
    public int node;
    public double entropy;
    public double cost;
    public double infoGain;

    public double[] observationPriors;
    public double[] faultPosteriors;
}
