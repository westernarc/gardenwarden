package com.westernarc.gardenwarden;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.westernarc.gardenwarden.Graphics.EffectDecal;
import com.westernarc.gardenwarden.Graphics.SpriteFade;
import com.westernarc.gardenwarden.Node.EnemyNode;
import com.westernarc.gardenwarden.Node.Node;
import com.westernarc.gardenwarden.Node.PlayerNode;
import com.westernarc.gardenwarden.Node.PlayerNode.ANIM;

public class GardenWarden implements ApplicationListener {
	public static boolean exporting = false;
	private float CONST_SCREEN_WIDTH;
	private float CONST_SCREEN_HEIGHT;
	
	private static final float CONST_BOUND_LEFT = -35;
	private static final float CONST_BOUND_RIGHT = 35;
	private static final float CONST_BOUND_UP = 67;
	private static final float CONST_BOUND_DOWN = -6f;
	
	private OrthographicCamera cam2d;
	private PerspectiveCamera cam3d;
	private SpriteBatch batch2d;
	private DecalBatch batch3d;
	SpriteFade sprTitle;

	Node nodCarrot;
	Node nodPumpkin;
	Node nodTomato;
	Node nodWatermelon;
	Node nodScallion;
	Node nodSquash;

	float camAngle;
	float camDistance;
	float camHeight;
	
	BitmapFont fntUi;
	private float varUiAlpha;
	private float varDeathTextAlpha;
	
	private float tmrDeadState;
	
	private static final float CONST_TMRDEADSTATE_MAX = 5;
	//Game flow:
	//Splash Screen: 
	private enum GAMESTATE {
		splash, title, play, dead, score
	}
	private GAMESTATE varGameState;
	
	PlayerNode nodPlayer;
	Node nodGarden;
	ArrayList<EnemyNode> lstEnemies;
	
	//Game Variables
	float varRequestedWeight;  //Amount needed per round
	float varTimePerRound; //Time alloted to gather the weight needed
	float tmrRound; //Round time, counts down to 0
	int cntRound; //Round number.  Every time you complete a request the round number goes up
	float varWeightProgress; //Amount of weight currently gathered
	boolean flgRoundEnd;
	
	float tmrResetOpaque;
	SpriteFade sprResetFilter;
	boolean flgReset;
	float CONST_RESETOPAQUETIME = 0.5f;
	
	int varPlantHeld; //This stores what the character is holding.
	int CONST_HELD_NOTHING = 0;
	int CONST_HELD_CARROT = 1;
	int CONST_HELD_PUMPKIN = 2;
	int CONST_HELD_TOMATO = 3;
	int CONST_HELD_WATERMELON = 4;
	int CONST_HELD_SCALLION = 5;
	int CONST_HELD_SQUASH = 6;
	
	@Override
	public void create() {
		CONST_SCREEN_WIDTH = Gdx.graphics.getWidth();
		CONST_SCREEN_HEIGHT = Gdx.graphics.getHeight();
		
		cam2d = new OrthographicCamera(CONST_SCREEN_WIDTH, CONST_SCREEN_HEIGHT);
		batch2d = new SpriteBatch();
		
		cam3d = new PerspectiveCamera(67, CONST_SCREEN_WIDTH, CONST_SCREEN_HEIGHT);
		batch3d = new DecalBatch();
		//batch3d.setGroupStrategy(new CameraGroupStrategy(cam3d));
		
		camDistance = 20;
		camHeight = 20;
		camAngle = (float)-Math.PI/2f;
		cam3d.position.set((float)Math.sin(camAngle) * camDistance, camHeight, (float)Math.cos(camAngle) * camDistance);
		cam3d.lookAt(0, 70, 0);
		cam3d.far = 2000;
		
		nodPlayer = new PlayerNode();
		nodGarden = new Node();
		nodGarden.setModel(loadModel("models/garden"));
		Material grassMat = new Material("mat", new TextureAttribute(new Texture(Gdx.files.internal("textures/gardentex.png")), 0, "s_tex"), new ColorAttribute(Color.WHITE, ColorAttribute.diffuse));
		nodGarden.setMaterial(grassMat);
		
		nodWatermelon = new Node();
		nodWatermelon.setModel(loadModel("models/watermelon"));
		nodWatermelon.setMaterial(grassMat);
		
		sprTitle = new SpriteFade(new Texture(Gdx.files.internal("textures/title.png")));
		sprTitle.setPosition(-sprTitle.getWidth()/2, -sprTitle.getHeight()/2);
		sprTitle.setScale(CONST_SCREEN_HEIGHT / sprTitle.getHeight());
		sprTitle.fade(1, false);
		
		fntUi = new BitmapFont(Gdx.files.internal("text/BimboJVE_24.fnt"), false);
		varGameState = GAMESTATE.title;
		
		lstEnemies = new ArrayList<EnemyNode>();
		
		sprResetFilter = new SpriteFade(new Texture(Gdx.files.internal("textures/reset.png")));
		//16 x 16 are the dimensions of the fade sprite
		sprResetFilter.scale(Math.max(CONST_SCREEN_WIDTH, CONST_SCREEN_HEIGHT) / 16f);
		sprResetFilter.setPosition(-sprResetFilter.getWidth()/2f, -sprResetFilter.getHeight()/2f);
		sprResetFilter.fade(1, false);
		reinitialize();
	}
	
