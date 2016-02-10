package com.albertzhang.javagame;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Main extends JFrame {

    private static final int INITIAL_WIDTH = 800;
    private static final int INITIAL_HEIGHT = 600;
    static final boolean DEBUG = true;

    public Main() {
	super("Window"); // prints title in title bar of window
	setSize(INITIAL_WIDTH, INITIAL_HEIGHT);

	// Instantiates LogicEngine
	LogicEngine logic = new LogicEngine();
	logic.start();

	// Makes new RenderEngine and gives tells it to render a LogicEngine
	RenderEngine render = new RenderEngine(logic);
	render.start();

	getContentPane().add(render); // Add render to frame

	// Instantiates KeyHandlingEngine and activates it
	KeyHandling keyHandlingEngine = new KeyHandling(logic);
	keyHandlingEngine.start();
	addKeyListener(keyHandlingEngine);

	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setVisible(true);
    }

    public static void main(String args[]) {
	new Main();
    }
}