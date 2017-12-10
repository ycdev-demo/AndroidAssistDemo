package me.ycdev.android.demo.assist.voice;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStructure;

/**
 * Test for asynchronously creating additional assist structure.
 */
public class AsyncStructure extends android.support.v7.widget.AppCompatTextView {
    public AsyncStructure(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onProvideVirtualStructure(ViewStructure structure) {
        structure.setChildCount(1);
        final ViewStructure child = structure.asyncNewChild(0);
        final int width = getWidth();
        final int height = getHeight();
        new Thread() {
            @Override
            public void run() {
                // Simulate taking a long time to build this.
                SystemClock.sleep(2000);

                child.setClassName(AsyncStructure.class.getName());
                child.setVisibility(View.VISIBLE);
                child.setDimens(width / 4, height / 4, 0, 0, width / 2, height / 2);
                child.setEnabled(true);
                child.setContentDescription("This is some async content");
                child.setText("We could have lots and lots of async text!");
                child.asyncCommit();
            }
        }.start();
    }
}
