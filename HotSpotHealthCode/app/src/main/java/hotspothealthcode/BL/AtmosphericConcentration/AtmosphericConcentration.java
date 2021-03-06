package hotspothealthcode.BL.AtmosphericConcentration;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;

import java.util.ArrayList;

import hotspothealthcode.BL.AtmosphericConcentration.Functions.DepletionFactorFunction;
import hotspothealthcode.BL.AtmosphericConcentration.results.ConcentrationPoint;
import hotspothealthcode.BL.AtmosphericConcentration.results.ConcentrationResult;
import hotspothealthcode.BL.AtmosphericConcentration.results.OutputResult;
import hotspothealthcode.BL.AtmosphericConcentration.results.ResultField;

/**
 * Created by Giladl on 09/01/2016.
 */
public abstract class AtmosphericConcentration
{
    //region Static constants

    protected static final double G = 9.8; // gravitational acceleration (9.8 m/s2)

    //endregion

    //region Data Members

    protected LatLng location;
    protected PasquillStability pasquillStability;
    protected MeteorologicalConditions meteorologicalCondition;
    protected double referenceHeight;
    protected double windSpeedAtReferenceHeight;
    protected double windDirection;
    protected double surfaceRoughnessHeight; // cm
    protected int sampleTime;
    protected TerrainType terrainType;
    protected double sourceTerm;
    protected ArrayList<ConcentrationPoint> concentrationPoints;
    protected double dfx = 0.025;
    protected double effectiveReleaseHeight;

    private Double dy;
    private Double dz;

    //endregion

    //region C'tors

    //endregion

    //region setters

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setDfx(double Dfx) {
        this.dfx = Dfx;
    }

    public void setPasquillStability(PasquillStability pasquillStability) {
        this.pasquillStability = pasquillStability;
    }

    public void setMeteorologicalCondition(MeteorologicalConditions meteorologicalCondition) {
        this.meteorologicalCondition = meteorologicalCondition;
    }

    public void setReferenceHeight(double referenceHeight) {
        this.referenceHeight = referenceHeight;
    }

    public void setWindSpeedAtReferenceHeight(double windSpeedAtReferenceHeight) {
        this.windSpeedAtReferenceHeight = windSpeedAtReferenceHeight;
    }

    public void setWindDirection(double windDirection) {
        this.windDirection = windDirection;
    }

    public void setSurfaceRoughnessHeight(double surfaceRoughnessHeight) {
        this.surfaceRoughnessHeight = surfaceRoughnessHeight;
    }

