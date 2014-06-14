package de.fau.cs.mad.fly;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.fau.cs.mad.fly.game.GameController;
import de.fau.cs.mad.fly.player.Player;
import de.fau.cs.mad.fly.res.Assets;
import de.fau.cs.mad.fly.res.Level;
import de.fau.cs.mad.fly.ui.*;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.NodeVisitor;

import java.io.IOException;
import java.io.StringReader;

/**
 * Manages the different game screens.
 * <p>
 * Includes screens for SplashScreen, LoadingScreen, GameScreen, MainMenuScreen,
 * OptionScreen, HelpScreen and LevelChooserScreen.
 * <p>
 * All screens should be loaded only when they are needed. To create an instance
 * of all of them takes about 4 seconds on a Nex5.
 * 
 * @author Tobias Zangl
 */
public class Fly extends Game {
	private SplashScreen splashScreen;
	private LoadingScreen loadingScreen;
	private LevelChooserScreen levelChooserScreen;
	private MainMenuScreen mainMenuScreen;
	private SettingScreen settingScreen;
	private GameScreen gameScreen;
	
	private Player player;
	private GameController gameController;

	private Skin skin;
	private ShapeRenderer shapeRenderer;

	@Override
	public void create() {

		String src = Gdx.files.internal("bla.js").readString();

		CompilerEnvirons env = new CompilerEnvirons();
		env.setRecoverFromErrors(true);
		env.setGenerateDebugInfo(true);
		env.setRecordingComments(true);

		StringReader strReader = new StringReader(src);

		IRFactory factory = new IRFactory(env);
		try {
			AstRoot root = factory.parse(strReader, null, 0);
			System.out.println(root.debugPrint());
			root.visit(new NodeVisitor() {
				@Override
				public boolean visit(AstNode node) {
					if ( node.getType() == 38 ) { // Function
						FunctionCall c = (FunctionCall) node;
						for ( AstNode n : c.getArguments() )
							System.out.println("shortname = " + n.shortName() + ", source = " + n.toSource());
					} else if ( node.getType() == 39 ) { // Name
						System.out.println(node.getScope());
					}

					return true;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.exit(0);

		Bullet.init();
		Assets.init();

		createSkin();
		shapeRenderer = new ShapeRenderer();

		player = new Player();
		player.createSettings(skin);

		setMainMenuScreen();
		// disabled for debugging reasons
		// setSplashScreen();
	}

	/**
	 * Creates the Skin for the UI.
	 */
	public void createSkin() {
		FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(
				Gdx.files.internal("OpenSans-Regular.ttf"));
		FreeTypeFontParameter fontParameter = new FreeTypeFontParameter();
		fontParameter.size = (int) (Gdx.graphics.getWidth() * 0.04);
		BitmapFont bitmapFont = fontGenerator.generateFont(fontParameter);
		fontGenerator.dispose();

		skin = new Skin();
		skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin.atlas")));
		skin.add("default-font", bitmapFont);

		skin.load(Gdx.files.internal("uiskin.json"));
	}
	
	/**
	 * Getter for the Player.
	 */
	public Player getPlayer() {
		return player;
	}
	
	public GameController getGameController() {
		return gameController;
	}

	/**
	 * Setter for the Player.
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * Getter for the Skin.
	 */
	public Skin getSkin() {
		return skin;
	}
	
	/**
	 * Getter for the ShapeRenderer.
	 */
	public ShapeRenderer getShapeRenderer() {
		return shapeRenderer;
	}

	/**
	 * Lazy loading of screen to choose level.
	 */
	public void setLevelChoosingScreen() {
		if (levelChooserScreen == null) {
			levelChooserScreen = new LevelChooserScreen();
		}
		setScreen(levelChooserScreen);
	}

	/**
	 * Switches the current Screen to the SplashScreen.
	 */
	public void setSplashScreen() {
		if (splashScreen == null) {
			splashScreen = new SplashScreen(this);
		}
		setScreen(splashScreen);
	}

	public void loadLevel() {
		Level l = player.getLastLevel();
		if ( l == null )
			loadLevel(LevelChooserScreen.getLevelList().get(0));
		else loadLevel(l);
	}

	/**
	 * Switches the current Screen to the LoadingScreen.
	 */
	public void loadLevel(Level.Head head) {
		Gdx.app.log("Fly.loadLevel", "Telling AssetManager to load level...");
		String f = head.file.path();
		Assets.manager.load(f, Level.class);
		Assets.manager.finishLoading();
		Gdx.app.log("Fly.loadLevel", "Level ready. Displaying...");
		loadLevel(Assets.manager.get(f, Level.class));
	}

	public void loadLevel(Level level) {
		player.setLastLevel(level);

		if (loadingScreen == null) {
			loadingScreen = new LoadingScreen(this);
		}
		// TODO: make loading asynchronous to the loading screen and display the progress
		setScreen(loadingScreen);
		Gdx.app.log("Fly.loadLevel", "LoadingScreen on.");
		// TODO: this should be level dependent in the future

		GameController.Builder builder = new GameController.Builder();
		builder.init(this);

		gameController = builder.build();
		Gdx.app.log("Fly.loadLevel", "Controller built.");
		gameController.loadGame();
		Gdx.app.log("Fly.loadLevel", "Game loaded.");
	}

	/**
	 * set game screen as current screen.
	 */
	public void setGameScreen() {
		if (gameScreen == null) {
			gameScreen = new GameScreen(this);
		}
		Gdx.app.log("Fly.setGameScreen", "Just a little bit more...");
		setScreen(gameScreen);
	}

	/**
	 * Switches the current Screen to the MainMenuScreen.
	 */
	public void setMainMenuScreen() {
		if (mainMenuScreen == null) {
			mainMenuScreen = new MainMenuScreen();
		}
		setScreen(mainMenuScreen);
	}

	/**
	 * Switches the current Screen to the SettingScreen.
	 */
	public void setSettingScreen() {
		if (settingScreen == null) {
			settingScreen = new SettingScreen();
		}
		setScreen(settingScreen);
	}
}