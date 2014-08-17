package de.fau.cs.mad.fly.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import de.fau.cs.mad.fly.Fly;
import de.fau.cs.mad.fly.I18n;
import de.fau.cs.mad.fly.Loader;
import de.fau.cs.mad.fly.profile.LevelGroupManager;
import de.fau.cs.mad.fly.profile.LevelProfile;
import de.fau.cs.mad.fly.profile.PlayerProfile;
import de.fau.cs.mad.fly.profile.PlayerProfileManager;
import de.fau.cs.mad.fly.res.Level;
import de.fau.cs.mad.fly.ui.help.HelpFrameText;
import de.fau.cs.mad.fly.ui.help.HelpFrameTextWithArrow;
import de.fau.cs.mad.fly.ui.help.HelpOverlay;
import de.fau.cs.mad.fly.ui.help.WithHelpOverlay;

/**
 * Displays the main menu with Start, Options, Help and Exit buttons.
 * 
 * @author Tobias Zangl
 */
public class MainMenuScreen extends BasicScreen implements WithHelpOverlay {
    
    private HelpOverlay helpOverlay;
    private boolean showHelpScreen = false;
    private Button continueButton;
    private Button chooseLevelButton;
    private Button choosePlaneButton;
    private Button statsButton;
    private ImageButton settingsButton;
    
    /**
     * Adds the main menu to the main menu screen.
     * <p>
     * Includes buttons for Start, Options, Help, Exit.
     */
    protected void generateContent() {
        // Create an instance of the PlayerManager, which needs an access to the
        // database
		
        
        Table table = new Table();
        table.defaults().width(viewport.getWorldWidth() / 3);
        table.setFillParent(true);
        table.pad(UI.Window.BORDER_SPACE, UI.Window.BORDER_SPACE, UI.Window.BORDER_SPACE, UI.Window.BORDER_SPACE);
        stage.addActor(table);
        
        TextButtonStyle textButtonStyle = skin.get(UI.Buttons.DEFAULT_STYLE, TextButtonStyle.class);
        continueButton = new TextButton(I18n.t("play"), textButtonStyle);
        chooseLevelButton = new TextButton(I18n.t("choose.level"), textButtonStyle);
        choosePlaneButton = new TextButton(I18n.t("choose.plane"), textButtonStyle);
        statsButton = new TextButton(I18n.t("highscores"), textButtonStyle);
        settingsButton = new ImageButton(skin.get(UI.Buttons.SETTING_BUTTON_STYLE, ImageButtonStyle.class));
        
        textButtonStyle = skin.get(UI.Buttons.BIG_FONT_SIZE_STYLE, TextButtonStyle.class);
        final Button helpButton = new TextButton("?", textButtonStyle);
        
        
        table.add(helpButton).width(UI.Buttons.MAIN_BUTTON_HEIGHT).height(UI.Buttons.MAIN_BUTTON_HEIGHT).left();
        table.add();
        table.add(settingsButton).width(UI.Buttons.MAIN_BUTTON_HEIGHT).height(UI.Buttons.MAIN_BUTTON_HEIGHT).right();
        table.row().expand();
        table.add();
        table.add(continueButton).width(UI.Buttons.MAIN_BUTTON_WIDTH).height(UI.Buttons.MAIN_BUTTON_HEIGHT);
        table.add();
        table.row().expand();
        table.add();
        table.add(chooseLevelButton).width(UI.Buttons.MAIN_BUTTON_WIDTH).height(UI.Buttons.MAIN_BUTTON_HEIGHT);
        table.add();
        table.row().expand();
        table.add();
        table.add(choosePlaneButton).width(UI.Buttons.MAIN_BUTTON_WIDTH).height(UI.Buttons.MAIN_BUTTON_HEIGHT);
        table.add();
        table.row().expand();
        table.add();
        table.add(statsButton).width(UI.Buttons.MAIN_BUTTON_WIDTH).height(UI.Buttons.MAIN_BUTTON_HEIGHT);
        table.row().expand();
        
        chooseLevelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((Fly) Gdx.app.getApplicationListener()).setLevelGroupScreen();
            }
        });
        
        choosePlaneButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((Fly) Gdx.app.getApplicationListener()).setPlaneChoosingScreen();
            	
            }
        });
        
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                PlayerProfile playerProfile = PlayerProfileManager.getInstance().getCurrentPlayerProfile();
                LevelProfile levelHead = playerProfile.getLastLevel();
                if (levelHead == null) {
                    levelHead = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getChosenLevelGroup().getFirstLevel();
                    playerProfile.setCurrentLevel(levelHead);                    
                }
                Loader.loadLevel(levelHead);
            }
        });
        
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((Fly) Gdx.app.getApplicationListener()).setSettingScreen();
            }
        });
        
        this.helpOverlay = new HelpOverlay(this);
        helpOverlay.addHelpFrame(new HelpFrameText(skin, "welcome"));
        helpOverlay.addHelpFrame(new HelpFrameTextWithArrow(skin, "helpPlay", continueButton));
        helpOverlay.addHelpFrame(new HelpFrameTextWithArrow(skin, "helpSelectLevel", chooseLevelButton));
        helpOverlay.addHelpFrame(new HelpFrameTextWithArrow(skin, "helpSelectShip", choosePlaneButton));
        helpOverlay.addHelpFrame(new HelpFrameTextWithArrow(skin, "helpHighscore", statsButton));
        helpOverlay.addHelpFrame(new HelpFrameTextWithArrow(skin, "helpSettings", settingsButton));
        Actor dummyActorForBackButton = new Actor();
        dummyActorForBackButton.setBounds(UI.Window.REFERENCE_WIDTH + 100, 500, 0, 100);
        helpOverlay.addHelpFrame(new HelpFrameTextWithArrow(skin, "helpEnd", dummyActorForBackButton));
        
        helpButton.addListener(helpOverlay);
        showHelpScreen = false;
        
		statsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				((Fly) Gdx.app.getApplicationListener()).setStatisticsScreen();
			}
		});
    }
    
    @Override
    public void render(float delta) {
        super.render(delta);
        if (showHelpScreen) {
            helpOverlay.render();
        }
    }
    
    @Override
    public void dispose() {
        stage.dispose();
    }
    
    @Override
    public void startHelp() {
        showHelpScreen = true;
        Gdx.input.setInputProcessor(helpOverlay);
    }
    
    @Override
    public void endHelp() {
        showHelpScreen = false;
        Gdx.input.setInputProcessor(inputProcessor);
    }
}