	private void reinitialize() {
		varUiAlpha = 0;
		varDeathTextAlpha = 0;
		lstEnemies.clear();
		
		tmrDeadState = 0;
		
		nodPlayer.setAnim(PlayerNode.ANIM.stand);
		nodPlayer.setFrame(4);
		nodPlayer.setPosition(0,0,0);
		nodPlayer.setRotation(0);
		flgReset = false;
		tmrResetOpaque = 0;
		
		varRequestedWeight = 10;  //Amount needed per round
		varTimePerRound = 30; //Time alloted to gather the weight needed
		varWeightProgress = 0; //Amount of weight currently gathered
	}

	//Spawns waves
	private void updateWave() {
		//Spawn when the last wave has ended
		if(lstEnemies.size() < (cntRound + 3)) {
			EnemyNode nodEnemy = new EnemyNode();
			//Random from left, right or top
			double random = Math.random();
			if(random < 0.33) {
				nodEnemy.setPosition(70, 0, (float)Math.random() * 60 - 30);
			} else if(random < 0.66) {
				nodEnemy.setPosition((float)Math.random() * 60 - 30, 0, 60);
			} else {
				nodEnemy.setPosition((float)Math.random() * 60 - 30, 0, -60);
			}
			lstEnemies.add(nodEnemy);
		}
	}
	
	@Override
	public void dispose() {
		batch2d.dispose();
	}

