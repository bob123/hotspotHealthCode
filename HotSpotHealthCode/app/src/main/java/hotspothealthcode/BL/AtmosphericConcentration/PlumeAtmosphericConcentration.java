package hotspothealthcode.BL.AtmosphericConcentration;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;

import java.util.ArrayList;

import hotspothealthcode.BL.AtmosphericConcentration.Functions.BouyantPlumeRiseStableFunc;
import hotspothealthcode.BL.AtmosphericConcentration.Functions.BouyantPlumeRiseUnstableFunc;
import hotspothealthcode.BL.AtmosphericConcentration.Functions.MomentumPlumeRiseFunc;
import hotspothealthcode.BL.AtmosphericConcentration.results.ConcentrationResult;
import hotspothealthcode.BL.AtmosphericConcentration.results.OutputResult;
import hotspothealthcode.BL.AtmosphericConcentration.results.ResultField;

/**
 * Created by Giladl on 09/01/2016.
 */
public class PlumeAtmosphericConcentration extends AtmosphericConcentration
{
    //region Data Members

    protected double physicalStackHeight;
    protected double stackExitVelocity;
    protected double stackRadius;
    protected double airTemp;
    protected double stackTemp;
    protected double heatEmission;
    protected boolean calcMomentum;

    //endregion

    //region C'tors

    //endregion

    //region setters

    public void setPhysicalStackHeight(double physicalStackHeight) {
        this.physicalStackHeight = physicalStackHeight;
    }

    public void setStackExitVelocity(double stackExitVelocity) {
        this.stackExitVelocity = stackExitVelocity;
    }

    public void setStackRadius(double stackRadius) {
        this.stackRadius = stackRadius;
    }

    public void setAirTemp(double airTemp) {
        this.airTemp = airTemp;
    }

    public void setStackTemp(double stackTemp) {
        this.stackTemp = stackTemp;
    }

    public void setHeatEmission(double heatEmission) {
        this.heatEmission = heatEmission;
    }

    public void setCalcMomentum(boolean calcMomentum) {
        this.calcMomentum = calcMomentum;
    }

    public void setEffectiveReleaseHeight(double effectiveReleaseHeight) {
        this.effectiveReleaseHeight = effectiveReleaseHeight;
    }


    //endregion

    //region Effective Release Height

    /**
     * The method calculate the Buoyancy Flux
     * @param v - stack exit velocity (m/s)
     * @param r - stack radius (m)
     * @param ta - ambient air temperature (deg K)
     * @param ts - stack effluent temperature (deg K)
     * @return The Buoyancy Flux
     */
    private double calcBuoyancyFlux(double v,
                                    double r,
                                    double ta,
                                    double ts)
    {
        return ((r * r) * AtmosphericConcentration.G * v) * (1 - (ta / ts));
    }

