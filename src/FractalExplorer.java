/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.geom.Rectangle2D;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.filechooser.*;
import javax.imageio.*;



/**
 *
 * @author sgoldber
 */
public class FractalExplorer {
    public JFrame frame;
    
    private final int size;
    
    private JImageDisplay display;
    private FractalGenerator generator;
    private Rectangle2D.Double range;
    
    private static final int NUM_FRACTALS = 3;
    private FractalGenerator[] generators;
    
    private JComboBox comboBox;
    private JButton buttonR;
    private JButton buttonS;
    
    private int rowsRemaining;
    
    public FractalExplorer(int size) {
        this.size = size;
        
        generators = new FractalGenerator[NUM_FRACTALS];
        generators[0] = new Mandelbrot();
        generators[1] = new Tricorn();
        generators[2] = new BurningShip();
        
        generator = generators[0];
        range = new Rectangle2D.Double();
        generator.getInitialRange(range);
        
    }
    
    public void createAndShowGUI() {
        frame = new JFrame("Fractal Explorer");
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setLayout(new BorderLayout());
        
        // add display to draw fractals on
        display = new JImageDisplay(size, size);
        display.addMouseListener(new MouseHandler());
        frame.getContentPane().add(display, BorderLayout.CENTER);
        
        // add panel with two buttons, to reset and to save
        buttonR = new JButton("ResetDisplay");
        buttonR.setActionCommand("reset");
        buttonR.addActionListener(new ActionHandler());
        
        buttonS = new JButton("SaveImage");
        buttonS.setActionCommand("save");
        buttonS.addActionListener(new ActionHandler());
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(buttonS);
        bottomPanel.add(buttonR);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        
        
        // add panel with label and combo-box, allowing switching fractals
        
        JLabel label = new JLabel("Fractal: ");
        
        comboBox = new JComboBox();
        for (int i = 0; i < NUM_FRACTALS; i++) {
            comboBox.addItem(generators[i]);
        }
        comboBox.setActionCommand("choose fractal");
        comboBox.addActionListener(new ActionHandler());
        
        JPanel topPanel = new JPanel();
        topPanel.add(label);
        topPanel.add(comboBox);
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        
        
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }
    
    private void drawFractal() {
        enableUI(false);
        rowsRemaining = size;
        
        for (int y = 0; y < size; y++) {
            FractalWorker worker = new FractalWorker(y);
            worker.execute();
        }
        
    }
    
    private class ActionHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("choose fractal")) {
                generator = (FractalGenerator) comboBox.getSelectedItem();
                
                // reset range and draw fractal
                generator.getInitialRange(range);
                drawFractal();
            }
            else if (command.equals("reset")) {
                // reset range and draw fractal
                generator.getInitialRange(range);
                drawFractal();
            }
            else if (command.equals("save")) {
                JFileChooser chooser = new JFileChooser();
                FileFilter filter = new FileNameExtensionFilter("PNG Images", "png");
                chooser.setFileFilter(filter);
                chooser.setAcceptAllFileFilterUsed(false);
                
                int returnValue = chooser.showSaveDialog(frame);
                
                if (returnValue != JFileChooser.APPROVE_OPTION)
                    return;
                
                try {
                    File file = chooser.getSelectedFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    ImageIO.write(display.getImage(), "png", fos);
                }
                catch (java.io.IOException exception) {
                    JOptionPane.showMessageDialog(frame, exception.getMessage(), "Cannot Save Image",  JOptionPane.ERROR_MESSAGE);
                }
                
            }
        }
    }
    
    private class MouseHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (rowsRemaining > 0)
                return;
            
            int x = e.getX();
            int y = e.getY();
            
            double centerX = FractalGenerator.getCoord(range.x, range.x + range.width, size, x);
            double centerY = FractalGenerator.getCoord(range.y, range.y + range.width, size, y);
            
            // recenter, and zoom range
            generator.recenterAndZoomRange(range, centerX, centerY, 0.5 /* scale */);
            
            // draw fractal
            drawFractal();
        }
    }
    
    private class FractalWorker extends SwingWorker<Object, Object> {
        private final int y;
        private int[] rgbColorArray;
        
        FractalWorker(int y) {
            this.y = y;
        }
        
        @Override
        public Object doInBackground() {
            rgbColorArray = new int[size];
            double xCoord, yCoord = FractalGenerator.getCoord(range.y, range.y + range.width, size, y);
            
            for (int x = 0; x < size; x++) {
                xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, size, x);
                
                double nIterations = generator.numIterations(xCoord, yCoord);
                if (nIterations == -1) {
                    rgbColorArray[x] = 0; // black
                }
                else {
                    rgbColorArray[x] = Color.HSBtoRGB(0.7f + (float) nIterations / 200f, 1f, 1f);
                }
            }
            
            return null;
        }
        
        @Override
        public void done() {
            for (int x = 0; x < size; x++) {
                display.drawPixel(x, y, rgbColorArray[x]);
            }
            
            display.repaint(0, 0, y, size, 1);
            rowsRemaining--;
            
            if (rowsRemaining == 0)
                enableUI(true);
        }
    }
    
    void enableUI(boolean val) {
        comboBox.setEnabled(val);
        buttonR.setEnabled(val);
        buttonS.setEnabled(val);
    }
    
    public static void main(String[] args) {
        FractalExplorer fractalExplorer = new FractalExplorer(800);
        
        fractalExplorer.createAndShowGUI();
        
        fractalExplorer.drawFractal();
    }
}

