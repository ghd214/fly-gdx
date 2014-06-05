package de.fau.cs.mad.fly.game;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import de.fau.cs.mad.fly.Assets;
import de.fau.cs.mad.fly.features.IFeatureFinish;
import de.fau.cs.mad.fly.features.IFeatureGatePassed;
import de.fau.cs.mad.fly.features.IFeatureInit;
import de.fau.cs.mad.fly.features.IFeatureRender;
import de.fau.cs.mad.fly.res.Gate;

/**
 * This class implements the function to show in the game small arrows that
 * indicate the direction of the next gates.
 * 
 * @author Lukas Hahmann
 * 
 */
public class GateIndicator implements IFeatureInit, IFeatureFinish,
		IFeatureRender, IFeatureGatePassed {

	
	private GameController gameController;
	private ModelBatch batch;
	private Environment environment;

	private ModelBuilder modelBuilder;
	private GameObject arrowModel;

	@Override
	public void init(GameController game) {
		this.gameController = game;
		Assets.loadArrow();
		arrowModel = new GameObject(Assets.manager.get(Assets.arrow));
		batch = game.batch;
		
		environment = gameController.getLevel().getEnvironment();
	}

	@Override
	public void render(float delta) {
		for (Gate gate : gameController.getLevelProgress().getNextGates()) {
			//System.out.println("nextGates" + gameController.getLevelProgress().getNextGates().size());
			Vector3 targetPosition = gate.model.getPosition();
			Vector3 vectorToTarget = new Vector3();
			Vector3 cross = new Vector3();
			Vector3 cameraDirection = gameController.getCamera().direction
					.cpy();
			Vector3 up = gameController.getCamera().up.cpy();
			Vector3 down = up.cpy().scl(-1);

			// The arrow should be in the middle of the screen, a little before
			// the
			// camera, that it is always visible and below the vertical
			// midpoint.
			Vector3 gatePositionRelativeToCamera = cameraDirection.scl(3)
					.add(gameController.getCamera().position)
					.add(down.scl(1.4f));

			vectorToTarget.set(targetPosition.cpy()
					.sub(gatePositionRelativeToCamera).scl(-1).nor());

			// calculate orthogonal up vector
			up.crs(vectorToTarget).crs(vectorToTarget).nor();

			cross.set(vectorToTarget.cpy().crs(up).nor());

			// create local coordinate system for the arrow. All axes have to be
			// normalized, otherwise, the arrow is scaled.
			float[] values = { up.x, up.y, up.z, 0f, cross.x, cross.y, cross.z,
					0f, vectorToTarget.x, vectorToTarget.y, vectorToTarget.z,
					0f, 0f, 0f, 0f, 1f };

			arrowModel.transform.set(values).trn(gatePositionRelativeToCamera);
			// batch.begin(gameController.getCamera());
			batch.render(arrowModel, environment);
			// batch.end();
		}
	}

	@Override
	public void gatePassed(Gate passedGate) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
