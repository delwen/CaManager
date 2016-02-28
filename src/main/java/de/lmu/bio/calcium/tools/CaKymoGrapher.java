package de.lmu.bio.calcium.tools;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

import javax.vecmath.Point2d;
import java.util.ArrayList;

public class CaKymoGrapher {

    private static double[] createPlotForStack(ImageStack stack, Roi roi, int z) {

        ImageProcessor cip = stack.getProcessor(z+1);
        cip.setInterpolate(true);
        ImagePlus temp = new ImagePlus("<temp>" + z+1, cip);
        temp.setRoi(roi);
        ProfilePlot pp = new ProfilePlot(temp, false);
        return pp.getProfile();
    }

    public static void setValues(float[][] target, int x, double[] source) {
        for (int j = 0; j < source.length; j++) {
            target[j][x] = (float) source[j];
        }
    }

    public static float[][] createRawKymoGraphOld(ImagePlus imp, Roi roi) {
        ImageStack stack = imp.getImageStack();

        int stackSize = stack.getSize();
        double[] profile = createPlotForStack(stack, roi, 0);

        float[][] data = new float[profile.length][stackSize];
        setValues(data, 0, profile);

        for (int i = 1; i < stackSize; i++) {
            profile = createPlotForStack(stack, roi, i);
            setValues(data, i, profile);
        }
        return data;
    }

    public static float[][] createRawKymoGraph(ImagePlus imp, Roi roi) {
        ImageStack stack = imp.getImageStack();

        int stackSize = stack.getSize();
        Point2d[] points = roi2YX(roi);

        if (points == null) {
            return null;
        }

        float[][] data = new float[points.length][stackSize];

        for (int i = 0; i < stackSize; i++) {
            ImageProcessor cip = stack.getProcessor(i+1);
            for (int p = 0; p < points.length; p++) {
                final Point2d point = points[p];
                if (i == 0)
                    System.err.println(point);

                data[p][i] = (float) cip.getInterpolatedValue(point.x, point.y);
            }
        }

        return data;
    }

    // heavily based on ij.gui.ProfilePlot
    public static Point2d[] roi2YX(Roi roi) {
        if (roi == null) {
            IJ.error("KymoGraph", "Selction required.");
            return null;
        }

        int roiType = roi.getType();
        if (!(roiType==Roi.POLYLINE || roiType==Roi.FREELINE)) {
            IJ.error("PolyLine or Freeline selection required.");
            return null;
        }

        int lineWidth = (int)Math.round(roi.getStrokeWidth());
        if (lineWidth > 1) {
            IJ.log("[WARNING] linewidth ignored!");
        }

        FloatPolygon p = roi.getFloatPolygon();
        int n = p.npoints;
        float[] xpoints = p.xpoints;
        float[] ypoints = p.ypoints;
        ArrayList<Point2d> values = new ArrayList<Point2d>();

        int n2;
        double inc = 0.01;
        double distance, distance2, dx, dy, xinc, yinc;
        double x, y, lastx=0.0, lasty=0.0, x1, y1, x2=xpoints[0], y2=ypoints[0];
        for (int i=1; i<n; i++) {
            x1=x2; y1=y2;
            x=x1; y=y1;
            x2=xpoints[i]; y2=ypoints[i];
            dx = x2-x1;
            dy = y2-y1;
            distance = Math.sqrt(dx*dx+dy*dy);
            xinc = dx*inc/distance;
            yinc = dy*inc/distance;
            n2 = (int)(distance/inc);
            if (n == 2)
                n2++;
            do {
                dx = x-lastx;
                dy = y-lasty;
                distance2 = Math.sqrt(dx*dx+dy*dy);
                if (distance2 >= 1.0 - inc/2.0) {
                    values.add(new Point2d(x, y));
                    lastx=x; lasty=y;
                }
                x += xinc;
                y += yinc;
            } while (--n2 > 0);
        }
        Point2d[] points = new Point2d[values.size()];
        return values.toArray(points);
    }

}
