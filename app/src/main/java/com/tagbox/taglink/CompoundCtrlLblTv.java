package com.tagbox.taglink;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Suhas on 10/24/2016.
 */

public class CompoundCtrlLblTv extends LinearLayout {
    private TextView mLabel;
    private EditText mValue;

    public CompoundCtrlLblTv(Context context) {
        super(context);
        initializeViews(context);
    }

    public CompoundCtrlLblTv(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public CompoundCtrlLblTv(Context context,
                             AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    /**
     * Inflates the views in the layout.
     * @param context
     *           the current context for the view.
     */
    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.cc_lbl_tv, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setLabel(String label) {
        mLabel = (TextView)this.findViewById(R.id.cc_label);
        mLabel.setText(label);
    }

    public String getValue() {
        mValue = (EditText)this.findViewById(R.id.cc_value);
        return mValue.getText().toString();
    }

    public void setValue(String value) {
        mValue = (EditText) this.findViewById(R.id.cc_value);
        mValue.setText(value);
    }
}
