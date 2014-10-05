package de.fau.cs.mad.fly.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;

import de.fau.cs.mad.fly.Fly;
import de.fau.cs.mad.fly.features.overlay.TouchScreenOverlay;
import de.fau.cs.mad.fly.player.Player;
import de.fau.cs.mad.fly.profile.PlayerProfile;
import de.fau.cs.mad.fly.profile.PlayerProfileManager;
import de.fau.cs.mad.fly.settings.SettingManager;


/**
 * Controls the flight of the player regarding to user-input
 * 
 * @author Sebastian
 * 
 */
public class FlightController implements InputProcessor {
    
    protected boolean useSensorData;
    protected boolean inTouch = false;
    protected boolean invertPitch;
    
    Set<Integer> pressedKeys = new HashSet<Integer>();

    protected Player player;
    
    protected float startRoll, startPitch;
    
    protected float rollFactor = 0.0f;
    protected float azimuthFactor = 0.0f;
    
    protected float rollFactorChange = 1.0f;
    protected float azimuthFactorChange = 1.0f;
    
    protected int currentEvent = -1;
    
    protected float screenHeight = Gdx.graphics.getHeight();
    protected float screenWidth = Gdx.graphics.getWidth();
    
    // variables for Sensor input smoothing
    protected int bufferSize;
    protected List<Float> rollInput;
    protected List<Float> pitchInput;
    
    /** Degree of lifting and leaning the ship */
    protected float roll;
    /** degree of steering left and right */
    protected float pitch;
    
    /** This member defines the maximum rotation angle of the device that causes a change in steering */
    protected float maxRotate = 45.f;
    
    protected float centerX = TouchScreenOverlay.X_POS_OF_STEERING_CIRCLE + screenWidth / 2;
    protected float centerY = -TouchScreenOverlay.Y_POS_OF_STEERING_CIRCLE + screenHeight / 2;
    protected float radius = TouchScreenOverlay.RADIUS_OF_STEERING_CIRCLE;
    
    protected boolean screenRotated;
    
    public FlightController(Player player, PlayerProfile playerProfile) {
        this.player = player;
        Preferences preferences = playerProfile.getSettingManager().getPreferences();
        this.useSensorData = !preferences.getBoolean(SettingManager.USE_TOUCH);
        this.invertPitch = preferences.getBoolean(SettingManager.INVERT_PITCH);
        this.bufferSize = 10;
        screenRotated = ((Fly) Gdx.app.getApplicationListener()).orientationSwapped();
        Gdx.app.log("orientation", "" + screenRotated);
    }
    
    /**
     * Resets steering and the buffers.
     */
    public void init() {
        useSensorData = !PlayerProfileManager.getInstance().getCurrentPlayerProfile().getSettingManager().getPreferences().getBoolean(SettingManager.USE_TOUCH);
        if (useSensorData) {
            resetSteering();
            resetBuffers();
        }
    }
    
    /**
     * Getter for the factor by which the player rotate up/down
     * 
     * @return The rollFactor
     */
    public float getRollFactor() {
        return rollFactor / player.getPlane().getRollingSpeed();
    }
    
    /**
     * Getter for the factor by which the player should fly curves
     * 
     * @return
     */
    public float getAzimuthFactor() {
        return azimuthFactor / player.getPlane().getAzimuthSpeed();
    }
    
    /**
     * Resets the Steering with Sensors to the current Smartphone position
     */
    public void resetSteering() {
        startRoll = Gdx.input.getRoll();
        if (invertPitch && !screenRotated || !invertPitch && screenRotated) {
            startRoll = -startRoll;
        }
        startPitch = Gdx.input.getPitch();
    }
    
    /**
     * Computes the new position to fly to regarding to the user input
     * 
     * @param delta
     *            Time since last frame
     */
    public void update(float delta) {
        // rotating the camera according to UserInput
        if (useSensorData)
            interpretSensorInput();
        player.getPlane().rotate(rollFactor, azimuthFactor, 60 * delta);
    }

    protected void resetBuffers() {
        rollInput = new ArrayList<Float>();
        pitchInput = new ArrayList<Float>();
    }
    
