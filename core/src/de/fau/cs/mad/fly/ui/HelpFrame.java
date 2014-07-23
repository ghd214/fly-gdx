package de.fau.cs.mad.fly.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Abstract class for HelpFrames.
 * <p>
 * A help frame is a virtual frame that is shown when accessing the help. Each
 * HelpFrame explains an own functionality.
 * <p>
 * To use a HelpFrame, extend this class. Make sure to call
 * {@link #setupBatchAndStage()} before you place elements on the screen. This
 * makes sure that your stuff is displayed independent from the resolution.
 * After you have placed your stuff set the {@link #viewport} to the width and
 * the height of the screen.
 * 
 * @author Lukas Hahmann
 * 
 */
public abstract class HelpFrame {
    
    protected Batch batch;
    protected Stage stage;
    protected float scalingFactor;
    protected Viewport viewport;
    
    /**
     * Set up batch and stage that are independent from the resolution. Call
     * this method before you create your content.
     */
    public void setupBatchAndStage() {
        this.batch = new SpriteBatch();
        
        stage = new Stage();
        float widthScalingFactor = UI.Window.REFERENCE_WIDTH / (float) Gdx.graphics.getWidth();
        float heightScalingFactor = UI.Window.REFERENCE_HEIGHT / (float) Gdx.graphics.getHeight();
        scalingFactor = Math.max(widthScalingFactor, heightScalingFactor);
        viewport = new FillViewport(Gdx.graphics.getWidth() * scalingFactor, Gdx.graphics.getHeight() * scalingFactor, stage.getCamera());
        stage.setViewport(viewport);
    }
    
    /**
     * Is called when this frame is shown. Should be rendered on top of the
     * current visible content.
     */
    public abstract void render();
}