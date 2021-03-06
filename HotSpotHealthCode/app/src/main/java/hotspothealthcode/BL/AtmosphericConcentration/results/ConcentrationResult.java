package hotspothealthcode.BL.AtmosphericConcentration.results;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by Giladl on 09/01/2016.
 */
public class ConcentrationResult
{
    private static int resultsNum = 0;

    private int id;
    private double concentration;
    private int arrivalTime; // HH:MM
    private ConcentrationPoint point;
    private double crossWindRadios;
    private double plumeTop;
    private double plumeBottom;

    public ConcentrationResult(ConcentrationPoint point,
                               double concentration,
                               int arrivalTime,
                               double crossWindRadios,
                               double plumeTop,
                               double plumeBottom)
    {
        this.id = ConcentrationResult.resultsNum;
        this.point = point;
        this.concentration = concentration;
        this.arrivalTime = arrivalTime;
        this.crossWindRadios = crossWindRadios;
        this.plumeTop = plumeTop;
        this.plumeBottom = plumeBottom;

        ConcentrationResult.resultsNum++;
    }

    public ConcentrationResult(JSONObject jsonObject) throws JSONException {

        this.id = ConcentrationResult.resultsNum;
        this.point = new ConcentrationPoint(jsonObject.getJSONObject("point"));
        this.concentration = jsonObject.getDouble("concentration");
        this.arrivalTime = jsonObject.getInt("arrivalTime");
        this.crossWindRadios = jsonObject.getDouble("crossWindRadios");
        this.plumeTop = jsonObject.getDouble("plumeTop");
        this.plumeBottom = jsonObject.getDouble("plumeBottom");

        ConcentrationResult.resultsNum++;
    }

    public int getId() {
        return this.id;
    }

    public ConcentrationPoint getPoint() {
        return this.point;
    }

    public double getConcentration() {
        return this.concentration;
    }

    public int getArrivalTime() {
        return this.arrivalTime;
    }

    public double getCrossWindRadios() {
        return crossWindRadios;
    }

    public String getStringCrossWindRadios() {

        return String.format("%.2f", this.crossWindRadios);
    }

    public String getStringPlumeTop() {
        return String.format("%.2f", plumeTop);
    }

    public String getStringPlumeBottom() {
        return String.format("%.2f", plumeBottom);
    }

    public String getStringPoint() {
        return this.point.toString();
    }

    public String getStringConcentration() {
        NumberFormat formatter = new DecimalFormat("0.#E0");

        return formatter.format(this.concentration);
    }

    public String getStringArrivalTime() {

        int hours = this.arrivalTime / 3600;
        int minutes = (this.arrivalTime % 3600) / 60;

        String time = "";

        if (minutes == 0)
            time += "<";

        if (hours < 10)
            time += "0" + hours;
        else
            time += hours;

        time += ":";

        if (minutes < 10)
            time += "0" + minutes;
        else
            time += minutes;

        return time;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("point", this.point.toJSON());
        jsonObject.put("concentration", this.concentration);
        jsonObject.put("arrivalTime", this.arrivalTime);
        jsonObject.put("crossWindRadios", this.crossWindRadios);
        jsonObject.put("plumeTop", this.plumeTop);
        jsonObject.put("plumeBottom", this.plumeBottom);

        return jsonObject;
    }

    @Override
    public String toString()
    {
        return "Concentration: " + this.getStringConcentration() + "\n" +
               "Arrival Time: " + this.getStringArrivalTime() + "\n" +
               "Point: " + this.getStringPoint() + "\n" +
               "Cross Wind Offset: " + this.crossWindRadios + "\n" +
               "Plume Top: " + this.plumeTop + "\n" +
               "Plume Bottom: " + this.plumeBottom + "\n";
    }
}
