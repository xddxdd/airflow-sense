package lantian.airflowsense;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * DataPlotView
 * Self-defined View Element
 */
public class DataPlotView extends View {
    int data_length = 1024; // The number of data points to be collected
    int data_offset = 0;
    double data_min = 0;
    double data_max = 2;
    double[] data;          // Data points collected
    float[] data_y_coordinates;

    float[] x_coordinates;  // The x coordinates for each data to be printed

    Paint linePaint; // The paintbrush that draws lines
    Path path;

    public DataPlotView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        /* Initialize the linePaint element */
        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE); // Set the paint style -- Draw as paintbrush
        linePaint.setStrokeWidth(2); // Set the width of the paintbrush
        linePaint.setColor(Color.argb(255, 255, 255, 255)); // Set the paintbrush to white

        /* Initialize the x_coordinates array */
        _assignXCoordinates();

        /* Initialize the raw data array and the y_coordinates array */
        data = new double[data_length];
        data_y_coordinates = new float[data_length];

        for(int i = 0; i < data_length; i++) {
            data[i] = 0.0; // Initialize the raw data to 0
            data_y_coordinates[i] = getHeight(); // Initialize the y coordinates to the canvas's height
        }

        /* Draw the initial graph */
        _calculatePath();
    }

    public void setRange(double min, double max) {
        data_min = min;
        data_max = max;
    }

    /**
     * mapDataToZeroToOne
     * Normalize the value in convenience of plotting
     * @param value The raw data to be normalized
     * @return The normalized data
     */
    public double mapDataToZeroToOne(double value) {
        if (value > data_max) return 1.0;
        if (value < data_min) return 0.0;
        return (value - data_min) / (data_max - data_min);
    }

    /**
     * addDataPoint
     * Add the new data point to the data array and the data_y_coordinates array
     * @param value The new data point
     */
    public void addDataPoint(double value) {
        data[data_offset] = value;
        data_y_coordinates[data_offset] = (float) (getHeight() * (1 - mapDataToZeroToOne(value))); // Normalize the value in convenience of plotting
        data_offset = (data_offset + 1) % data_length;
        _calculatePath();
        invalidate();
    }

    /**
     * getRecentDataMax
     * Get the highest data point in a segment of data
     * @param length The starting index of the segment
     * @return The maximum value
     */
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

    /**
     * getRecentDataMin
     * Get the lowest data point in a segment of data
     * @param length The starting index of the segment
     * @return The minimum value
     */
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

    /**
     * getRecentDataAverage
     * Get the average value in a segment of data
     * @param length The starting index of the segment
     * @return The average value
     */
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

    /**
     * _assignXCoordinates
     * Initialize the x_coordinates array element with respect to the width of the canvas
     */
    private void _assignXCoordinates() {
        if (null == x_coordinates) {
            x_coordinates = new float[data_length];
        }

        for (int i = 0; i < data_length; i++) {
            x_coordinates[i] = getWidth() * i / (data_length - 1);
        }
    }

    /**
     * _calculatePath
     * Draw the graph point by point
     */
    private void _calculatePath() {
        path = new Path();
        path.moveTo(x_coordinates[0], data_y_coordinates[data_offset]); // Set the starting point of the graph
        for(int i = 1; i < data_length; i++) {
            float x = x_coordinates[i];
            float y = data_y_coordinates[(i + data_offset) % data_length];
            path.lineTo(x, y); // Draw a line between this point and the last point (the first point is set by the moveTo function)
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, linePaint);
    }
}
