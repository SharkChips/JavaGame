package com.albertzhang.javagame;

import java.awt.EventQueue;
import java.awt.Font;
import java.util.Hashtable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Launcher {

    private JFrame frame;

    public static final String NAME = "To Be Named";

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
	frame.setBounds(100, 100, 256, 350);
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

	JLabel title = new JLabel(NAME);
	springLayout.putConstraint(SpringLayout.NORTH, title, 10, SpringLayout.NORTH, frame.getContentPane());
	springLayout.putConstraint(SpringLayout.WEST, title, 10, SpringLayout.WEST, frame.getContentPane());
	springLayout.putConstraint(SpringLayout.EAST, title, 230, SpringLayout.WEST, frame.getContentPane());
	title.setFont(new Font("Tempus Sans ITC", Font.PLAIN, 36));
	title.setHorizontalAlignment(SwingConstants.CENTER);
	frame.getContentPane().add(title);

	JLabel diffLbl = new JLabel("Difficulty");
	springLayout.putConstraint(SpringLayout.SOUTH, title, -6, SpringLayout.NORTH, diffLbl);
	springLayout.putConstraint(SpringLayout.WEST, diffLbl, 0, SpringLayout.WEST, playBtn);
	diffLbl.setFont(new Font("Tahoma", Font.BOLD, 12));
	frame.getContentPane().add(diffLbl);

	JSlider slider = new JSlider();
	springLayout.putConstraint(SpringLayout.SOUTH, diffLbl, -6, SpringLayout.NORTH, slider);
	springLayout.putConstraint(SpringLayout.WEST, slider, 0, SpringLayout.WEST, playBtn);
	springLayout.putConstraint(SpringLayout.EAST, slider, -10, SpringLayout.EAST, frame.getContentPane());
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
	springLayout.putConstraint(SpringLayout.SOUTH, slider, -17, SpringLayout.NORTH, playersLbl);
	springLayout.putConstraint(SpringLayout.WEST, playersLbl, 0, SpringLayout.WEST, playBtn);
	playersLbl.setFont(new Font("Tahoma", Font.BOLD, 12));
	frame.getContentPane().add(playersLbl);

	JComboBox<String> numPlayersBox = new JComboBox<String>();
	springLayout.putConstraint(SpringLayout.SOUTH, playersLbl, -6, SpringLayout.NORTH, numPlayersBox);
	springLayout.putConstraint(SpringLayout.WEST, numPlayersBox, 0, SpringLayout.WEST, playBtn);
	springLayout.putConstraint(SpringLayout.EAST, numPlayersBox, -20, SpringLayout.EAST, frame.getContentPane());
	numPlayersBox.setModel(new DefaultComboBoxModel<String>(new String[] { "One Player", "Other modes not implemented yet" }));
	frame.getContentPane().add(numPlayersBox);

	frame.getRootPane().setDefaultButton(playBtn);

	JLabel lblDebug = new JLabel("Debug");
	springLayout.putConstraint(SpringLayout.SOUTH, numPlayersBox, -6, SpringLayout.NORTH, lblDebug);
	springLayout.putConstraint(SpringLayout.WEST, lblDebug, 0, SpringLayout.WEST, playBtn);
	lblDebug.setFont(new Font("Tahoma", Font.BOLD, 12));
	frame.getContentPane().add(lblDebug);

	JCheckBox dbgGra = new JCheckBox("Graphics");
	springLayout.putConstraint(SpringLayout.NORTH, dbgGra, 217, SpringLayout.NORTH, frame.getContentPane());
	springLayout.putConstraint(SpringLayout.SOUTH, lblDebug, -6, SpringLayout.NORTH, dbgGra);
	springLayout.putConstraint(SpringLayout.WEST, dbgGra, 0, SpringLayout.WEST, playBtn);
	frame.getContentPane().add(dbgGra);

	JCheckBox dbgLog = new JCheckBox("Logic");
	springLayout.putConstraint(SpringLayout.NORTH, dbgLog, 0, SpringLayout.NORTH, dbgGra);
	springLayout.putConstraint(SpringLayout.EAST, dbgLog, -108, SpringLayout.EAST, frame.getContentPane());
	frame.getContentPane().add(dbgLog);

	JCheckBox dbgKey = new JCheckBox("Keyhandling");
	springLayout.putConstraint(SpringLayout.NORTH, dbgKey, 0, SpringLayout.NORTH, dbgGra);
	springLayout.putConstraint(SpringLayout.WEST, dbgKey, 6, SpringLayout.EAST, dbgLog);
	frame.getContentPane().add(dbgKey);
	playBtn.requestFocusInWindow();

	playBtn.addActionListener(listener -> {
	    new Main(slider.getValue(), dbgGra.isSelected(), dbgLog.isSelected(), dbgKey.isSelected());
	});
    }
}
