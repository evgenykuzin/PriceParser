package com.github.evgenykuzin.app.gui;


import com.github.evgenykuzin.app.cnfg.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

import static com.github.evgenykuzin.app.cnfg.AppConfig.EXEL_PATH_KEY;


public class DropPanel extends JPanel implements DropTargetListener {
    DropTarget dt;
    TextArea ta;

    public DropPanel(Container container, TextArea ta) {
        super();
        setSize(1,1);
        container.add(
                new JLabel("Drop a file here:"),
                BorderLayout.NORTH);
        this.ta = ta;
        ta.setBackground(Color.white);
        //container.add(ta, BorderLayout.CENTER);
        dt = new DropTarget(ta, this);
        setVisible(true);
    }

    public void dragEnter(DropTargetDragEvent dtde) {
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void dragOver(DropTargetDragEvent dtde) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void drop(DropTargetDropEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            DataFlavor[] flavors = tr.getTransferDataFlavors();
            for (DataFlavor flavor : flavors) {
                if (flavor.isFlavorJavaFileListType()) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    List<File> list = (List<File>) tr.getTransferData(flavor);
                    String string = list.get(0).getAbsolutePath();
                    ta.append(string + "\n");
                    save(string);
                    dtde.dropComplete(true);
                    return;
                } else if (flavor.isFlavorSerializedObjectType()) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Object o = tr.getTransferData(flavor);
                    save((String) o);
                    dtde.dropComplete(true);
                    return;
                }
            }
            dtde.rejectDrop();
        } catch (Exception e) {
            e.printStackTrace();
            dtde.rejectDrop();
        }
    }

    public void save(String string) {
        AppConfig.preferences.put(EXEL_PATH_KEY, string);
    }
}