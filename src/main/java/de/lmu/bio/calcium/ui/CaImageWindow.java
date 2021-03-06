package de.lmu.bio.calcium.ui;

import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaRoiBox;
import de.lmu.bio.calcium.model.CaRoiClass;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.*;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class CaImageWindow extends StackWindow implements MouseListener, TreeModelListener{
    protected CaImage image;
    protected DefaultTreeModel treeModel;

    public CaImageWindow(ImagePlus timp, CaImage img, DefaultTreeModel treeModel) {
        super(new CaImagePlus(timp));
        this.image = img;
        this.treeModel = treeModel;
        CaIWOverlay overlay = new CaIWOverlay();
        imp.setOverlay(overlay);
        getCanvas().addMouseListener(this);
        treeModel.addTreeModelListener(this);
    }

    @Override
    public boolean close() {
        boolean res = super.close();
        treeModel.removeTreeModelListener(this);
        return res;
    }

    public static CaImageWindow createWindow(CaImage img, DefaultTreeModel treeModel) {
        ImagePlus ip = img.openImage();

        if (ip == null) {
            IJ.showMessage("Could not open Image");
            return null;
        }

        return new CaImageWindow(ip, img, treeModel);
    }

    public void replaceCurrentRoi(Roi newRoi) {
        Roi roi = imp.getRoi();
        Overlay overlay = imp.getOverlay();
        if (overlay.contains(roi)) {
            overlay.remove(roi);
            imp.deleteRoi();
        }

        imp.setRoi(newRoi, true);
    }

    public CaImage getCaImage() {
        return image;
    }

    //Event listeners (Mouse, Keyboard)
    //------------------------------------------------------
    private boolean roiChanged = false;
    @Override
    public void mouseClicked(MouseEvent e) { /* System.out.println("Mouse clicked!"); */ }

    @Override
    public void mousePressed(MouseEvent e) {
        Roi roi = imp.getRoi();
        if (roi != null && roi.getState() != 3) {
            System.out.println("Roi change: Pressed State " + roi.getState());
            roiChanged = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Overlay overlay = imp.getOverlay();
        System.err.println(overlay);
        Roi roi = imp.getRoi();

        if (roiChanged) {
            if (roi != null) {
                System.out.println("Roi Changed! " + roi.getState());
            }
            roiChanged = false;
        }

        if (roi != null && roi.getState() != Roi.CONSTRUCTING && !overlay.contains(roi)) {
            System.out.println("State " + roi.getState());
            System.err.print("New ROI!\n");
            overlay.add(roi);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }


    @Override
    public void treeNodesChanged(TreeModelEvent e) {}

    @Override
    public void treeNodesInserted(TreeModelEvent e) {}

    @Override
    public void treeNodesRemoved(TreeModelEvent e) { }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        System.err.print("[+] treeStructureChanged !!");
        //lets do a two way sync
        Overlay overlay = imp.getOverlay();
        Roi[] ours = overlay.toArray();
        java.util.List<CaRoiBox> theirs = image.listRois();
        //O(n^2) but so few rois!
        for (Roi r : ours) {
            boolean have = false;
            for (CaRoiBox b : theirs) {
                if (b.getRoi() == r) {
                    have = true;
                    break;
                }
            }

            if (!have) {
                overlay.remove(r);
            }
        }

        for (CaRoiBox b : theirs) {
            boolean have = false;
            for (Roi r : ours) {
                if (b.getRoi() == r) {
                    have = true;
                    break;
                }
            }

            if (!have) {
                overlay.add(b.getRoi());
            }
        }

        getCanvas().repaint();
    }

    //------------------------------------------------------------------------------------------------------------------
    public class CaIWOverlay extends Overlay {

        public CaIWOverlay() {
            for (CaRoiBox box : image.listRois()) {
                add(box.getRoi());
            }

            drawLabels(true);
            drawNames(true);
            drawBackgrounds(true);
        }

        @Override
        public void remove(Roi roi) {
            super.remove(roi);
            System.err.print("ROI REMOVED!\n");
            image.remove(roi);
        }

        @Override
        public void remove(int index) {
            Roi roi = super.get(index);
            remove(roi);
            System.err.print("ROI REMOVED (+)!\n");
        }

        @Override
        public void add(Roi roi) {

            if (roi == null)
                return;

            System.err.println("Overlay: Roi added!");
            CaRoiBox box = image.maybeAddRoi(roi);

            super.add(roi);
            if (box != null) {
                System.err.println("Roi added as " + box.getName());
                treeModel.nodeStructureChanged(image);
            }
        }
    }

    //Event listeners (Mouse, Keyboard)
    //------------------------------------------------------
    public static class CaImagePlus extends ImagePlus {

        public CaImagePlus(ImagePlus tip) {
            String title = tip.getTitle();
            setImage(tip);
            setTitle(title);
        }

        @Override
        public void createNewRoi(int sx, int sy) {
            System.err.println("[de.lmu.bio.calcium.ui.CaImageWindow.CaImagePlus] create ROI!");
            super.createNewRoi(sx, sy);
        }

        @Override
        public void setRoi(Roi newRoi, boolean updateDisplay) {
            System.err.println("[de.lmu.bio.calcium.ui.CaImageWindow.CaImagePlus] set ROI!");
            super.setRoi(newRoi, updateDisplay);
            Overlay overlay = getOverlay();
            if (overlay != null && !overlay.contains(roi)) {
                overlay.add(roi);
            }

        }

        @Override
        public void deleteRoi() {
            System.err.println("[de.lmu.bio.calcium.ui.CaImageWindow.CaImagePlus] delete ROI!");
            super.deleteRoi();
        }
//
//        @Override
//        public void restoreRoi() {
//            super.restoreRoi();
//            System.err.println("[de.lmu.bio.calcium.ui.CaImageWindow.CaImagePlus] restore ROI!");
//            Overlay overlay = getOverlay();
//            if (overlay != null) {
//                overlay.add(roi);
//            }
//        }
    }
}