	@Override
	public void render() {
		float tpf = Gdx.graphics.getDeltaTime();
		
		Gdx.gl10.glClearColor(0.51f, 0.76f, 0.88f, 1);
		
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		Gdx.gl10.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl10.glEnable(GL10.GL_TEXTURE_2D);
		Gdx.gl10.glEnable(GL10.GL_COLOR_MATERIAL);
		
		Gdx.gl10.glPushMatrix();
		Gdx.gl10.glTranslatef(nodPlayer.getX(), nodPlayer.getY(), nodPlayer.getZ());
		Gdx.gl10.glRotatef(nodPlayer.getRotation(), 0, 1, 0);
		nodPlayer.render();
		Gdx.gl10.glPopMatrix();
		
		//Draw enemies
		for(EnemyNode curNodEnemy : lstEnemies) {
			Gdx.gl10.glPushMatrix();
			Gdx.gl10.glTranslatef(curNodEnemy.getX(), curNodEnemy.getY(), curNodEnemy.getZ());
			Gdx.gl10.glRotatef(curNodEnemy.getRotation(), 0, 1, 0);
			if(curNodEnemy.isFlinching()) {
				Gdx.gl10.glRotatef(180, 0, 0, 1);
				Gdx.gl10.glTranslatef(0, -2, 0);
			}
			curNodEnemy.render();
			Gdx.gl10.glPopMatrix();
		}

		nodGarden.render();
		
		Gdx.gl10.glPushMatrix();
		Gdx.gl10.glTranslatef(nodWatermelon.getX(), nodWatermelon.getY(), nodWatermelon.getZ());
		Gdx.gl10.glScalef(0.5f, 0.5f, 0.5f);
		nodWatermelon.render();
		Gdx.gl10.glPopMatrix();

		batch3d.flush();
		
		Gdx.gl10.glDisable(GL10.GL_DEPTH_TEST);
		Gdx.gl10.glDisable(GL10.GL_TEXTURE_2D);
		Gdx.gl10.glDisable(GL10.GL_COLOR_MATERIAL);
		
		//Draw 2d		
		drawUi(tpf);
		
		handleInput(tpf);
		update(tpf);
	}
	private void update(float tpf) {
					
		cam3d.position.set(-camDistance + nodPlayer.getX(), camHeight, nodPlayer.getPosition().z);
		cam3d.lookAt(nodPlayer.getX(), 0, nodPlayer.getZ());
		cam3d.update(true);
		cam3d.apply(Gdx.gl10);
		
		switch(varGameState) {
		case dead:
			sprResetFilter.fade(tpf * 2, false);
			tmrDeadState += tpf;
			if(tmrDeadState <= CONST_TMRDEADSTATE_MAX - 1) {
				if(varDeathTextAlpha < 1) {
					varDeathTextAlpha += tpf;
					if(varDeathTextAlpha > 1) {
						varDeathTextAlpha = 1;
					}
				}
			} else if(tmrDeadState > CONST_TMRDEADSTATE_MAX - 1 && tmrDeadState < CONST_TMRDEADSTATE_MAX){
				if(varDeathTextAlpha > 0) {
					varDeathTextAlpha -= tpf;
					if(varDeathTextAlpha < 0) {
						varDeathTextAlpha = 0;
					}
				}
			} else if(tmrDeadState >= CONST_TMRDEADSTATE_MAX) {
				varGameState = GAMESTATE.score;
				varDeathTextAlpha = 0;
			}
			nodPlayer.update(tpf);
			break;
		case play:
			sprResetFilter.fade(tpf * 2, false);
			nodPlayer.update(tpf);
			
			//Check player bounds
			if(nodPlayer.getZ() < CONST_BOUND_LEFT) nodPlayer.setZ(CONST_BOUND_LEFT);
			if(nodPlayer.getZ() > CONST_BOUND_RIGHT) nodPlayer.setZ(CONST_BOUND_RIGHT);
			if(nodPlayer.getX() < CONST_BOUND_DOWN) nodPlayer.setX(CONST_BOUND_DOWN);
			if(nodPlayer.getX() > CONST_BOUND_UP) nodPlayer.setX(CONST_BOUND_UP);
			
			//Update field			
			updatePlants(tpf);
			for(int i = 0; i < 3; i++) {
				//dclWatermelon[i].setScale(dclWatermelon[i].getScaleX() * 0.99f);	
			}
			updateWave();
			break;
		case score:
			if(flgReset){
				sprResetFilter.fade(tpf, true);
				if(sprResetFilter.getAlpha() >= 0.97f) {
					tmrResetOpaque += tpf;
					if(tmrResetOpaque > CONST_RESETOPAQUETIME) {
						//Restart the game
						reinitialize();
						
						varGameState = GAMESTATE.play;
					}
				}
			} else {
				sprResetFilter.fade(tpf * 2, false);
			}
			nodPlayer.update(tpf);
			break;
		case splash:
			break;
		case title:
			sprTitle.fade(tpf, true);
			break;
		}
	}
	
	private void updatePlants(float tpf) {
		//Go through each enemy.  For each enemy, see if they are close to each plant.
		//If the enemy is near a plant, scale down the plant.
		
		//Also check whether or not enemy is attacking players when looping through the enemies.
		Iterator<EnemyNode> enemyItr = lstEnemies.iterator();
		while(enemyItr.hasNext()){
			EnemyNode enode = enemyItr.next();
			if(Math.abs(enode.getX()) > 90 || Math.abs(enode.getZ()) > 90 || enode.getY() < -10) {
				enemyItr.remove();
				continue;
			}
			
			//Dont update enemies that are out of bounds
			enode.update(tpf);
			int rad = 4;
			float enemyX = enode.getX();
			float enemyZ = enode.getZ();
			//Check carrots, pumpkins, tomatoes, watermelon, scallion, and squash

		}
		
		//Update veggies
		nodWatermelon.setPosition(nodPlayer.getX() + nodPlayer.getDirection().x * 8, 5, nodPlayer.getZ() + nodPlayer.getDirection().z * 8);
	}
	private void handleInput(float tpf) {
		switch(varGameState) {
		case play:
			
			//Handle Touching
			if(Gdx.input.isTouched()) {
				int x = Gdx.input.getX();
				int y = Gdx.input.getY();

				float angle = 0;
				float difX = x - CONST_SCREEN_WIDTH / 2;
				float difY = y - CONST_SCREEN_HEIGHT / 2;
				if(difX >= 0) { 
					angle = (float)(Math.toDegrees(Math.atan(difY/difX))) - 90;
				} else {
					angle = (float)(Math.toDegrees(Math.atan(difY/difX))) + 90;
				}
				nodPlayer.setRotation(-angle);
				nodPlayer.move(nodPlayer.getDirection());
				nodPlayer.setAnim(PlayerNode.ANIM.walk);
			} else {
				nodPlayer.setAnim(PlayerNode.ANIM.stand);
			}
			
			if(Gdx.input.isKeyPressed(Keys.ANY_KEY)) {
				if(!Gdx.input.isKeyPressed(Keys.E) && !Gdx.input.isKeyPressed(Keys.S)) {
					nodPlayer.setAnim(PlayerNode.ANIM.walk);
				}
			} else {
				nodPlayer.setAnim(PlayerNode.ANIM.stand);
			}
			if(Gdx.input.isKeyPressed(Keys.A)){
					nodPlayer.rotate(5);
			}
			if(Gdx.input.isKeyPressed(Keys.D)){
					nodPlayer.rotate(-5);
			}
			if(Gdx.input.isKeyPressed(Keys.W)){
					nodPlayer.move(nodPlayer.getDirection());
			}
			if(Gdx.input.isKeyPressed(Keys.R)){
				nodPlayer.setAnim(PlayerNode.ANIM.walk);
			}
			if(Gdx.input.isKeyPressed(Keys.Y)) {
				varGameState = GAMESTATE.dead;
			}
			break;
		case splash:
			break;
		case title:
			if(Gdx.input.isTouched()) {
				varGameState = GAMESTATE.play;
			}
			break;
		case dead:
			break;
		case score:
			if(Gdx.input.isTouched() && !flgReset) {
				System.out.println("Resetting");
				//Start the reset
				flgReset = true;
			}
			break;
		}
	}
	
