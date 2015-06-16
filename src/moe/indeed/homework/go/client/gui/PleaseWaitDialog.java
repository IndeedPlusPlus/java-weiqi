package moe.indeed.homework.go.client.gui;

import javax.swing.*;
import java.awt.*;

public class PleaseWaitDialog extends JDialog {
    private JLabel text = new JLabel("Please wait.", SwingConstants.CENTER);

    public void setText(String text) {
        this.text.setText(text);
    }

    public PleaseWaitDialog(Frame parent) {
        super(parent , "Please Wait");
        this.setLayout(new BorderLayout());
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(bar);
        panel.add(text);
        panel.setBorder(BorderFactory.createEmptyBorder(30 , 30 , 30 ,30));
        this.add(panel, BorderLayout.CENTER);
        this.pack();
        this.setLocationRelativeTo(null);
    }
}
