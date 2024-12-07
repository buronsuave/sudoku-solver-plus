package master.graphics;

import javax.swing.*;
import java.awt.*;

public class LogPanel extends JPanel {
    private final JTextArea logArea;

    public LogPanel() {
        setLayout(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
        add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    public void log(String msg) {
        logArea.append(msg);
        logArea.setCaretPosition(logArea.getDocument().getLength());
        logArea.revalidate();
        logArea.repaint();
    }

    public void flush() {
        logArea.setText("");
        logArea.revalidate();
        logArea.repaint();
    }
}