    /**
     * Interprets the rotation of the smartphone
     */
    protected void interpretSensorInput() {
        pitch = Gdx.input.getPitch();
        roll = Gdx.input.getRoll();
        
        // interpret if screen on smartphone is rotated or not
        if (invertPitch && !screenRotated || !invertPitch && screenRotated) {
            roll = roll * -1;
        }
        if(screenRotated) {
            pitch = -pitch;
        }
        
        // removing oldest element in buffers
        if (rollInput.size() >= bufferSize) {
            rollInput.remove(0);
            pitchInput.remove(0);
        }
        
        // adding newest sensor-data to buffers
        rollInput.add(roll);
        pitchInput.add(pitch);
        
        roll = average(rollInput);
        pitch = average(pitchInput);
        
        float difRoll = roll - startRoll;
        if (Math.abs(difRoll) > 180) {
            difRoll -= Math.signum(difRoll) * 360;
        }
        
        float difPitch = pitch - startPitch;
        if (Math.abs(difPitch) > 180) {
            difPitch -= Math.signum(difPitch) * 360;
        }
        
        // capping the rotation to a maximum
        if (Math.abs(difRoll) > maxRotate) {
            difRoll = maxRotate * Math.signum(difRoll);
        }
        
        if (Math.abs(difPitch) > maxRotate) {
            difPitch = maxRotate * Math.signum(difPitch);
        }
        
        rollFactor = 0.0f;
        azimuthFactor = 0.0f;
        
        // camera rotation according to smartphone rotation
        setAzimuthFactor(difPitch / maxRotate);
        setRollFactor(difRoll / -maxRotate);
    }
    
    /**
     * Setter for the roll factor change.
     * 
     * @param rollFactorChange
     */
    public void setRollFactorChange(float rollFactorChange) {
        this.rollFactorChange = rollFactorChange;
    }
    
    /**
     * Setter for the azimuth factor change.
     * 
     * @param azimuthFactorChange
     */
    public void setAzimuthFactorChange(float azimuthFactorChange) {
        this.azimuthFactorChange = azimuthFactorChange;
    }
    
    /**
     * Resets the roll and azimuth factor changes to 1.0f.
     */
    public void resetFactorChange() {
        rollFactorChange = 1.0f;
        azimuthFactorChange = 1.0f;
    }
    
    /**
     * Setter for the {@link #azimuthDir}. Values greater than the azimuthSpeed
     * of the plane are reduced to the azimuth speed of the plane.
     * 
     * @param azimuthFactor
     */
    protected void setAzimuthFactor(float azimuthFactor) {
        this.azimuthFactor = azimuthFactor * player.getPlane().getAzimuthSpeed();
    }
    
    /**
     * Setter for the {@link #rollDir}. Values greater than the rollingSpeed of
     * the plane are reduced to the azimuth speed of the plane.
     * 
     * @param rollFactor
     */
    protected void setRollFactor(float rollFactor) {
        this.rollFactor = rollFactor * player.getPlane().getRollingSpeed();

    }
    
    protected float average(List<Float> input) {
        float result = 0.0f;
        int size = input.size();
        for (int i = 0; i < size; i++) {
            result += input.get(i);
        }
        
        return result / (float) input.size();
    }

    private void evaluateKeyboardInput() {
        setAzimuthFactor(0);
        setRollFactor(0);
        for ( Integer keycode : pressedKeys )
            switch ( keycode ) {
                case Keys.A:
                case Keys.LEFT:
                    setAzimuthFactor(getAzimuthFactor() + 1);
                    break;
                case Keys.D:
                case Keys.RIGHT:
                    setAzimuthFactor(getAzimuthFactor() - 1);
                    break;
                case Keys.W:
                case Keys.UP:
                    setRollFactor(getRollFactor() + 1);
                    break;
                case Keys.S:
                case Keys.DOWN:
                    setRollFactor(getRollFactor() - 1);
                    break;
            }
    }
    
    @Override
    public boolean keyDown(int keycode) {
        pressedKeys.add(keycode);
        evaluateKeyboardInput();
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        pressedKeys.remove(keycode);
        evaluateKeyboardInput();
        return false;
    }
    
    @Override
    public boolean keyTyped(char character) {
        return false;
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // using the touchscreen to rotate camera
        
        if (button == Buttons.LEFT && !useSensorData) {
            
            float xDif = screenX - centerX;
            float yDif = screenY - centerY;
            float length = (float) Math.sqrt(xDif * xDif + yDif * yDif);
            
            if (length <= radius) {
                setAzimuthFactor(-xDif / radius);
                setRollFactor(-yDif / radius);
                inTouch = true;
            } else {
                inTouch = false;
            }
            
            currentEvent = pointer;
        } else {
            setAzimuthFactor(0);
            setRollFactor(0);
        }
        
        return false;
    }
    
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // set camera rotation to 0 when finger is lifted from touchscreen
        if (button == Buttons.LEFT) {
            rollFactor = 0;
            azimuthFactor = 0;
        }
        return false;
    }
    
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // changing camera rotation when finger is dragged on the touchscreen
        if (pointer == currentEvent) {
            
            float xDif = centerX - screenX;
            float yDif = centerY - screenY;
            float length = (float) Math.sqrt(xDif * xDif + yDif * yDif);
            
            if (length <= radius) {
                setAzimuthFactor(xDif / radius);
                setRollFactor(yDif / radius);
            } else if (inTouch) {
                setAzimuthFactor(xDif / length);
                setRollFactor(yDif / length);
            }
            
        }
        return false;
    }
    
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // nothing should happen here
        return false;
    }
    
    @Override
    public boolean scrolled(int amount) {
        // nothing should happen here
        return false;
    }
}
