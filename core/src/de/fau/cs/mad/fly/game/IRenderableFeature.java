package de.fau.cs.mad.fly.game;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

/**
 * Each feature that has some stuff to render, should implement this Interface.
 * 
 * @author Lukas Hahmann
 * 
 */
public interface IRenderableFeature {
	public void render(ModelBatch batch, Environment environment, 
			 float delta);
}