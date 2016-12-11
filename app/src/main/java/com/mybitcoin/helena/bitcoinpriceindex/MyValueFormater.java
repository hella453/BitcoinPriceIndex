package com.mybitcoin.helena.bitcoinpriceindex;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

/**
 * Created by Helena on 12/11/2016.
 */

public class MyValueFormater implements IValueFormatter {

    private DecimalFormat mFormat;

    public MyValueFormater() {
        mFormat = new DecimalFormat("###,###,##0.00"); // use one decimal
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        // write your logic here
        return mFormat.format(value) + " €"; // e.g. append a dollar-sign
    }
}