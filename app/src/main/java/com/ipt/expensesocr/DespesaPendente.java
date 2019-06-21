package com.ipt.expensesocr;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.TextView;

public class DespesaPendente {
    Context ctx;

    public DespesaPendente(Context ctx) {
        this.ctx = ctx;
    }

    public TextView despesTextView(Context context, String texto){
        final ViewGroup.LayoutParams lparams= new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        final TextView textView = new TextView(context);
        textView.setLayoutParams(lparams);
        textView.setTextColor(Color.rgb(0,0,0));
        textView.setText(" "+texto+" ");
        textView.setMaxEms(8);
        return textView;
    }
}
