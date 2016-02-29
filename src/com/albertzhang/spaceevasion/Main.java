package com.albertzhang.spaceevasion;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Main extends JFrame {

    public static int width;
    public static int height;

    public Main(int width, int height, int difficulty, boolean graphicsDebug, boolean logicDebug, boolean keyDebug, boolean audioDebug, boolean fullscreen, double musicVol, double soundVol) {
	super(Launcher.getName()); // Prints title in title bar of window
	Main.width = width;
	Main.height = height;
	setSize(Main.width, Main.height);
	if (fullscreen) {
	    setUndecorated(true);
	    setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	// Instantiates LogicEngine
	LogicEngine logic = new LogicEngine(difficulty + 1, logicDebug); // +1 because launcher goes from 0 to 3
	logic.start();

	// Makes new RenderEngine and gives tells it to render a LogicEngine
	RenderEngine render = new RenderEngine(logic, graphicsDebug);
	render.start();

	getContentPane().add(render); // Add render to frame

	// Instantiates KeyHandlingEngine and activates it
	KeyHandling keyHandlingEngine = new KeyHandling(logic, keyDebug);
	keyHandlingEngine.start();
	addKeyListener(keyHandlingEngine);

	// If we want to play music, make a AudioEngine
	if (musicVol >= 0.01d) {
	    AudioEngine music = new AudioEngine(musicVol, soundVol, audioDebug);
	    music.start();
	}

	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setResizable(false);
	setVisible(true);
    }
}