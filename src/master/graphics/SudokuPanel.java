package master.graphics;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class SudokuPanel extends JPanel {
    private static final int GRID_SIZE = 9;
    private static final int CELL_SIZE = 50;
    private SudokuCell[][] cells = new SudokuCell[GRID_SIZE][GRID_SIZE];
    private JButton getStringButton;
    private JButton setStringButton;

    private JTextArea stringArea;

    public SudokuPanel() {
        setLayout(new BorderLayout());

        // Create the grid panel with padding
        JPanel gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 0, 0));
        gridPanel.setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));

        Font cellFont = new Font("Monospaced", Font.BOLD, 20);

        // Initialize cells and set custom borders
        for (int row = 0; row < GRID_SIZE; ++row) {
            for (int col = 0; col < GRID_SIZE; ++col) {
                cells[row][col] = new SudokuCell();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(cellFont);

                // Set custom borders based on 3x3 box position
                cells[row][col].setBorder(getCellBorder(row, col));
                gridPanel.add(cells[row][col]);
            }
        }

        // Control panel with button and text area
        JPanel controlPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        getStringButton = new JButton("Get String");
        setStringButton = new JButton("Set String");
        stringArea = new JTextArea(3, 20);

        getStringButton.addActionListener(actionEvent -> {
            String sudokuString = generateSudokuString();
            stringArea.setText(sudokuString);
        });

        setStringButton.addActionListener(actionEvent -> loadSudoku(stringArea.getText()));

        buttonPanel.add(getStringButton);
        buttonPanel.add(setStringButton);

        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        controlPanel.add(new JScrollPane(stringArea), BorderLayout.CENTER);

        // Add panels to the main panel
        add(gridPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE + 20, GRID_SIZE * CELL_SIZE + 150));
    }

    // Method to create custom borders for each cell
    private Border getCellBorder(int row, int col) {
        int top = (row % 3 == 0) ? 3 : 1;
        int left = (col % 3 == 0) ? 3 : 1;
        int bottom = (row == GRID_SIZE - 1) ? 3 : 1;
        int right = (col == GRID_SIZE - 1) ? 3 : 1;

        return BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK);
    }

    public String generateSudokuString() {
        StringBuilder sb = new StringBuilder(81);
        for (int row = 0; row < GRID_SIZE; ++row) {
            for (int col = 0; col < GRID_SIZE; ++col) {
                String text = cells[row][col].getText().trim();
                if (text.isEmpty()) sb.append('.');
                else sb.append(text.charAt(0));
            }
        }
        return sb.toString();
    }

    public void loadSudoku(String sudoku) {
        if (sudoku.length() != 81) throw new IllegalArgumentException("Sudoku string must be exactly 81 characters");
        for (int i = 0; i < 81; ++i) {
            int row = i / GRID_SIZE;
            int col = i % GRID_SIZE;
            char ch = sudoku.charAt(i);
            if (ch != '.') {
                cells[row][col].setText(String.valueOf(ch));
                cells[row][col].setEditable(false);
            } else {
                cells[row][col].setText("");
                cells[row][col].setEditable(true);
            }
        }
    }
}

class SudokuCell extends JTextField {
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(50, 50);
    }
}