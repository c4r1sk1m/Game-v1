package de.nerogar.gameV1;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.input.Keyboard;

import de.nerogar.gameV1.debug.DebugFelk;
import de.nerogar.gameV1.debug.DebugNerogar;
import de.nerogar.gameV1.gui.*;
import de.nerogar.gameV1.level.Entity;
import de.nerogar.gameV1.level.Tile;
import de.nerogar.gameV1.sound.Sound;

public class Game implements Runnable {
	public boolean running = true;

	public World world;
	public Game game;
	public GuiList guiList = new GuiList();
	public RenderEngine renderEngine = RenderEngine.instance;
	private long[] stressTimes = new long[4];
	public long stressTimeMainloop = 0;
	public long stressTimeRender = 0;
	public long stressTimeUpdate = 0;
	public long stressTimeTotal = 0;
	public DebugFelk debugFelk = new DebugFelk(this);
	public DebugNerogar debugNerogar = new DebugNerogar(this);

	public void run() {
		this.game = this;

		try {

			init();

			Timer.instance.registerEvent("gc", 10);

			InputHandler.loadGamepad();
			InputHandler.registerGamepadButton("start", "7", 0.25f);
			InputHandler.registerGamepadButton("back", "6", 0.25f);

			debugFelk.startup();
			debugNerogar.startup();

			while (running) {
				stressTimes[0] = System.nanoTime();
				mainloop();
				stressTimes[1] = System.nanoTime();
				render();
				stressTimes[2] = System.nanoTime();
				Display.update();
				Display.sync(GameOptions.instance.getIntOption("fps"));
				stressTimes[3] = System.nanoTime();
				if (Timer.instance.shellExecute("gc")) {
					System.gc();
					System.out.println("gc");
				}

				updateStressTimes();
				//InputHandler.printGamepadButtons();
			}

			debugFelk.end();
			debugNerogar.end();
			AL.destroy();

			if (world.isLoaded) world.closeWorld();
			renderEngine.cleanup();
			GameOptions.instance.save();

			//} catch (LWJGLException | IOException e) {
		} catch (Exception e) {
			Logger.printThrowable(e, "gotta catch 'em all", false);
		}

	}

	private void updateStressTimes() {
		stressTimeMainloop = stressTimes[1] - stressTimes[0];
		stressTimeRender = stressTimes[2] - stressTimes[1];
		stressTimeUpdate = stressTimes[3] - stressTimes[2];
		stressTimeTotal = System.nanoTime() - stressTimes[0];
	}

	private void mainloop() {
		InputHandler.update(game);

		if (InputHandler.isKeyPressed(Keyboard.KEY_F3) || InputHandler.isGamepadButtonPressed("back")) {
			//GameOptions.instance.setOption("debug", String.valueOf(!GameOptions.instance.getBoolOption("debug")));
			GameOptions.instance.switchBoolOption("debug");
			if (!GameOptions.instance.getBoolOption("debug")) {
				guiList.removeGui(new GuiDebug(game));
			} else {
				guiList.addGui(new GuiDebug(game));
			}
		}

		if (Display.isCloseRequested()) { // Exit if window is closed
			running = false;
		}

		if (InputHandler.isKeyPressed(Keyboard.KEY_F11)) {
			renderEngine.toggleFullscreen();
		}

		debugFelk.run();
		debugNerogar.run();

		//update game logics
		Timer.instance.update();
		guiList.update();
		if (!guiList.pauseGame()) {
			world.update();
		}
		Sound.setListener(new Vector3d(world.camera.scrollX, world.camera.scrollY, world.camera.scrollZ), new Vector3d(), new Vector3d());
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear The Screen And The Depth Buffer
		world.render();
		guiList.render();
	}

	private void init() {
		RenderHelper.renderLoadingScreen();

		world = new World(game);
		if (GameOptions.instance.getBoolOption("debug")) {
			guiList.addGui(new GuiDebug(game));
		}
		guiList.addGui(new GuiMain(game));
		Entity.initEntityList(game);
		Tile.initTileList();
		Timer.instance.init();

		try {
			AL.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Game game = new Game();
		game.run();
	}
}