package com.hotspothealthcode.hotspothealthcode.Components.Steps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.ToggleButton;

import com.hotspothealthcode.hotspothealthcode.R;

/**
 * Created by Giladl on 16/01/2016.
 */
public class GeneralPlumeStepView extends StepView
{
    private EditText materialAtRisk;

    private TabHost tabHost;
    private EditText releaseHeight;
    private EditText airTemp;
    private EditText stackHeight;
    private EditText heatEmission;
    private CheckBox heatEmissionEnter;

    private CheckBox momentumCalc;
    private GridLayout momentumGrid;
    private EditText stackRadios;
    private EditText exitVelocity;
    private EditText effluentTemp;

    public GeneralPlumeStepView(Context context, int stepNumber, String title, int contentViewId, OnClickListener continueBtnHandler) {
        super(context, stepNumber, title, contentViewId, continueBtnHandler);

        this.initControl();
    }

    public GeneralPlumeStepView(Context context) {
        super(context);
    }

    public GeneralPlumeStepView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GeneralPlumeStepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initControl()
    {
        // create the TabHost that will contain the Tabs
        this.tabHost = (TabHost)findViewById(android.R.id.tabhost);
        this.tabHost.setup();

        TabSpec tab1 = this.tabHost.newTabSpec("EnterHeight");
        TabSpec tab2 = this.tabHost.newTabSpec("CalcHeight");

        tab1.setIndicator("Enter Height");
        tab1.setContent(R.id.tbEnterHeight);

        tab2.setIndicator("Calc Height");
        tab2.setContent(R.id.tbCalcHeight);

        this.tabHost.addTab(tab1);
        this.tabHost.addTab(tab2);

        // Get all fields
        this.releaseHeight = (EditText)this.tabHost.findViewById(R.id.etReleaseHeight);
        this.airTemp = (EditText)this.tabHost.findViewById(R.id.etAirTemp);
        this.stackHeight = (EditText)this.tabHost.findViewById(R.id.etStackHeight);
        this.heatEmission = (EditText)this.tabHost.findViewById(R.id.etHeatEmission);
        this.heatEmissionEnter = (CheckBox)this.tabHost.findViewById(R.id.cbHeatEmissionEnter);
        this.momentumCalc = (CheckBox)findViewById(R.id.cbMomentum);
        this.momentumGrid = (GridLayout)findViewById(R.id.glMomentum);
        this.stackRadios = (EditText)findViewById(R.id.etStackRadius);
        this.exitVelocity = (EditText)findViewById(R.id.etExitVelocity);
        this.effluentTemp = (EditText)findViewById(R.id.etEffluentTemp);

        this.materialAtRisk = (EditText)findViewById(R.id.etMaterialAtRisk);

        // Set events

        this.heatEmissionEnter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowMomentumGrid();

                // Set emission field to read only
                if (((CheckBox)v).isChecked())
                    heatEmission.setEnabled(true);
                else
                    heatEmission.setEnabled(false);
            }
        });

        this.momentumCalc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowMomentumGrid();
            }
        });

        this.tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                // If the enter height tab was clicked and include momentum is not checked
                if ((tabId == "EnterHeight") && (!momentumCalc.isChecked()))
                    heatEmissionEnter.callOnClick();
            }
        });
    }

    private void ShowMomentumGrid()
    {
        if((this.momentumCalc.isChecked()) || (this.heatEmissionEnter.isChecked() == false))
            this.momentumGrid.setVisibility(VISIBLE);
        else
            this.momentumGrid.setVisibility(GONE);
    }

    @Override
    protected boolean validateData() {

        boolean emptyFieldsInTab = true;

        // Get current selected tab
        int tabId = this.tabHost.getCurrentTab();

        switch (tabId)
        {
            case 0:
            {
                emptyFieldsInTab = !this.releaseHeight.getText().toString().matches("");

                break;
            }

            case 1:
            {
                emptyFieldsInTab = (!this.heatEmission.getText().toString().matches("")) &&
                                   (!this.airTemp.getText().toString().matches("")) &&
                                   (!this.stackHeight.getText().toString().matches(""));

                break;
            }
        }

        return ((emptyFieldsInTab) &&
                (!this.materialAtRisk.getText().toString().matches("")));
    }
}
