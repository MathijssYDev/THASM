package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import src.THASMCompiler;
public class THASMInterface extends JFrame {
    private JTextField inputFileField;
    private JTextField outputFileField;
    private JTextArea consoleTextArea;

    public THASMInterface() {
        setTitle("THASM Compiler Interface");
        setSize(800, 600); // Initial size
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 1));
        JPanel fileInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel inputFileLabel = new JLabel("Input File:");
        inputFileField = new JTextField(40); // Set the input field width
        inputFileField.setPreferredSize(new Dimension(600, 30)); // Set the input field height
        fileInputPanel.add(inputFileLabel);
        fileInputPanel.add(inputFileField);

        JPanel fileOutputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel outputFileLabel = new JLabel("Output File:");
        outputFileField = new JTextField(40); // Set the input field width
        outputFileField.setPreferredSize(new Dimension(600, 30)); // Set the input field height
        fileOutputPanel.add(outputFileLabel);
        fileOutputPanel.add(outputFileField);

        inputPanel.add(fileInputPanel);
        inputPanel.add(fileOutputPanel);

        // Compile Button Panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton compileButton = new JButton("Compile");
        compileButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 60)); // Max width, fixed height
        compileButton.setBackground(new Color(255, 200, 200)); // Light red
        compileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                compile();
            }
        });
        buttonPanel.add(compileButton, BorderLayout.CENTER);

        // Console Panel
        consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);
        JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);
        consoleScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Add components to the frame
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(consoleScrollPane, BorderLayout.SOUTH);
    }

    private void compile() {
        String inputFile = inputFileField.getText().trim();
        String outputFile = outputFileField.getText().trim();

        if (inputFile.isEmpty() || outputFile.isEmpty()) {
            consoleTextArea.append("Error: Please provide both input and output file locations.\n");
            return;
        }

        try {
            // Call the compile method of THASMCompiler class directly
            String[] args = new String[]{inputFile, outputFile};
            new src.THASMCompiler(args,consoleTextArea);
        } catch (Exception e) {
            consoleTextArea.append("Error during compilation: " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new THASMInterface().setVisible(true);
            }
        });
    }
}