	private void drawUi(float tpf) {
		batch2d.setProjectionMatrix(cam2d.combined);
		batch2d.begin();
		
		sprTitle.fade(1/60f * 2, (varGameState == GAMESTATE.title));
		sprTitle.draw(batch2d);
		
		if(varGameState == GAMESTATE.play){
			if(varUiAlpha < 1) {
				varUiAlpha += 1/60f;
			}
			if(varUiAlpha > 1) {
				varUiAlpha = 1;
			}
		} else {
			varUiAlpha -= 1/60f;
			if(varUiAlpha < 0) {
				varUiAlpha = 0;
			}
		}
		fntUi.setColor(1,1,1,varUiAlpha);
		fntUi.setScale(1);
		fntUi.draw(batch2d, "Request no." + (cntRound + 1), -CONST_SCREEN_WIDTH/2 + fntUi.getSpaceWidth(), CONST_SCREEN_HEIGHT/2 - fntUi.getLineHeight()/2);
		fntUi.setScale(2);
		String time = Math.round(varTimePerRound - tmrRound) + "s";
		fntUi.draw(batch2d, time, -fntUi.getBounds(time).width/2f, CONST_SCREEN_HEIGHT/2 - fntUi.getLineHeight()/4);
		fntUi.setScale(1);
		String weightStats = Math.round(varWeightProgress * 10) / 10f + "/" + Math.round(varRequestedWeight * 10) / 10f + " lbs";
		fntUi.draw(batch2d, weightStats, CONST_SCREEN_WIDTH/2 - fntUi.getBounds(weightStats).width - fntUi.getSpaceWidth(), CONST_SCREEN_HEIGHT/2 - fntUi.getLineHeight()/2);
		//Debug
		//fntUi.draw(batch2d, "wvdn:" + flgWaveEnd + " sz:" + lstEnemies.size() + " lf:" + (varGardenHealth/varMaxGardenHealth), -CONST_SCREEN_WIDTH/2f,0);
		
		//Draw death text
		
		if(varGameState == GAMESTATE.dead) {
			fntUi.setColor(1,1,1,varDeathTextAlpha);
			fntUi.setScale(3);
			fntUi.draw(batch2d, "Request Failed!", -fntUi.getBounds("Garden Lost!").width/2f, 0);
			fntUi.setScale(1);
			fntUi.setColor(1,1,1,1);
		} else if(varGameState == GAMESTATE.score){
			fntUi.setColor(1,1,1,1);
			fntUi.draw(batch2d, "Requests Fulfilled: " + cntRound, 0, 0);
		}

		sprResetFilter.draw(batch2d);
		
		batch2d.end();
	}
	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	
	public static StillModel loadModel(String url) {
		if(GardenWarden.exporting) {
			return ModelLoaderRegistry.loadStillModel(Gdx.files.internal(url + ".g3d"));
		} else {
			return ModelLoaderRegistry.loadStillModel(Gdx.files.internal(url + ".g3dt"));
		}
	}
}
