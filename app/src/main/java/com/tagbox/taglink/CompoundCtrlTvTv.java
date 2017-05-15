package com.tagbox.taglink;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Suhas on 11/7/2016.
 */

public class CompoundCtrlTvTv extends LinearLayout {
    private TextView mLabel;
    private TextView mValue;

    public CompoundCtrlTvTv(Context context) {
        super(context);
        initializeViews(context);
    }

    public CompoundCtrlTvTv(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public CompoundCtrlTvTv(Context context,
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
        inflater.inflate(R.layout.cc_tv_tv, this);
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
        mValue = (TextView)this.findViewById(R.id.cc_value);
        return mValue.getText().toString();
    }

    public void setValue(String value) {
        mValue = (TextView) this.findViewById(R.id.cc_value);
        mValue.setText(value);
    }
}
