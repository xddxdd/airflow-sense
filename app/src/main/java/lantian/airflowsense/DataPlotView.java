package lantian.airflowsense;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class DataPlotView extends View {
    int data_length = 384;
    int data_offset = 0;
    double data_min = 0;
    double data_max = 1;
    double[] data;
    float[] data_y_coordinates;

    float[] x_coordinates;

    Paint linePaint;
    Path path;

    public DataPlotView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2);
        linePaint.setColor(Color.argb(255, 255, 255, 255));

        _assignXCoordinates();

        data = new double[data_length];
        data_y_coordinates = new float[data_length];

        for(int i = 0; i < data_length; i++) {
            data[i] = 0.0;
            data_y_coordinates[i] = getHeight();
        }

        _calculatePath();
    }

    public void setRange(double min, double max) {
        data_min = min;
        data_max = max;
    }

    public double mapDataToZeroToOne(double value) {
        if (value > data_max) return 1.0;
        if (value < data_min) return 0.0;
        return (value - data_min) / (data_max - data_min);
    }

    public void addDataPoint(double value) {
        data[data_offset] = value;
        data_y_coordinates[data_offset] = (float) (getHeight() * (1 - mapDataToZeroToOne(value)));
        data_offset = (data_offset + 1) % data_length;
        _calculatePath();
        invalidate();
    }

    public double getRecentDataMax(int length) {
        if(length > data_length || length == 0) {
            length = data_length;
        }
        double max = 0.0;
        for(int i = data_length - length; i < data_length; i++) {
            double d = data[(i + data_offset) % data_length];
            if(d > max) max = d;
        }
        return max;
    }

    public double getRecentDataMin(int length) {
        if(length > data_length || length == 0) {
            length = data_length;
        }
        double min = 1.0;
        for(int i = data_length - length; i < data_length; i++) {
            double d = data[(i + data_offset) % data_length];
            if(d < min) min = d;
        }
        return min;
    }

    public double getRecentDataAverage(int length) {
        if(length > data_length || length == 0) {
            length = data_length;
        }
        double sum = 0.0;
        for(int i = data_length - length; i < data_length; i++) {
            sum += data[(i + data_offset) % data_length];
        }
        return sum / length;
    }

    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        super.onSizeChanged(w, h, old_w, old_h);

        _assignXCoordinates();

        // Recalculate Y coordinates
        for(int i = 0; i < data_length; i++) {
            data_y_coordinates[i] = (float) (getHeight() * (1 - mapDataToZeroToOne(data[i])));
        }
        _calculatePath();
    }

    private void _assignXCoordinates() {
        if (null == x_coordinates) {
            x_coordinates = new float[data_length];
        }

        for (int i = 0; i < data_length; i++) {
            x_coordinates[i] = getWidth() * i / (data_length - 1);
        }
    }

    private void _calculatePath() {
        path = new Path();
        path.moveTo(x_coordinates[0], data_y_coordinates[data_offset]);
        for(int i = 1; i < data_length; i++) {
            float x = x_coordinates[i];
            float y = data_y_coordinates[(i + data_offset) % data_length];
            path.lineTo(x, y);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, linePaint);
    }
}
