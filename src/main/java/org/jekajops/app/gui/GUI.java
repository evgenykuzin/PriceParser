package org.jekajops.app.gui;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionListener;

import static java.awt.BorderLayout.*;
import static org.jekajops.app.cnfg.AppConfig.loger;

public class GUI {
    private JFrame frame;
    public static final TextArea LOG_COMPONENT = initLogComponent();
    private static final int SIZE = 500;
    private ActionListener runActionListener;
    public void start() {
        frame = getFrame();
        frame.add(getMainPanel());
        var simpleDnD = new DropPanel(frame.getContentPane(), LOG_COMPONENT);
        frame.add(simpleDnD, AFTER_LAST_LINE);
    }

    public void setRunActionListener(ActionListener actionListener) {
        this.runActionListener = actionListener;
    }

    private JFrame getFrame() {
        var frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(SIZE, SIZE));
        frame.setVisible(true);
        frame.pack();
        return frame;
    }

    private JPanel getMainPanel() {
        var panel = new JPanel();
        panel.setPreferredSize(new Dimension(SIZE, SIZE));
        panel.setLayout(new BorderLayout());
        panel.add(getButton("run", runActionListener), PAGE_START);
        panel.add(LOG_COMPONENT, AFTER_LINE_ENDS);
        return panel;
    }

    private JLabel getLabel(String text) {
        var label = new JLabel(text);
        initSmallComponent(label);
        return label;
    }

    private JButton getButton(String text, ActionListener actionListener) {
        var button = new JButton(text);
        button.addActionListener(actionListener);
        initSmallComponent(button);
        return button;
    }

    private void initSmallComponent(JComponent component) {
        component.setPreferredSize(new Dimension(100, 30));
        component.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    }

    private static TextArea initLogComponent(){
        var textArea = new TextArea();
        //textArea.setPreferredSize(new Dimension(200, 200));
        return textArea;
    }

    private static void log(String from, String msg) {
        loger.log(from, msg);
    }

    private static void log(String msg) {
        log("main", msg);
    }

}
