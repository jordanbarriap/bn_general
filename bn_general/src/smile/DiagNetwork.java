// DiagNetwork.java

package smile;

public class DiagNetwork extends Wrapper {
    public DiagNetwork(Network net) {
        super(net);
        this.net = net;
    }
	
    public DiagResults update() { return updateNative(multiFaultAlgorithm); }
    public native void restart();

    public int getMultiFaultAlgorithm() { return multiFaultAlgorithm; }
    public void setMultiFaultAlgorithm(int type) { multiFaultAlgorithm = type; }
	
    public native boolean getDSep();
    public native void setDSep(boolean value);
	
    public native void setDetailedEntropyEnabled(boolean enable);
    public native boolean isDetailedEntropyEnabled();

    public native void setPursuedFault(int faultIndex);
    public native void setPursuedFaults(int[] faultIndices);
    public native int getPursuedFault();
    public native int[] getPursuedFaults();
	
    public native int getFaultCount();
    public native int getUnperformedTestCount();
	
    public native void instantiateObservation(int nodeHandle, int outcomeIndex);
    public native void instantiateObservation(int nodeHandle, String outcomeId);
    public native void instantiateObservation(String nodeId, String outcomeId);
    public native void instantiateObservation(String nodeId, int outcomeIndex);
	
    public native void releaseObservation(int nodeHandle);
    public native void releaseObservation(String nodeId);
	
    public native int[] getUnperformedObservations();
    public native String[] getUnperformedObservationIds();
	
    public native boolean mandatoriesInstantiated();
    public native int findMostLikelyFault();
	
    public native int getFaultIndex(int nodeHandle, int outcomeIndex);
    public native int getFaultIndex(int nodeHandle, String outcomeId);
    public native int getFaultIndex(String nodeId, String outcomeId);
    public native int getFaultIndex(String nodeId, int outcomeIndex);
	
    public native int getFaultNode(int faultIndex);
    public native String getFaultNodeId(int faultIndex);
    public native int getFaultOutcome(int faultIndex);
    public native String getFaultOutcomeId(int faultIndex);
	
    public native FaultInfo getFault(int faultIndex);
	
    private native DiagResults updateNative(int multiFaultAlgorithm);
	
    protected native long createNative(Object param);
    protected native void deleteNative(long nativePtr);

    private Network net;

    private int multiFaultAlgorithm = MultiFaultAlgorithmType.INDEPENDENCE_AT_LEAST_ONE;

    private final static int DSL_DIAG_MARGINAL = 1;
    private final static int DSL_DIAG_INDEPENDENCE = 2;
    private final static int DSL_DIAG_DEPENDENCE = 4;
    private final static int DSL_DIAG_PURSUE_ATLEAST_ONE_COMB = 8;
    private final static int DSL_DIAG_PURSUE_ONLY_ONE_COMB = 16;
    private final static int DSL_DIAG_PURSUE_ONLY_ALL_COMB = 32;
    private final static int DSL_DIAG_MARGINAL_STRENGTH1 = 64;
    private final static int DSL_DIAG_MARGINAL_STRENGTH2 = 128;
    private final static double DSL_NOT_RELEVANT = Double.MIN_VALUE * 5;
    private final static double DSL_NOT_AVAILABLE = Double.MIN_VALUE * 4;

    public final static double NOT_RELEVANT = DSL_NOT_RELEVANT;
    public final static double NOT_AVAILABLE = DSL_NOT_AVAILABLE;

    @Deprecated
    public final static double NotRelevant = NOT_RELEVANT;
    @Deprecated
    public final static double NotAvailable = NOT_AVAILABLE;
		
    public static class MultiFaultAlgorithmType {
        public static final int INDEPENDENCE_AT_LEAST_ONE = DSL_DIAG_INDEPENDENCE | DSL_DIAG_PURSUE_ATLEAST_ONE_COMB;
        public static final int INDEPENDENCE_ONLY_ONE = DSL_DIAG_INDEPENDENCE | DSL_DIAG_PURSUE_ONLY_ONE_COMB;
        public static final int INDEPENDENCE_ONLY_ALL = DSL_DIAG_INDEPENDENCE | DSL_DIAG_PURSUE_ONLY_ALL_COMB;
		
        public static final int DEPENDENCE_AT_LEAST_ONE = DSL_DIAG_DEPENDENCE | DSL_DIAG_PURSUE_ATLEAST_ONE_COMB;
        public static final int DEPENDENCE_ONLY_ONE = DSL_DIAG_DEPENDENCE | DSL_DIAG_PURSUE_ONLY_ONE_COMB;
        public static final int DEPENDENCE_ONLY_ALL = DSL_DIAG_DEPENDENCE | DSL_DIAG_PURSUE_ONLY_ALL_COMB;

        public static final int MARGINAL_1 = DSL_DIAG_MARGINAL | DSL_DIAG_MARGINAL_STRENGTH1;
        public static final int MARGINAL_2 = DSL_DIAG_MARGINAL | DSL_DIAG_MARGINAL_STRENGTH2;

        @Deprecated
        public static final int IndependenceAtLeastOne = INDEPENDENCE_AT_LEAST_ONE;
        @Deprecated
        public static final int IndependenceOnlyOne = INDEPENDENCE_ONLY_ONE;
        @Deprecated
        public static final int IndependenceOnlyAll = INDEPENDENCE_ONLY_ALL;
        @Deprecated
        public static final int DependenceAtLeastOne = DEPENDENCE_AT_LEAST_ONE;
        @Deprecated
        public static final int DependenceOnlyOne = DEPENDENCE_ONLY_ONE;
        @Deprecated
        public static final int DependenceOnlyAll = DEPENDENCE_ONLY_ALL;
        @Deprecated
        public static final int Marginal1 = MARGINAL_1;
        @Deprecated
        public static final int Marginal2 = MARGINAL_2;
    }

}
