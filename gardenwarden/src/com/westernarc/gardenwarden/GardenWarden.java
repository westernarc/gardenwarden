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
	private float CONST_SCREEN_WIDTH;
	private float CONST_SCREEN_HEIGHT;
	
	private static final float CONST_BOUND_LEFT = -40;
	private static final float CONST_BOUND_RIGHT = 40;
	private static final float CONST_BOUND_UP = 50;
	private static final float CONST_BOUND_DOWN = -30;
	
	private OrthographicCamera cam2d;
	private PerspectiveCamera cam3d;
	private SpriteBatch batch2d;
	private DecalBatch batch3d;
	SpriteFade sprTitle;
	
	private static final int CONST_GRASSDECAL_MAX = 70;
	private static final int CONST_FLOWERDECAL_MAX = 20;
	//Foliage decals
	Decal[] dclGrass;
	Decal[] dclFlower;
	
	private static final int CONST_VEGGIEDECAL_MAX = 10;
	private static final int CONST_GROUNDMOUND_MAX = 6;
	//Veggie decals
	private static final int DECALWATERMELON = 1;
	private static final int DECALSQUASH = 2;
	private static final int DECALSCALLION = 3;
	private static final int DECALTOMATO = 4;
	private static final int DECALPUMPKIN = 5;
	private static final int DECALCARROT = 6;
	Decal[] dclGroundMound;
	Decal[] dclWatermelon;
	Decal[] dclSquash;
	Decal[] dclScallion;
	Decal[] dclTomato;
	Decal[] dclPumpkin;
	Decal[] dclCarrot;
	//Store whether or not vegetables have died here.
	//Length is the total amount of veggies.
	boolean[] varVeggiesDead;

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
	//Wave number
	int varWave;
	int spawnedInWave;
	int killedInWave;
	int killedTotal;
	boolean flgWaveEnd;
	//int 
	float varGardenHealth;
	float varMaxGardenHealth;
	float CONST_BASE_PLANT_SCALE = 0.04f;
	float varPlantDecayRate = 0.0001f;
	
	float tmrResetOpaque;
	SpriteFade sprResetFilter;
	boolean flgReset;
	float CONST_RESETOPAQUETIME = 0.5f;
	
	int TOUCH_MOVE_X;
	int TOUCH_MOVE_Y;
	
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
		nodGarden.setModel(ModelLoaderRegistry.loadStillModel(Gdx.files.internal("models/garden.g3dt")));
		nodGarden.setMaterial(new Material("mat", new TextureAttribute(new Texture(Gdx.files.internal("textures/gardentex.png")), 0, "s_tex"), new ColorAttribute(Color.WHITE, ColorAttribute.diffuse)));
		
		TextureRegion grassTex = new TextureRegion(new Texture(Gdx.files.internal("textures/grass.png")));
		TextureRegion flowerTex = new TextureRegion(new Texture(Gdx.files.internal("textures/flower.png")));
		dclGrass = new Decal[CONST_GRASSDECAL_MAX];
		dclFlower = new Decal[CONST_FLOWERDECAL_MAX];
		for(int i = 0; i < CONST_GRASSDECAL_MAX; i++) {
			dclGrass[i] = Decal.newDecal(grassTex, true);
			dclGrass[i].rotateY(90);
			dclGrass[i].rotateX(50);
			float randomX = (float)Math.random() * 120 - 30;
			float randomY = (float)Math.random() * 120 - 60;
			if(Math.abs(randomX) > 50 || Math.abs(randomY) > 30) {
				dclGrass[i].setScale(0.04f);
				dclGrass[i].setPosition(randomX, 1.4f, randomY);
			} else {
				dclGrass[i].setScale(0.02f);
				dclGrass[i].setPosition(randomX, 1f, randomY);
			}
		}
		for(int i = 0; i < CONST_FLOWERDECAL_MAX; i++) {
			dclFlower[i] = Decal.newDecal(flowerTex, true);
			dclFlower[i].rotateY(90);
			dclFlower[i].rotateX(50);
			dclFlower[i].setScale(0.05f);
			dclFlower[i].setPosition((float)Math.random() * 120 - 30, 1, (float)Math.random() * 120 - 60);
		}
		
		TextureRegion watermelonTex = new TextureRegion(new Texture(Gdx.files.internal("textures/watermelon.png")));
		dclWatermelon = new Decal[CONST_VEGGIEDECAL_MAX];
		for(int i = 0; i < CONST_VEGGIEDECAL_MAX; i++) {
			dclWatermelon[i] = Decal.newDecal(watermelonTex, true);
			dclWatermelon[i].rotateY(90);
			dclWatermelon[i].rotateX(50);
			dclWatermelon[i].setScale(CONST_BASE_PLANT_SCALE);
			dclWatermelon[i].setPosition(( ((float)i / CONST_VEGGIEDECAL_MAX) * 25) + 4, 1, 2.4f);
		}
		
		TextureRegion tomatoTex = new TextureRegion(new Texture(Gdx.files.internal("textures/tomato.png")));
		dclTomato = new Decal[CONST_VEGGIEDECAL_MAX];
		for(int i = 0; i < CONST_VEGGIEDECAL_MAX; i++) {
			dclTomato[i] = Decal.newDecal(tomatoTex, true);
			dclTomato[i].rotateY(90);
			dclTomato[i].rotateX(50);
			dclTomato[i].setScale(CONST_BASE_PLANT_SCALE);
			dclTomato[i].setPosition(( ((float)i / CONST_VEGGIEDECAL_MAX) * 25) + 4, 1, -7.4f);
		}
		
		TextureRegion squashTex = new TextureRegion(new Texture(Gdx.files.internal("textures/squash.png")));
		dclSquash = new Decal[CONST_VEGGIEDECAL_MAX];
		for(int i = 0; i < CONST_VEGGIEDECAL_MAX; i++) {
			dclSquash[i] = Decal.newDecal(squashTex, true);
			dclSquash[i].rotateY(90);
			dclSquash[i].rotateX(50);
			dclSquash[i].setScale(CONST_BASE_PLANT_SCALE);
			dclSquash[i].setPosition(( ((float)i / CONST_VEGGIEDECAL_MAX) * 25) + 4, 1.2f, 20f);
		}
		
		TextureRegion carrotTex = new TextureRegion(new Texture(Gdx.files.internal("textures/carrot.png")));
		dclCarrot = new Decal[CONST_VEGGIEDECAL_MAX];
		for(int i = 0; i < CONST_VEGGIEDECAL_MAX; i++) {
			dclCarrot[i] = Decal.newDecal(carrotTex, true);
			dclCarrot[i].rotateY(90);
			dclCarrot[i].rotateX(50);
			dclCarrot[i].setScale(CONST_BASE_PLANT_SCALE);
			dclCarrot[i].setPosition(( ((float)i / CONST_VEGGIEDECAL_MAX) * 25) + 4, 1.3f, -25.5f);
		}
		
		TextureRegion scallionTex = new TextureRegion(new Texture(Gdx.files.internal("textures/scallion.png")));
		dclScallion = new Decal[CONST_VEGGIEDECAL_MAX];
		for(int i = 0; i < CONST_VEGGIEDECAL_MAX; i++) {
			dclScallion[i] = Decal.newDecal(scallionTex, true);
			dclScallion[i].rotateY(90);
			dclScallion[i].rotateX(50);
			dclScallion[i].setScale(CONST_BASE_PLANT_SCALE);
			dclScallion[i].setPosition(( ((float)i / CONST_VEGGIEDECAL_MAX) * 25) + 4, 1, 11.5f);
		}
		
		TextureRegion pumpkinTex = new TextureRegion(new Texture(Gdx.files.internal("textures/pumpkin.png")));
		dclPumpkin = new Decal[CONST_VEGGIEDECAL_MAX];
		for(int i = 0; i < CONST_VEGGIEDECAL_MAX; i++) {
			dclPumpkin[i] = Decal.newDecal(pumpkinTex, true);
			dclPumpkin[i].rotateY(90);
			dclPumpkin[i].rotateX(50);
			dclPumpkin[i].setScale(CONST_BASE_PLANT_SCALE);
			dclPumpkin[i].setPosition(( ((float)i / CONST_VEGGIEDECAL_MAX) * 25) + 4, 1, -17.5f);
		}
		
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
		
		varVeggiesDead = new boolean[CONST_VEGGIEDECAL_MAX * CONST_GROUNDMOUND_MAX];
		//Reinitialize veggie-dead boolean array
		for(int i = 0; i < varVeggiesDead.length; i++) {
			varVeggiesDead[i] = false;
		}
		spawnedInWave = 0;
		killedInWave = 0;
		flgWaveEnd = true;
		
		varGardenHealth = CONST_VEGGIEDECAL_MAX * CONST_GROUNDMOUND_MAX * CONST_BASE_PLANT_SCALE;
		varMaxGardenHealth = varGardenHealth;
		
		nodPlayer.setAnim(PlayerNode.ANIM.stand);
		nodPlayer.setFrame(4);
		nodPlayer.setPosition(0,0,0);
		nodPlayer.setRotation(0);
		flgReset = false;
		tmrResetOpaque = 0;
	}

	//Spawns waves
	private void updateWave() {
		//Spawn when the last wave has ended
		if(spawnedInWave < (varWave + 3) && flgWaveEnd) {
			spawnedInWave++;
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
		if(spawnedInWave >= varWave + 3) {
			flgWaveEnd = false;
		}
		if(killedInWave >= spawnedInWave) {
			flgWaveEnd = true;
			killedInWave = 0;
			spawnedInWave = 0;
			varWave++;
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
		for(int i = CONST_VEGGIEDECAL_MAX - 1; i >= 0; i--) {
			batch3d.add(dclWatermelon[i]);
		}
		for(int i = CONST_VEGGIEDECAL_MAX - 1; i >= 0; i--) {
			batch3d.add(dclTomato[i]);
		}
		for(int i = CONST_VEGGIEDECAL_MAX - 1; i >= 0; i--) {
			batch3d.add(dclSquash[i]);
		}
		for(int i = CONST_VEGGIEDECAL_MAX - 1; i >= 0; i--) {
			batch3d.add(dclCarrot[i]);
		}
		for(int i = CONST_VEGGIEDECAL_MAX - 1; i >= 0; i--) {
			batch3d.add(dclScallion[i]);
		}
		for(int i = CONST_VEGGIEDECAL_MAX - 1; i >= 0; i--) {
			batch3d.add(dclPumpkin[i]);
		}
		for(int i = 0; i < CONST_GRASSDECAL_MAX; i++) {
			batch3d.add(dclGrass[i]);
		}
		for(int i = 0; i < CONST_FLOWERDECAL_MAX; i++) {
			batch3d.add(dclFlower[i]);
		}
		if(nodPlayer.getEffect().isPlaying()) {
			nodPlayer.getEffect().update(tpf);
			batch3d.add(nodPlayer.getEffect().getDecal());
		}
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
				System.out.println(sprResetFilter.getAlpha());
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
			if(!enode.isFleeing() && !enode.isFlinching()) {
				for(int i = CONST_VEGGIEDECAL_MAX - 1; i >= 0; i--) {
					if(Math.abs(enemyX - dclWatermelon[i].getX()) < rad && Math.abs(enemyZ - dclWatermelon[i].getZ()) < rad) {
						if(dclWatermelon[i].getScaleX() >= 0) {
							dclWatermelon[i].setScale(dclWatermelon[i].getScaleX() - varPlantDecayRate);
							varGardenHealth -= varPlantDecayRate;
						} else {
							varGardenHealth -= dclWatermelon[i].getScaleX();
							dclWatermelon[i].setScale(0);
							
						}
					} else {
					}
				}
				for(int i = CONST_VEGGIEDECAL_MAX - 1; i >= 0; i--) {
					if(Math.abs(enemyX - dclTomato[i].getX()) < rad && Math.abs(enemyZ - dclTomato[i].getZ()) < rad) {
						if(dclTomato[i].getScaleX() >= 0) {
							dclTomato[i].setScale(dclTomato[i].getScaleX() - varPlantDecayRate);
							varGardenHealth -= varPlantDecayRate;
						} else {
							varGardenHealth -= dclTomato[i].getScaleX();
							dclTomato[i].setScale(0);
						}
					}
				}
				for(int i = CONST_VEGGIEDECAL_MAX - 1; i >= 0; i--) {
					if(Math.abs(enemyX - dclSquash[i].getX()) < rad && Math.abs(enemyZ - dclSquash[i].getZ()) < rad) {
						if(dclSquash[i].getScaleX() >= 0) {
							dclSquash[i].setScale(dclSquash[i].getScaleX() - varPlantDecayRate);
							varGardenHealth -= varPlantDecayRate;
						} else {
							varGardenHealth -= dclSquash[i].getScaleX();
							dclSquash[i].setScale(0);
						}
					}
				}
				for(int i = CONST_VEGGIEDECAL_MAX - 1; i >= 0; i--) {
					if(Math.abs(enemyX - dclCarrot[i].getX()) < rad && Math.abs(enemyZ - dclCarrot[i].getZ()) < rad) {
						if(dclCarrot[i].getScaleX() >= 0) {
							dclCarrot[i].setScale(dclCarrot[i].getScaleX() - varPlantDecayRate);
							varGardenHealth -= varPlantDecayRate;
						} else {
							varGardenHealth -= dclCarrot[i].getScaleX();
							dclCarrot[i].setScale(0);
						}
					}
				}
				for(int i = CONST_VEGGIEDECAL_MAX - 1; i >= 0; i--) {
					if(Math.abs(enemyX - dclScallion[i].getX()) < rad && Math.abs(enemyZ - dclScallion[i].getZ()) < rad) {
						if(dclScallion[i].getScaleX() >= 0) {
							dclScallion[i].setScale(dclScallion[i].getScaleX() - varPlantDecayRate);
							varGardenHealth -= varPlantDecayRate;
						} else {
							varGardenHealth -= dclScallion[i].getScaleX();
							dclScallion[i].setScale(0);
						}
					}
				}
				for(int i = CONST_VEGGIEDECAL_MAX - 1; i >= 0; i--) {
					if(Math.abs(enemyX - dclPumpkin[i].getX()) < rad && Math.abs(enemyZ - dclPumpkin[i].getZ()) < rad) {
						if(dclPumpkin[i].getScaleX() >= 0) {
							dclPumpkin[i].setScale(dclPumpkin[i].getScaleX() - varPlantDecayRate);
							varGardenHealth -= varPlantDecayRate;
						} else {
							varGardenHealth -= dclPumpkin[i].getScaleX();
							dclPumpkin[i].setScale(0);
						}
					}
				}
				if(varGardenHealth <= 0) {
					varGardenHealth = 0;
				} else if(varGardenHealth < (varMaxGardenHealth / 10f)) {
					//If garden reaches less than 10% you lose.
					varGameState = GAMESTATE.dead;
				}
			}
			
			//Now, check if enemies are hit.
			if(nodPlayer.isAttacking()) {
				//Calculate where player is attacking
				float playerAttackedX = nodPlayer.getPosition().x + nodPlayer.getDirection().x * 3;
				float playerAttackedZ = nodPlayer.getPosition().z + nodPlayer.getDirection().z * 3;
				
				if(Math.abs(enemyX - playerAttackedX) < rad * 2 && Math.abs(enemyZ - playerAttackedZ) < rad * 2) {
					//enode.move(nodPlayer.getDirection().x * 10, 0, nodPlayer.getDirection().z * 10);
					if(!enode.isFleeing() && !enode.isFlinching()) {
						enode.onHit(nodPlayer.getDirection());
						killedInWave++;
						killedTotal++;
					}
				}
			}
		}
	}
	private void handleInput(float tpf) {
		switch(varGameState) {
		case play:
			
			//Handle Touching
			if(Gdx.input.isTouched()) {
				int x = Gdx.input.getX();
				int y = Gdx.input.getY();
				System.out.println(x + "," + y);
				
				if(x < CONST_SCREEN_WIDTH / 2) {
					if(Gdx.input.justTouched()) {
						TOUCH_MOVE_X = x;
						TOUCH_MOVE_Y = y;
					} else {
						float angle = 0;
						float difX = x - TOUCH_MOVE_X;
						float difY = y - TOUCH_MOVE_Y;
						if(difX >= 0) { 
							angle = (float)(Math.toDegrees(Math.atan(difY/difX)));
						} else {
							angle = (float)(Math.toDegrees(Math.atan(difY/difX))) + 180;
						}
						nodPlayer.setRotation(angle);
					}
				}
			}
			
			if(Gdx.input.isKeyPressed(Keys.ANY_KEY)) {
				if(!Gdx.input.isKeyPressed(Keys.E) && !Gdx.input.isKeyPressed(Keys.S) && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.roll && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack2 && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack3) {
					nodPlayer.setAnim(PlayerNode.ANIM.walk);
				}
			} else {
				nodPlayer.setAnim(PlayerNode.ANIM.stand);
			}
			if(Gdx.input.isKeyPressed(Keys.A)){
				if(nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.roll && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack2 && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack3){ 
					nodPlayer.rotate(5);
				}
			}
			if(Gdx.input.isKeyPressed(Keys.S)){
				if(nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.roll && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack2 && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack3){ 
					nodPlayer.setAnim(PlayerNode.ANIM.roll);
				}
			}
			if(Gdx.input.isKeyPressed(Keys.D)){
				if(nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.roll && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack2 && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack3){ 
					nodPlayer.rotate(-5);
				}
			}
			if(Gdx.input.isKeyPressed(Keys.W)){
				if(nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.roll && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack2 && nodPlayer.getCurrentAnimation() != PlayerNode.ANIM.attack3){ 
					nodPlayer.move(nodPlayer.getDirection());
				}
			}
			if(Gdx.input.isKeyPressed(Keys.R)){
				nodPlayer.setAnim(PlayerNode.ANIM.walk);
			}
			if(Gdx.input.isKeyPressed(Keys.E)){
				nodPlayer.setAnim(PlayerNode.ANIM.attack);
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
		
		sprTitle.fade(tpf * 2, (varGameState == GAMESTATE.title));
		sprTitle.draw(batch2d);
		
		if(varGameState == GAMESTATE.play){
			if(varUiAlpha < 1) {
				varUiAlpha += tpf;
			}
			if(varUiAlpha > 1) {
				varUiAlpha = 1;
			}
		} else {
			varUiAlpha -= tpf;
			if(varUiAlpha < 0) {
				varUiAlpha = 0;
			}
		}
		fntUi.setColor(1,1,1,varUiAlpha);
		fntUi.setScale(1);
		fntUi.draw(batch2d, "Wave " + varWave, -CONST_SCREEN_WIDTH/2 + fntUi.getSpaceWidth(), CONST_SCREEN_HEIGHT/2 - fntUi.getLineHeight()/2);
		fntUi.setScale(2);
		int health = Math.round(varGardenHealth / varMaxGardenHealth * 100);
		fntUi.draw(batch2d, health + "% Remaining", -fntUi.getBounds(health + "% Remaining").width/2f, CONST_SCREEN_HEIGHT/2 - fntUi.getLineHeight()/4);
		fntUi.setScale(1);
		fntUi.draw(batch2d, killedTotal + " defeated", CONST_SCREEN_WIDTH/2 - fntUi.getBounds(spawnedInWave + " Remaining").width - fntUi.getSpaceWidth(), CONST_SCREEN_HEIGHT/2 - fntUi.getLineHeight()/2);
		//Debug
		//fntUi.draw(batch2d, "wvdn:" + flgWaveEnd + " sz:" + lstEnemies.size() + " lf:" + (varGardenHealth/varMaxGardenHealth), -CONST_SCREEN_WIDTH/2f,0);
		
		//Draw death text
		
		if(varGameState == GAMESTATE.dead){
			fntUi.setColor(1,1,1,varDeathTextAlpha);
			fntUi.setScale(3);
			fntUi.draw(batch2d, "Garden Lost!", -fntUi.getBounds("Garden Lost!").width/2f, 0);
			fntUi.setScale(1);
			fntUi.setColor(1,1,1,1);
		} else if(varGameState == GAMESTATE.score){
			fntUi.setColor(1,1,1,1);
			fntUi.draw(batch2d, "Last Wave Reached: " + varWave, 0, 0);
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
}
