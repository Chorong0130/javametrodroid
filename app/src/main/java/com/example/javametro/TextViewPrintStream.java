package com.example.javametro;

import android.widget.TextView;
import java.io.OutputStream;
import java.io.PrintStream;

public class TextViewPrintStream extends PrintStream {
    private TextView textView;

    public TextViewPrintStream(OutputStream out, TextView textView) {
        super(out);
        this.textView = textView;
    }

    @Override
    public void println(final String x) {
        textView.post(new Runnable() {
            @Override
            public void run() {
                textView.append(x + "\n");
            }
        });
    }
}
