package com.hotspothealthcode.hotspothealthcode.Components.Steps;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.EditText;

import com.hotspothealthcode.hotspothealthcode.R;

import hotspothealthcode.BL.AtmosphericConcentration.AtmosphericConcentration;
import hotspothealthcode.BL.AtmosphericConcentration.ExplosionAtmosphericConcentration;

/**
 * Created by Giladl on 16/01/2016.
 */
public class GeneralExplosionStepView extends StepView
{
    private EditText materialAtRisk;
    private EditText explosiveAmount;
    private CheckBox isGreenField;

    public GeneralExplosionStepView(Context context, int stepNumber, String title, int contentViewId) {
        super(context, stepNumber, title, contentViewId);

        this.initControl(context);
    }

    public GeneralExplosionStepView(Context context) {
        super(context);
    }

    public GeneralExplosionStepView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GeneralExplosionStepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initControl(Context context)
    {
        // Get all fields
        this.materialAtRisk = (EditText)findViewById(R.id.etMaterialAtRisk);
        this.explosiveAmount =(EditText)findViewById(R.id.etExplosiveAmount);
        this.isGreenField = (CheckBox)findViewById(R.id.cbIsGreenField);
    }

    @Override
    protected boolean validateData() {

        return ((!this.materialAtRisk.getText().toString().matches("")) &&
                (!this.explosiveAmount.getText().toString().matches("")));
    }

    @Override
    public void setFieldsToCalculate(AtmosphericConcentration calcConcentration)
    {
        ExplosionAtmosphericConcentration concentration = (ExplosionAtmosphericConcentration)calcConcentration;

        concentration.setSourceTerm(Double.parseDouble(this.materialAtRisk.getText().toString()));
        concentration.setExplosiveAmount(Double.parseDouble(this.explosiveAmount.getText().toString()));
        concentration.setIsGreenField(this.isGreenField.isChecked());
    }

    @Override
    public void saveFieldsToSharedPreferences(Context context) {

    }
}