    /**
     * The method calculate the Bouyant Effective Release Height
     * @param h - the physical height (m)
     * @param ta - ambient air temperature (deg K)
     * @param buoyancyFlux - the buoyancy Flux
     * @return The Bouyant Effective Release Height
     */
    private double calcBouyantEffectiveReleaseHeight(double h,
                                                     double ta,
                                                     double buoyancyFlux)
    {
        double effectiveReleaseHeight; // H
        double windSpeedAtEffectiveHeight;
        double windSpeedAtStackHeight;
        double x;
        double uh; // Wind speed at physical height
        double s;

        NewtonRaphsonSolver solver = new NewtonRaphsonSolver();

        double p = this.getCityTerrainWindExpoFactor();

        if (this.pasquillStability.getStabilityType() == PasquillStabilityType.TYPE_A ||
            this.pasquillStability.getStabilityType() == PasquillStabilityType.TYPE_B ||
            this.pasquillStability.getStabilityType() == PasquillStabilityType.TYPE_C ||
            this.pasquillStability.getStabilityType() == PasquillStabilityType.TYPE_D) {
            if (buoyancyFlux >= 55) {
                x = 119 * Math.pow(buoyancyFlux, 0.4);
            } else {
                x = 49 * Math.pow(buoyancyFlux, 0.625);
            }



            // Create bouyant plume rise function to solve it numericly
            BouyantPlumeRiseUnstableFunc bouyantPlumeRiseUnstableFunc = new BouyantPlumeRiseUnstableFunc(buoyancyFlux,
                                                                                                         this.physicalStackHeight,
                                                                                                         this.windSpeedAtReferenceHeight,
                                                                                                         this.referenceHeight,
                                                                                                         p,
                                                                                                         x);

            effectiveReleaseHeight = solver.solve(100, bouyantPlumeRiseUnstableFunc, h, 100);

            /*effectiveReleaseHeight = h;

            uh = this.calcWindSpeed(this.terrainType, h);

            effectiveReleaseHeight += ((1.6 * Math.pow(buoyancyFlux, (1.0 / 3.0))) * Math.pow(x, (2.0 / 3.0))) / uh;*/
        } else {




            // Create a wind speed function to find the wind speed at effective release height numericly
            /*WindSpeedFunc windSpeedFunc = new WindSpeedFunc(this.windSpeedAtReferenceHeight,
                                                            this.referenceHeight,
                                                            p);*/

            //windSpeedAtEffectiveHeight = solver.solve(10000, windSpeedFunc, 1, 15);

            windSpeedAtStackHeight = this.calcWindSpeed(this.terrainType, this.physicalStackHeight);

            if (this.pasquillStability.getStabilityType() == PasquillStabilityType.TYPE_E) {
                s = (0.020 * AtmosphericConcentration.G) / ta;
            } else // TYPE_F
            {
                s = (0.035 * AtmosphericConcentration.G) / ta;
            }

            if (windSpeedAtStackHeight > 1.4)
            {
                // Create bouyant plume rise function to solve it numericly
                BouyantPlumeRiseStableFunc bouyantPlumeRiseStableFunc = new BouyantPlumeRiseStableFunc(buoyancyFlux,
                                                                                     s,
                                                                                     this.physicalStackHeight,
                                                                                     this.windSpeedAtReferenceHeight,
                                                                                     this.referenceHeight,
                                                                                     p);

                effectiveReleaseHeight = solver.solve(100, bouyantPlumeRiseStableFunc, h, 100);

                //effectiveReleaseHeight = h + 2.6 * Math.pow((buoyancyFlux / (windSpeedAtStackHeight * s)), (1.0 / 3.0));

            } else {
                //todo: check this later
                effectiveReleaseHeight = h + 5.0 * Math.pow(buoyancyFlux, (1.0 / 4.0)) * Math.pow(s, -3.8);
            }
        }

        return effectiveReleaseHeight;
    }

    /**
     * The method calculate the Momentum Effective Release Height
     * @param h - the physical height (m)
     * @param v - stack exit velocity (m/s)
     * @param r - stack radius (m)
     * @return The Momentum Effective Release Height
     */
    private double calcMomentumEffectiveReleaseHeight(double h,
                                                      double v,
                                                      double r)
    {
        double momentumFlux = 0; // F
        double effectiveReleaseHeight; // H
        double uh; // Wind speed at physical height
        double s;

        NewtonRaphsonSolver solver = new NewtonRaphsonSolver();

        uh = calcWindSpeed(this.terrainType, h);

        if(this.pasquillStability.getStabilityType() == PasquillStabilityType.TYPE_A ||
           this.pasquillStability.getStabilityType() == PasquillStabilityType.TYPE_B ||
           this.pasquillStability.getStabilityType() == PasquillStabilityType.TYPE_C ||
           this.pasquillStability.getStabilityType() == PasquillStabilityType.TYPE_D) {

            effectiveReleaseHeight = h + ((6.0 * v * r) / uh);
        }
        else
        {
            momentumFlux = 0.25 * Math.pow(2 * r * v, 2);

            double p = this.getCityTerrainWindExpoFactor();

            if (this.pasquillStability.getStabilityType() == PasquillStabilityType.TYPE_E)
            {
                s = 0.000875;
            }
            else // TYPE_F
            {
                s = 0.00175;
            }

            // todo: dont use NewtonRaphsonSolver
            MomentumPlumeRiseFunc momentumPlumeRiseFunc = new MomentumPlumeRiseFunc(momentumFlux,
                                                                                    s,
                                                                                    this.physicalStackHeight,
                                                                                    this.windSpeedAtReferenceHeight,
                                                                                    this.referenceHeight,
                                                                                    p);

            effectiveReleaseHeight = h + solver.solve(100, momentumPlumeRiseFunc, h, 100);

            //effectiveReleaseHeight = h + 1.5 * Math.pow((momentumFlux / uh), (1.0 / 3.0)) * Math.pow(s, (-1.0 / 6.0));
        }

        return effectiveReleaseHeight;
    }

