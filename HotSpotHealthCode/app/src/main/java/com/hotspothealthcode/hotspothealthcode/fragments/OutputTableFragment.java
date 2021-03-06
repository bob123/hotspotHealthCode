package com.hotspothealthcode.hotspothealthcode.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.hotspothealthcode.hotspothealthcode.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import hotspothealthcode.BL.AtmosphericConcentration.results.ConcentrationResult;
import hotspothealthcode.BL.AtmosphericConcentration.results.OutputResult;
import hotspothealthcode.controllers.Controller;

/**
 * Created by Giladl on 05/02/2016.
 */
public class OutputTableFragment extends Fragment
{
    private TableLayout tableLayout;
    private OutputResult outputResult;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_output_table, container, false);

        this.tableLayout = (TableLayout)rootView.findViewById(R.id.tlOutputTable);

        this.outputResult = Controller.getOutputResultInstance();

        ArrayList<ConcentrationResult> results = this.outputResult.getResults();

        // Add rows to table
        for (ConcentrationResult result: results)
        {
            OutputRow outputRow = new OutputRow(rootView.getContext(), result);

            outputRow.addToTable(this.tableLayout);
        }

        return rootView;
    }

    private class OutputRow
    {
        private ConcentrationResult result;

        public TextView distance;
        public TextView concentration;
        public TextView arrivalTime;
        public TableRow tableRow;

        public OutputRow(Context context, ConcentrationResult result)
        {
            this.result = result;

            this.tableRow = (TableRow)View.inflate(context, R.layout.output_table_row, null);

            this.distance = (TextView)this.tableRow.findViewById(R.id.tvDistanceResult);
            this.distance.setText(this.result.getStringPoint());

            this.concentration = (TextView)this.tableRow.findViewById(R.id.tvAirConcentrationResult);
            this.concentration.setText(this.result.getStringConcentration());

            this.arrivalTime = (TextView)this.tableRow.findViewById(R.id.tvArrivalTimeResult);
            this.arrivalTime.setText(this.result.getStringArrivalTime());
        }

        public void addToTable(TableLayout tableLayout)
        {
            tableLayout.addView(this.tableRow);
        }
    }
}
