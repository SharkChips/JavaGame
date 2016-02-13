package com.albertzhang.javagame;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Launcher {

    private JFrame frame;

    private static final String VERSION = "Alpha 0.1-" + Launcher.class.getPackage().getImplementationVersion();
    private static final String NAME = "Space Evasion" + VERSION;

    public static String getName() {
	return NAME;
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	EventQueue.invokeLater(() -> {
	    try {
		Launcher window = new Launcher();
		window.frame.setVisible(true);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	});
    }

    /**
     * Create the application.
     */
    public Launcher() {
	initialize();
    }

    /**
     * Initialize the contents of the frame.
     * 
     * @wbp.parser.entryPoint
     */
    @SuppressWarnings("serial")
    private void initialize() {
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
	    e.printStackTrace();
	}
	frame = new JFrame(NAME);
	frame.setBounds(100, 100, 256, 450);
	frame.setResizable(false);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	SpringLayout springLayout = new SpringLayout();
	frame.getContentPane().setLayout(springLayout);

	JButton playBtn = new JButton("Play");
	playBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
	springLayout.putConstraint(SpringLayout.NORTH, playBtn, -53, SpringLayout.SOUTH, frame.getContentPane());
	springLayout.putConstraint(SpringLayout.WEST, playBtn, 10, SpringLayout.WEST, frame.getContentPane());
	springLayout.putConstraint(SpringLayout.SOUTH, playBtn, -10, SpringLayout.SOUTH, frame.getContentPane());
	springLayout.putConstraint(SpringLayout.EAST, playBtn, -10, SpringLayout.EAST, frame.getContentPane());
	frame.getContentPane().add(playBtn);

	JLabel title = new JLabel("Space");
	springLayout.putConstraint(SpringLayout.NORTH, title, 10, SpringLayout.NORTH, frame.getContentPane());
	springLayout.putConstraint(SpringLayout.WEST, title, 0, SpringLayout.WEST, playBtn);
	springLayout.putConstraint(SpringLayout.SOUTH, title, 53, SpringLayout.NORTH, frame.getContentPane());
	springLayout.putConstraint(SpringLayout.EAST, title, 0, SpringLayout.EAST, playBtn);
	JLabel title2 = new JLabel("Evasion");
	springLayout.putConstraint(SpringLayout.WEST, title2, 0, SpringLayout.WEST, playBtn);
	springLayout.putConstraint(SpringLayout.EAST, title2, 0, SpringLayout.EAST, playBtn);

	Font f;
	try {
	    f = Font.createFont(Font.TRUETYPE_FONT, RenderEngine.class.getClassLoader().getResourceAsStream("Nasaliza.ttf")).deriveFont(Font.PLAIN, 32f);
	} catch (FontFormatException | IOException e) {
	    e.printStackTrace();
	    f = this.frame.getFont().deriveFont(32f);
	}

	title.setFont(f);
	title.setHorizontalAlignment(SwingConstants.CENTER);
	title2.setFont(f);
	title2.setHorizontalAlignment(SwingConstants.CENTER);
	frame.getContentPane().add(title);
	frame.getContentPane().add(title2);

	JLabel diffLbl = new JLabel("Difficulty");
	springLayout.putConstraint(SpringLayout.NORTH, title2, -37, SpringLayout.NORTH, diffLbl);
	springLayout.putConstraint(SpringLayout.SOUTH, title2, -11, SpringLayout.NORTH, diffLbl);
	springLayout.putConstraint(SpringLayout.NORTH, diffLbl, 43, SpringLayout.SOUTH, title);
	springLayout.putConstraint(SpringLayout.WEST, diffLbl, 0, SpringLayout.WEST, playBtn);
	diffLbl.setFont(new Font("Tahoma", Font.BOLD, 12));
	frame.getContentPane().add(diffLbl);

	JSlider slider = new JSlider();
	springLayout.putConstraint(SpringLayout.NORTH, slider, 6, SpringLayout.SOUTH, diffLbl);
	springLayout.putConstraint(SpringLayout.WEST, slider, 0, SpringLayout.WEST, playBtn);
	springLayout.putConstraint(SpringLayout.EAST, slider, 0, SpringLayout.EAST, playBtn);
	slider.setFocusable(false);
	slider.setMaximum(3);
	slider.setPaintLabels(true);
	slider.setLabelTable(new Hashtable<Integer, JLabel>() {
	    {
		put(0, new JLabel("Easy"));
		put(1, new JLabel("Medium"));
		put(2, new JLabel("Hard"));
		put(3, new JLabel("Insane?"));

	    }
	});
	slider.setValue(1);
	frame.getContentPane().add(slider);

	JLabel playersLbl = new JLabel("Players");
	springLayout.putConstraint(SpringLayout.NORTH, playersLbl, 6, SpringLayout.SOUTH, slider);
	springLayout.putConstraint(SpringLayout.WEST, playersLbl, 0, SpringLayout.WEST, playBtn);
	playersLbl.setFont(new Font("Tahoma", Font.BOLD, 12));
	frame.getContentPane().add(playersLbl);

	JComboBox<String> numPlayersBox = new JComboBox<String>();
	springLayout.putConstraint(SpringLayout.NORTH, numPlayersBox, 6, SpringLayout.SOUTH, playersLbl);
	springLayout.putConstraint(SpringLayout.WEST, numPlayersBox, 10, SpringLayout.WEST, frame.getContentPane());
	springLayout.putConstraint(SpringLayout.EAST, numPlayersBox, -20, SpringLayout.EAST, frame.getContentPane());
	numPlayersBox.setEnabled(false);
	numPlayersBox.setFocusable(false);
	numPlayersBox.setModel(new DefaultComboBoxModel<String>(new String[] { "One Player", "Other modes not implemented yet" }));
	frame.getContentPane().add(numPlayersBox);

	frame.getRootPane().setDefaultButton(playBtn);

	JLabel lblDebug = new JLabel("Debug");
	springLayout.putConstraint(SpringLayout.WEST, lblDebug, 13, SpringLayout.WEST, frame.getContentPane());
	springLayout.putConstraint(SpringLayout.EAST, lblDebug, 53, SpringLayout.WEST, frame.getContentPane());
	lblDebug.setFont(new Font("Tahoma", Font.BOLD, 12));
	frame.getContentPane().add(lblDebug);

	JCheckBox dbgGra = new JCheckBox("Graphics");
	springLayout.putConstraint(SpringLayout.NORTH, dbgGra, 6, SpringLayout.SOUTH, lblDebug);
	springLayout.putConstraint(SpringLayout.WEST, dbgGra, 0, SpringLayout.WEST, playBtn);
	dbgGra.setFocusable(false);
	frame.getContentPane().add(dbgGra);

	JCheckBox dbgLog = new JCheckBox("Logic");
	springLayout.putConstraint(SpringLayout.NORTH, dbgLog, 0, SpringLayout.NORTH, dbgGra);
	springLayout.putConstraint(SpringLayout.WEST, dbgLog, 6, SpringLayout.EAST, dbgGra);
	dbgLog.setFocusable(false);
	frame.getContentPane().add(dbgLog);

	JCheckBox dbgKey = new JCheckBox("Keyhandling");
	springLayout.putConstraint(SpringLayout.NORTH, dbgKey, 0, SpringLayout.NORTH, dbgGra);
	springLayout.putConstraint(SpringLayout.WEST, dbgKey, 6, SpringLayout.EAST, dbgLog);
	dbgKey.setFocusable(false);
	frame.getContentPane().add(dbgKey);

	JLabel lblWin = new JLabel("Graphics");
	springLayout.putConstraint(SpringLayout.NORTH, lblWin, 6, SpringLayout.SOUTH, numPlayersBox);
	springLayout.putConstraint(SpringLayout.WEST, lblWin, 0, SpringLayout.WEST, playBtn);
	lblWin.setFont(new Font("Tahoma", Font.BOLD, 12));
	frame.getContentPane().add(lblWin);

	JLabel lblWidth = new JLabel("Width");
	springLayout.putConstraint(SpringLayout.NORTH, lblWidth, 6, SpringLayout.SOUTH, lblWin);
	springLayout.putConstraint(SpringLayout.WEST, lblWidth, 0, SpringLayout.WEST, playBtn);
	frame.getContentPane().add(lblWidth);

	JSpinner width = new JSpinner();
	springLayout.putConstraint(SpringLayout.NORTH, width, 6, SpringLayout.SOUTH, lblWidth);
	springLayout.putConstraint(SpringLayout.WEST, width, 0, SpringLayout.WEST, playBtn);
	springLayout.putConstraint(SpringLayout.SOUTH, width, 32, SpringLayout.SOUTH, lblWidth);
	springLayout.putConstraint(SpringLayout.EAST, width, 111, SpringLayout.WEST, frame.getContentPane());
	width.setFocusable(false);
	width.setValue(1024);
	frame.getContentPane().add(width);

	JSpinner height = new JSpinner();
	springLayout.putConstraint(SpringLayout.NORTH, height, 0, SpringLayout.NORTH, width);
	springLayout.putConstraint(SpringLayout.WEST, height, 6, SpringLayout.EAST, width);
	springLayout.putConstraint(SpringLayout.SOUTH, height, 26, SpringLayout.NORTH, width);
	springLayout.putConstraint(SpringLayout.EAST, height, -19, SpringLayout.EAST, frame.getContentPane());
	height.setFocusable(false);
	height.setValue(768);
	frame.getContentPane().add(height);

	JLabel lblHeight = new JLabel("Height");
	springLayout.putConstraint(SpringLayout.NORTH, lblHeight, 0, SpringLayout.NORTH, lblWidth);
	springLayout.putConstraint(SpringLayout.WEST, lblHeight, 79, SpringLayout.EAST, lblWidth);
	frame.getContentPane().add(lblHeight);

	JCheckBox chckFullScreen = new JCheckBox("Fullscreen");
	springLayout.putConstraint(SpringLayout.NORTH, lblDebug, 6, SpringLayout.SOUTH, chckFullScreen);
	springLayout.putConstraint(SpringLayout.NORTH, chckFullScreen, 6, SpringLayout.SOUTH, width);
	springLayout.putConstraint(SpringLayout.WEST, chckFullScreen, 0, SpringLayout.WEST, playBtn);
	frame.getContentPane().add(chckFullScreen);

	chckFullScreen.addActionListener(listener -> {
	    if (height.isEnabled()) {
		width.setValue((int) frame.getGraphicsConfiguration().getBounds().getWidth());
		height.setValue((int) frame.getGraphicsConfiguration().getBounds().getHeight());
		height.setEnabled(false);
		width.setEnabled(false);
	    } else {
		height.setEnabled(true);
		width.setEnabled(true);
	    }
	});

	playBtn.requestFocusInWindow();
	playBtn.addActionListener(listener -> {
	    new Main((int) width.getValue(), (int) height.getValue(), slider.getValue(), dbgGra.isSelected(), dbgLog.isSelected(), dbgKey.isSelected(), chckFullScreen.isSelected());
	    this.frame.dispose();
	});
    }
}
