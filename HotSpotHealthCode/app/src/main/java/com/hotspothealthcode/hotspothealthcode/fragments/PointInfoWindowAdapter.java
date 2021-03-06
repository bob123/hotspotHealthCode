package com.hotspothealthcode.hotspothealthcode.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.hotspothealthcode.hotspothealthcode.R;

import org.json.JSONException;
import org.json.JSONObject;

import hotspothealthcode.BL.AtmosphericConcentration.results.ConcentrationResult;
import hotspothealthcode.BL.AtmosphericConcentration.results.OutputResult;
import hotspothealthcode.BL.AtmosphericConcentration.results.ResultField;

/**
 * Created by Giladl on 08/02/2016.
 */
public class PointInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public PointInfoWindowAdapter(Context context)
    {
        this.context = context;
    }

    // Use default InfoWindow frame
    @Override
    public View getInfoWindow(Marker arg0) {
        return null;
    }

    // Defines the contents of the InfoWindow
    @Override
    public View getInfoContents(Marker marker) {

        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        View view;

        // If the point is a concentration point
        if (marker.getTitle().equals("Coordinate"))
            view = this.setConcentrationPointView(marker, inflater);
        else if (marker.getTitle().equals("Origin"))
            view = this.setOriginPointView(inflater);
        else
            view = this.setVirtualSourcePointView(inflater);

        // Returning the view containing InfoWindow contents
        return view;
    }

    private View setConcentrationPointView(Marker marker, LayoutInflater inflater)
    {
        View view = inflater.inflate(R.layout.concentration_point_info_view, null);

        TextView tvDownWindDistance = (TextView)view.findViewById(R.id.tvDownWindDistance);
        TextView tvCrossWindDistance = (TextView)view.findViewById(R.id.tvCrossWindDistance);
        TextView tvVerticalHeight = (TextView)view.findViewById(R.id.tvVerticalHeight);
        TextView tvCrossConcentration = (TextView)view.findViewById(R.id.tvConcentration);
        TextView tvArrivalTime = (TextView)view.findViewById(R.id.tvArrivalTime);
        TextView tvCrossWindOffset = (TextView)view.findViewById(R.id.tvCrossWindOffset);
        TextView tvPlumeTop = (TextView)view.findViewById(R.id.tvPlumeTop);
        TextView tvPlumeBottom = (TextView)view.findViewById(R.id.tvPlumeBottom);

        ConcentrationResult concentrationResult = null;

        // Find the result by its id
        for (ConcentrationResult res: OutputResult.getInstance().getResults())
        {
            if (res.getId() == Integer.parseInt(marker.getSnippet())) {
                concentrationResult = res;
                break;
            }
        }

        // If the result was found set the fields text
        if (concentrationResult != null) {

            tvDownWindDistance.setText(String.valueOf(concentrationResult.getPoint().getX() / 1000));
            tvCrossWindDistance.setText(String.valueOf(concentrationResult.getPoint().getY() / 1000));
            tvVerticalHeight.setText(String.valueOf(concentrationResult.getPoint().getZ()));
            tvCrossConcentration.setText(concentrationResult.getStringConcentration());
            tvArrivalTime.setText(concentrationResult.getStringArrivalTime());
            tvCrossWindOffset.setText(concentrationResult.getStringCrossWindRadios());
            tvPlumeTop.setText(concentrationResult.getStringPlumeTop());
            tvPlumeBottom.setText(concentrationResult.getStringPlumeBottom());
        }

        return view;
    }

    private View setOriginPointView(LayoutInflater inflater)
    {
        View view = inflater.inflate(R.layout.origin_point_info_view, null);

        TextView tvModelType = (TextView)view.findViewById(R.id.tvModelType);
        TextView tvStabilityType = (TextView)view.findViewById(R.id.tvStabilityType);
        TextView tvWindSpeed = (TextView)view.findViewById(R.id.tvWindSpeed);
        TextView tvWindDirection = (TextView)view.findViewById(R.id.tvWindDirection);

        OutputResult result = OutputResult.getInstance();

        tvModelType.setText(result.getValue(ResultField.MODEL_TYPE).toString());
        tvStabilityType.setText(result.getValue(ResultField.STABILITY_TYPE).toString());
        tvWindSpeed.setText(result.getValue(ResultField.WIND_SPEED).toString());
        tvWindDirection.setText(result.getValue(ResultField.WIND_DIRECTION).toString());

        return view;
    }

    private View setVirtualSourcePointView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.virtual_point_info_view, null);

        TextView tvDistanceFromOrigin = (TextView)view.findViewById(R.id.tvDistanceFromOrigin);

        OutputResult result = OutputResult.getInstance();

        tvDistanceFromOrigin.setText(String.format("%.2f", (Double)result.getValue(ResultField.DOWN_WIND_VIRTUAL_SOURCE)));

        return view;
    }
}