    /**
     * The method calculate the effective release height
     * @param h - the physical height (m)
     * @param v - stack exit velocity (m/s)
     * @param r - stack radius (m)
     * @param ta - ambient air temperature (deg K)
     * @param ts - stack effluent temperature (deg K)
     * @param calcMomentum - Indicates if to calculate the momentum release height
     * @return the effective release height
     */
    private double calcEffectiveReleaseHeight(double h,
                                              double v,
                                              double r,
                                              double ta,
                                              double ts,
                                              double flux,
                                              boolean calcMomentum)
    {
        double EffectiveReleaseHeight = 0;
        double buoyantEffectiveHeight;
        double buoyancyFlux;

        if(flux != 0)
        {
            buoyancyFlux = flux;
        }
        else
        {
            buoyancyFlux = this.calcBuoyancyFlux(v, r, ta, ts);
        }

        // Calc buoyant release height
        buoyantEffectiveHeight = this.calcBouyantEffectiveReleaseHeight(h, ta, buoyancyFlux);

        if(calcMomentum)
        {
            double momentumEffectiveHeight;

            // Calc momentum release height
            momentumEffectiveHeight = this.calcMomentumEffectiveReleaseHeight(h, v, r);

            EffectiveReleaseHeight = (buoyantEffectiveHeight > momentumEffectiveHeight ? buoyantEffectiveHeight : momentumEffectiveHeight);
        }
        else
        {
            EffectiveReleaseHeight = buoyantEffectiveHeight;
        }

        return EffectiveReleaseHeight;
    }

    //endregion

    //region Deviation Calculation

        @Override
        protected double calcDy() {
            return 0f;
        }

        @Override
        protected double calcDz() {
            return 0f;
        }

        @Override
        protected double calcCrossWindRadios(double sigmaY) {
            return sigmaY;
        }

        @Override
        protected double calcPlumeTop(double sigmaZ, double effectiveReleaseHeight) {
            return effectiveReleaseHeight + sigmaZ;
        }

        @Override
        protected double calcPlumeBottom(double sigmaZ, double effectiveReleaseHeight) {
            return effectiveReleaseHeight - sigmaZ > 0 ? effectiveReleaseHeight - sigmaZ : 0 ;
        }

    //endregion

    //region Atmospheric Concentration

    @Override
    public OutputResult calcAtmosphericConcentration()
    {
        double concentration = 0;
        double releaseHeight;
        ArrayList<ConcentrationResult> results = new ArrayList<ConcentrationResult>();

        if(this.pasquillStability == null)
        {
            this.pasquillStability = new PasquillStability(this.windSpeedAtReferenceHeight,
                                                           this.meteorologicalCondition);
        }

        if(this.effectiveReleaseHeight != 0)
        {
            releaseHeight = this.effectiveReleaseHeight;
        }
        else
        {
            releaseHeight = this.calcEffectiveReleaseHeight(this.physicalStackHeight,
                            this.stackExitVelocity,
                            this.stackRadius,
                            AtmosphericConcentration.convertToKelvin(this.airTemp),
                            AtmosphericConcentration.convertToKelvin(this.stackTemp),
                            this.heatEmission,
                            this.calcMomentum);
        }

        double windSpeed = this.calcWindSpeed(this.terrainType, releaseHeight);

        OutputResult outputResult = this.getOutputResult(effectiveReleaseHeight, windSpeed);

        outputResult.addValue(ResultField.MODEL_TYPE, "General Plume");

        return outputResult;
    }

    //endregion
}