    public void setSampleTime(int sampleTime) {
        this.sampleTime = sampleTime;
    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    public void setSourceTerm(double sourceTerm) {
        this.sourceTerm = sourceTerm;
    }

    public void setConcentrationPoints(ArrayList<ConcentrationPoint> concentrationPoints) {
        this.concentrationPoints = concentrationPoints;
    }

    //endregion

    //region Static Methods

    protected static double convertToKelvin(double temp)
    {
        return temp + 273.15;
    }

    //endregion

    //region Gussian Equation

    /**
     * The method calculate the Gussian Equation
     * @param sourceTerm - The source term (Ci).
     * @param sigmaY - Standard deviation of the integrated concentration distribution in the crosswind direction (m).
     * @param sigmaZ - Standard deviation of the integrated concentration distribution in the vertical direction (m).
     * @param effectiveReleaseHeight - Effective release height (m)
     * @param windSpeedAtHeight - Wind speed (m/s), at height effective release height (m)
     * @param point - Point to calculate the gussian equation (x, y , z)
     * @return The time-integrated atmospheric
               concentration of a gas or an aerosol at any point in space
     */
    private double calcGussianEquation(double sourceTerm,
                                         double sigmaY,
                                         double sigmaZ,
                                         double effectiveReleaseHeight,
                                         double windSpeedAtHeight,
                                         ConcentrationPoint point)
    {
        double retVal;

        retVal = sourceTerm / (2 * Math.PI * sigmaY * sigmaZ * windSpeedAtHeight);

        retVal *= Math.exp(-0.5 * Math.pow(point.getY() / sigmaY, 2));
        retVal *= (Math.exp(-0.5 * Math.pow((point.getZ() - effectiveReleaseHeight) / sigmaZ, 2)) +
                Math.exp(-0.5 * Math.pow((point.getZ() + effectiveReleaseHeight) / sigmaZ, 2)));
        retVal *= Math.exp((-point.getX()) / windSpeedAtHeight) * this.dfx;

        return retVal;
    }

    /**
     * The method calculate the Gussian Equation
     * @param sourceTerm - The source term (Ci).
     * @param sigmaY - Standard deviation of the integrated concentration distribution in the crosswind direction (m).
     * @param sigmaZ - Standard deviation of the integrated concentration distribution in the vertical direction (m).
     * @param effectiveReleaseHeight - Effective release height (m)
     * @param windSpeedAtHeight - Wind speed (m/s), at height effective release height (m)
     * @param inversionHeight - The inversion layer height (m)
     * @param point - Point to calculate the gussian equation (x, y , z)
     * @return The time-integrated atmospheric
                concentration of a gas or an aerosol at any point in space
     */
    private double calcGussianEquationWithInversionLayer(double sourceTerm,
                                                           double sigmaY,
                                                           double sigmaZ,
                                                           double effectiveReleaseHeight,
                                                           double windSpeedAtHeight,
                                                           int inversionHeight,
                                                           ConcentrationPoint point){
        // To avoid the sharp transition between the two above equations, the transition into the
        // Inversion layer equation begins when z equals 70% of L and is complete when z equals
        // L. Between these two values, the two equations are linearly interpolated.

        double retVal;

        retVal = sourceTerm / (Math.sqrt(2 * Math.PI) * sigmaY * inversionHeight * windSpeedAtHeight);

        retVal *= Math.exp(-0.5 * Math.pow(point.getY() / sigmaY, 2));
        retVal *= Math.exp((-point.getX()) / windSpeedAtHeight) * this.dfx;

        return retVal;
    }

    //endregion

    //TODO: MAYBE DELETE THIS
    //region Depletion factor (DF(x))

    private double calcDepletionFactor(double downWindDistance)
    {
        // TODO: MAYBE DELETE THIS
        UnivariateIntegrator integrator = new TrapezoidIntegrator(1e-4, 1e-8, 3, 32);

        DepletionFactorFunction function = new DepletionFactorFunction();

        double result = integrator.integrate(20000, function, 0, downWindDistance);

        return result;
    }

    //endregion

    //region Wind Speed

    /**
     * Calculate the standard terrain wind expo factor
     * @return the standard terrain wind expo factor
     */
    private double getStandardTerrainWindExpoFactor() {
        double p = 0;

        switch (this.pasquillStability.getStabilityType()) {
            case TYPE_A: {
                p = 0.07;
                break;
            }
            case TYPE_B: {
                p = 0.07;
                break;
            }
            case TYPE_C: {
                p = 0.10;
                break;
            }
            case TYPE_D: {
                p = 0.15;
                break;
            }
            case TYPE_E: {
                p = 0.35;
                break;
            }
            case TYPE_F: {
                p = 0.55;
                break;
            }
        }

        return p;
    }

    /**
     * Calculate the city terrain wind expo factor
     * @return the city terrain wind expo factor
     */
    protected double getCityTerrainWindExpoFactor() {
        double p = 0;

        switch (this.pasquillStability.getStabilityType()) {
            case TYPE_A: {
                p = 0.15;
                break;
            }
            case TYPE_B: {
                p = 0.15;
                break;
            }
            case TYPE_C: {
                p = 0.20;
                break;
            }
            case TYPE_D: {
                p = 0.25;
                break;
            }
            case TYPE_E: {
                p = 0.40;
                break;
            }
            case TYPE_F: {
                p = 0.60;
                break;
            }
        }

        return p;
    }

    /**
     * Calculate the wind speed in the given height
     * @param terrainType - The terrain type
     * @param h - The height
     * @return the wind speed in the given height
     */
    protected double calcWindSpeed(TerrainType terrainType, double h)
    {
        double p = 0;

        switch (terrainType)
        {
            case STANDARD_TERRAIN: {
                p = getStandardTerrainWindExpoFactor();

                break;
            }

            case CITY_TERRAIN: {
                p = getCityTerrainWindExpoFactor();

                break;
            }
        }

        return this.windSpeedAtReferenceHeight * Math.pow((h / this.referenceHeight), p);
    }

    //endregion

    //region Deviation Calculation

    protected abstract double calcDy();

    protected abstract double calcDz();

    protected abstract double calcCrossWindRadios(double sigmaY);

    protected abstract double calcPlumeTop(double sigmaZ, double effectiveReleaseHeight);

    protected abstract double calcPlumeBottom(double sigmaZ, double effectiveReleaseHeight);

    protected double calcVirtualSourceDistanceForSigmaY(TerrainType terrainType,
                                                         double guess,
                                                         double sigmaY)
    {
        switch (terrainType)
        {
            case STANDARD_TERRAIN: {
                return this.calcVirtualSourceDistanceForSigmaYStandardTerrain(guess, sigmaY);
            }

            case CITY_TERRAIN: {
                return this.calcVirtualSourceDistanceForSigmaYCityTerrain(guess, sigmaY);
            }
        }

        return 0.0;
    }

    private double calcVirtualSourceDistanceForSigmaYStandardTerrain(double guess,
                                                                     double sigmaY)
    {
        double parameter = 0;

        // Get the parameter by the stability type
        switch (this.pasquillStability.getStabilityType()){
            case TYPE_A: {  parameter = 0.22; break; }
            case TYPE_B: {  parameter = 0.16; break; }
            case TYPE_C: {  parameter = 0.11; break; }
            case TYPE_D: {  parameter = 0.08; break; }
            case TYPE_E: {  parameter = 0.06; break; }
            case TYPE_F: {  parameter = 0.04; break; }
        }

        return (sigmaY * Math.sqrt(1 + 0.0001 * guess)) / (parameter);
    }

    private double calcVirtualSourceDistanceForSigmaYCityTerrain(double guess,
                                                                 double sigmaY)
    {
        double parameter = 0;

        // Get the parameter by the stability type
        switch (this.pasquillStability.getStabilityType()){
            case TYPE_A: { }
            case TYPE_B: {  parameter = 0.32; break; }
            case TYPE_C: {  parameter = 0.22; break; }
            case TYPE_D: {  parameter = 0.16; break; }
            case TYPE_E: {  parameter = 0.11; break; }
            case TYPE_F: {  parameter = 0.11; break; }
        }

        return (sigmaY * Math.sqrt(1 + 0.0004 * guess)) / (parameter);
    }

    protected double calcVirtualSourceDistanceForSigmaZ(TerrainType terrainType,
                                                        double guess,
                                                        double sigmaZ)
    {
        switch (terrainType)
        {
            case STANDARD_TERRAIN: {
                return this.calcVirtualSourceDistanceForSigmaZStandardTerrain(guess, sigmaZ);
            }

            case CITY_TERRAIN: {
                return this.calcVirtualSourceDistanceForSigmaZCityTerrain(guess, sigmaZ);
            }
        }

        return 0.0;
    }

    private double calcVirtualSourceDistanceForSigmaZStandardTerrain(double guess,
                                                                     double sigmaZ)
    {
        double Aparameter = 0;
        double Bparameter = 1;

        // Get the parameter by the stability type
        switch (this.pasquillStability.getStabilityType()){
            case TYPE_A: { Aparameter = 0.20; Bparameter = 1; break; }
            case TYPE_B: { Aparameter = 0.12; Bparameter = 1; break; }
            case TYPE_C: { Aparameter = 0.080; Bparameter = Math.sqrt(1 + (0.0002 * guess)); break; }
            case TYPE_D: { Aparameter = 0.060; Bparameter = Math.sqrt(1 + (0.0015 * guess)); break; }
            case TYPE_E: { Aparameter = 0.030; Bparameter = 1 + (0.0003 * guess); break; }
            case TYPE_F: { Aparameter = 0.016; Bparameter = 1 + (0.0003 * guess); break; }
        }

        return (sigmaZ * Bparameter) / (Aparameter);
    }

    private double calcVirtualSourceDistanceForSigmaZCityTerrain(double guess,
                                                                 double sigmaZ)
    {
        double Aparameter = 0;
        double Bparameter = 1;

        // Get the parameter by the stability type
        switch (this.pasquillStability.getStabilityType()){
            case TYPE_A: { }
            case TYPE_B: { Aparameter = 0.24 * Math.sqrt(1 + (0.001 * guess)); Bparameter = 1; break; }
            case TYPE_C: { Aparameter = 0.20; Bparameter = 1; break; }
            case TYPE_D: { Aparameter = 0.14; Bparameter = Math.sqrt(1 + (0.0003 * guess)); break; }
            case TYPE_E: { Aparameter = 0.08; Bparameter = Math.sqrt(1 + (0.0015 * guess)); break; }
            case TYPE_F: { Aparameter = 0.08; Bparameter = Math.sqrt(1 + (0.0015 * guess)); break; }
        }

        return (sigmaZ * Bparameter) / (Aparameter);
    }

    /**
     * Calculate the deviation (sigmaY and sigmaZ)
     * @param terrainType - The terrain type
     * @param downWindOffset - The downwind axis (x-axis) offset (m)
     * @return List containing sigmaY at 0 index and sigmaZ at 1 index
     */
    private ArrayList<Double> calcSigmaYZ(TerrainType terrainType,
                                            double downWindOffset)
    {
        double sigmaY;
        double sigmaZ;
        ArrayList<Double> lst = new ArrayList<>();

        switch (terrainType)
        {
            case STANDARD_TERRAIN: {
                lst = this.calcStandardTerrainSigmaYZ(downWindOffset);

                break;
            }

            case CITY_TERRAIN: {
                lst = this.calcCityTerrainSigmaYZ(downWindOffset);

                break;
            }
        }

        sigmaY = lst.get(0);
        sigmaZ = lst.get(1);

        // Add sample time to calculation (in case the sample time is the default 10 minutes
        // the sigmaY will remain the same)
        sigmaY = sigmaY * Math.pow((this.sampleTime / 10), 0.2);

        // Add Surface roughness height to calculation (z0)
        sigmaZ = sigmaZ * Math.pow((this.surfaceRoughnessHeight / 3), 0.2);

        lst.clear();

        lst.add(sigmaY);
        lst.add(sigmaZ);

        return lst;
    }

    /**
     * Calculate with standard terrain
     * @param downWindOffset - The downwind axis (x-axis) offset (m)
     * @return List containing sigmaY at 0 index and sigmaZ at 1 index
     */
    private ArrayList<Double> calcStandardTerrainSigmaYZ(double downWindOffset){

        double parameter = 0;
        double Aparameter = 0;
        double Bparameter = 1;
        double sigmaY;
        double sigmaZ;
        double downWindDistanceSigmaY = downWindOffset + this.dy;
        double downWindDistanceSigmaZ = downWindOffset + this.dz;
        ArrayList<Double> lst = new ArrayList<>();

        switch (this.pasquillStability.getStabilityType()){
            case TYPE_A: {  parameter = 0.22; Aparameter = 0.20; Bparameter = 1; break; }
            case TYPE_B: {  parameter = 0.16; Aparameter = 0.12; Bparameter = 1; break; }
            case TYPE_C: {  parameter = 0.11; Aparameter = 0.080; Bparameter = Math.sqrt(1 + (0.0002 * downWindDistanceSigmaZ)); break; }
            case TYPE_D: {  parameter = 0.08; Aparameter = 0.060; Bparameter = Math.sqrt(1 + (0.0015 * downWindDistanceSigmaZ)); break; }
            case TYPE_E: {  parameter = 0.06; Aparameter = 0.030; Bparameter = 1 + (0.0003 * downWindDistanceSigmaZ); break; }
            case TYPE_F: {  parameter = 0.04; Aparameter = 0.016; Bparameter = 1 + (0.0003 * downWindDistanceSigmaZ); break; }
        }

        sigmaY = ((parameter * downWindDistanceSigmaY) / Math.sqrt(1 + 0.0001 * downWindDistanceSigmaY));
        sigmaZ = ((Aparameter * downWindDistanceSigmaZ) / Bparameter);

        lst.add(sigmaY);
        lst.add(sigmaZ);

        return lst;
    }

    /**
     * Calculate with city terrain
     * @param downWindOffset - The downwind axis (x-axis) offset (m)
     * @return List containing sigmaY at 0 index and sigmaZ at 1 index
     */
    private ArrayList<Double> calcCityTerrainSigmaYZ(double downWindOffset){

        double parameter = 0;
        double Aparameter = 0;
        double Bparameter = 1;
        double sigmaY;
        double sigmaZ;
        double downWindDistanceSigmaY = downWindOffset + this.dy;
        double downWindDistanceSigmaZ = downWindOffset + this.dz;
        ArrayList<Double> lst = new ArrayList<>();

        switch (this.pasquillStability.getStabilityType()){
            case TYPE_A: { }
            case TYPE_B: {  parameter = 0.32; Aparameter = 0.24 * Math.sqrt(1 + (0.001 * downWindDistanceSigmaZ)); Bparameter = 1; break; }
            case TYPE_C: {  parameter = 0.22; Aparameter = 0.20; Bparameter = 1; break; }
            case TYPE_D: {  parameter = 0.16; Aparameter = 0.14; Bparameter = Math.sqrt(1 + (0.0003 * downWindDistanceSigmaZ)); break; }
            case TYPE_E: {  parameter = 0.11; Aparameter = 0.08; Bparameter = Math.sqrt(1 + (0.0015 * downWindDistanceSigmaZ)); break; }
            case TYPE_F: {  parameter = 0.11; Aparameter = 0.08; Bparameter = Math.sqrt(1 + (0.0015 * downWindDistanceSigmaZ)); break; }
        }

        sigmaY = ((parameter * downWindDistanceSigmaY) / Math.sqrt(1 + 0.0004 * downWindDistanceSigmaY));
        sigmaZ = ((Aparameter * downWindDistanceSigmaZ) / Bparameter);

        lst.add(sigmaY);
        lst.add(sigmaZ);

        return lst;
    }

    //endregion

    //region Result Methods

    protected OutputResult getOutputResult(double effectiveReleaseHeight,
                                           double windSpeed)
    {
        ArrayList<ConcentrationResult> results = new ArrayList<ConcentrationResult>();

        OutputResult outputResult = OutputResult.newInstance();

        this.dy = this.calcDy();
        this.dz = this.calcDz();

        // Calculate the concentration at points
        for (ConcentrationPoint point: this.concentrationPoints)
        {
            // Calculate sigmaY and sigmaZ
            ArrayList<Double> lst = this.calcSigmaYZ(this.terrainType, point.getX());

            double sigmaY = lst.get(0);
            double sigmaZ = lst.get(1);

            // (x, y, z, H)
            this.addResult(this.sourceTerm,
                    sigmaY,
                    sigmaZ,
                    effectiveReleaseHeight,
                    windSpeed,
                    point,
                    this.calcCrossWindRadios(sigmaY),
                    this.calcPlumeTop(sigmaZ, effectiveReleaseHeight),
                    this.calcPlumeBottom(sigmaZ, effectiveReleaseHeight),
                    results);
        }

        outputResult.setResults(results);

        outputResult.addValue(ResultField.DOWN_WIND_VIRTUAL_SOURCE, this.dy);
        outputResult.addValue(ResultField.LOCATION, this.location);
        outputResult.addValue(ResultField.WIND_SPEED, this.windSpeedAtReferenceHeight);
        outputResult.addValue(ResultField.WIND_DIRECTION, this.windDirection);
        outputResult.addValue(ResultField.STABILITY_TYPE, this.pasquillStability.getStabilityType());

        return outputResult;
    }

    private void addResult(double sourceTerm,
                           double sigmaY,
                           double sigmaZ,
                           double effectiveReleaseHeight,
                           double windSpeed,
                           ConcentrationPoint point,
                           double crossWindRadios,
                           double plumeTop,
                           double plumeBottom,
                           ArrayList<ConcentrationResult> results)
    {
        double concentration = this.calcGussianEquation(sourceTerm,
                                                        sigmaY,
                                                        sigmaZ,
                                                        effectiveReleaseHeight,
                                                        windSpeed,
                                                        point);

        results.add(new ConcentrationResult(point,
                                            concentration,
                                            (int) (point.getX() / windSpeed),
                                            crossWindRadios,
                                            plumeTop,
                                            plumeBottom));
    }

    public abstract OutputResult calcAtmosphericConcentration();

    //endregion
}
