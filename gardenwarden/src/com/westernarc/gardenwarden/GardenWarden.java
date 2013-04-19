package com.westernarc.gardenwarden;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.G3dExporter;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.westernarc.gardenwarden.Graphics.SpriteFade;
import com.westernarc.gardenwarden.Node.EnemyNode;
import com.westernarc.gardenwarden.Node.Node;
import com.westernarc.gardenwarden.Node.PlayerNode;

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

	Node nodPlant[];
	
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
	final int NOTHING = 0;
	final int CARROT = 1;
	final int PUMPKIN = 2;
	final int TOMATO = 3;
	final int WATERMELON = 4;
	final int SCALLION = 5;
	final int SQUASH = 6;
	
	final float PLANTWEIGHT[] = {0, 0.2f, 1.3f, 0.4f, 2f, 0.1f, 0.8f};
	final float PLANTRATE[] = {1, 1, 0.8f, 1, 0.7f, 1, 0.9f};
	
	float varBonus;//Multiplier for certain plants
	
	float varWeightTextScale;
	
	SpriteFade sprArrows;
	float TOUCHX;
	float TOUCHY;
	
	int varGameScore;
	
	Sprite sprBar;
	float varBarScale;
	float varRequestAlpha;
	float varWeightAlpha;
	float varScoreScale;
	
	Music musBackground;
	Sound sndCollect;
	Sound sndDrop;
	Sound sndPickup;
	
	float varJudgeAlpha;//Alpha of text that judges how well you met the request
	String strBestJudge = "Perfect Weight, Great!";
	String strOkJudge = "Bit Overweight, Not bad!";
	String strBadJudge = "...Overweight, But Ok";
	String strWorstJudge = "Too Much! You Joking?";
	float varJudgePosY;//Position of judge text vertically
	String varJudgeString; //Set this to the correct judge string
	boolean flgShowJudge; //Flag for whether or not to show judgement
	
	@Override
	public void create() {
		CONST_SCREEN_WIDTH = Gdx.graphics.getWidth();
		CONST_SCREEN_HEIGHT = Gdx.graphics.getHeight();
		TOUCHX = 0;
		TOUCHY = CONST_SCREEN_HEIGHT/4f;
		
		cam2d = new OrthographicCamera(CONST_SCREEN_WIDTH, CONST_SCREEN_HEIGHT);
		batch2d = new SpriteBatch();
		
		cam3d = new PerspectiveCamera(67, CONST_SCREEN_WIDTH, CONST_SCREEN_HEIGHT);
		batch3d = new DecalBatch();
		//batch3d.setGroupStrategy(new CameraGroupStrategy(cam3d));
		
		camDistance = 30;
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

		nodPlant = new Node[7];
		for(int i = 1; i <= 6; i++) {
			nodPlant[i] = new Node();
			nodPlant[i].setModel(loadModel("models/plant" + i));
			nodPlant[i].setMaterial(grassMat);
		}
		
		sprTitle = new SpriteFade(new Texture(Gdx.files.internal("textures/title.png")));
		sprTitle.setPosition(-sprTitle.getWidth()/2, -sprTitle.getHeight()/2);
		sprTitle.setScale(CONST_SCREEN_WIDTH / sprTitle.getWidth());
		sprTitle.fade(1, false);
		
		fntUi = new BitmapFont(Gdx.files.internal("text/BimboJVE_32.fnt"), false);
		varGameState = GAMESTATE.title;
		
		lstEnemies = new ArrayList<EnemyNode>();
		
		sprResetFilter = new SpriteFade(new Texture(Gdx.files.internal("textures/reset.png")));
		//16 x 16 are the dimensions of the fade sprite
		sprResetFilter.scale(Math.max(CONST_SCREEN_WIDTH, CONST_SCREEN_HEIGHT) / 16f);
		sprResetFilter.setPosition(-sprResetFilter.getWidth()/2f, -sprResetFilter.getHeight()/2f);
		sprResetFilter.fade(1, false);

		//Load enemy
		EnemyNode enode = new EnemyNode(EnemyNode.TYPE.lady);
		enode = null;
		
		sprArrows = new SpriteFade(new Texture(Gdx.files.internal("textures/arrows.png")));
		sprArrows.fade(1, false);
		sprArrows.setPosition(-sprArrows.getWidth()/2, -TOUCHY - sprArrows.getHeight()/2);
		
		varGameScore = 0;
		
		sprBar = new Sprite(new Texture(Gdx.files.internal("textures/bar.png")));
		sprBar.setScale(50,2);
		sprBar.setPosition(fntUi.getSpaceWidth() + sprBar.getBoundingRectangle().width/2 - CONST_SCREEN_WIDTH / 2f, CONST_SCREEN_HEIGHT / 2f);
		
		musBackground = Gdx.audio.newMusic(Gdx.files.internal("audio/Born Barnstomers.mp3"));
		musBackground.setLooping(true);
		musBackground.setVolume(0.2f);
		musBackground.play();
		
		sndDrop = Gdx.audio.newSound(Gdx.files.internal("audio/drop.wav"));
		sndPickup = Gdx.audio.newSound(Gdx.files.internal("audio/pickup.wav"));
		sndCollect = Gdx.audio.newSound(Gdx.files.internal("audio/collect.wav"));
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
		
		varRequestedWeight = 0.3f;  //Amount needed per round
		varTimePerRound = 60; //Time alloted to gather the weight needed
		varWeightProgress = 0; //Amount of weight currently gathered
		tmrRound = 0;
		
		varWeightTextScale = 1;
		varBarScale = ( (CONST_SCREEN_WIDTH - (fntUi.getSpaceWidth() * 2)) / sprBar.getWidth() );;
		
		varGameScore = 0;
		varRequestAlpha = 1;
		varWeightAlpha = 1;
		varScoreScale = 1;
		
		varPlantHeld = NOTHING;
		
		//Reset plant positions
		for(int i = 1; i <= 6; i++) {
			switch(i) {
			case CARROT:
				nodPlant[CARROT].setPosition(52,-2,-25.5f);
				break;
			case PUMPKIN:
				nodPlant[PUMPKIN].setPosition(52,-2,-16);
				break;
			case TOMATO:
				nodPlant[TOMATO].setPosition(52,-2,-7);
				break;
			case WATERMELON:
				nodPlant[i].setPosition(52,-2,3);
				break;
			case SCALLION:
				nodPlant[SCALLION].setPosition(52,-2,11);
				break;
			case SQUASH:
				nodPlant[SQUASH].setPosition(52,-2, 17.5f);
				break;
			}
		}
		
		varJudgeAlpha = 0;
		varJudgePosY = 0;
	}

	//Spawns waves
	private void updateWave() {
		//Spawn when the last wave has ended
		if(lstEnemies.size() < (cntRound + 6)) {
			EnemyNode nodEnemy;
			//Roly
			if(Math.random() > 0.5) {
				nodEnemy = new EnemyNode(EnemyNode.TYPE.roly);
			} else {
				nodEnemy = new EnemyNode(EnemyNode.TYPE.lady);
			}
			//Random from left, right or top
			double random = Math.random();
			if(random < 0.5) {
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
		
		//Draw veggies
		for(int i = 1; i <= 6; i++) {
			Gdx.gl10.glPushMatrix();
			Gdx.gl10.glTranslatef(nodPlant[i].getX(), nodPlant[i].getY(), nodPlant[i].getZ());
			Gdx.gl10.glScalef(0.5f, 0.5f, 0.5f);
			nodPlant[i].render();
			Gdx.gl10.glPopMatrix();
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
		if(varWeightTextScale > 1) {
			varWeightTextScale -= tpf * 2;
			if(varWeightTextScale < 1) {
				varWeightTextScale = 1;
			}
		}
		if(varScoreScale > 1) {
			varScoreScale -= tpf * 2;
			if(varScoreScale < 1) {
				varScoreScale = 1;
			}
		}
		
		if(flgShowJudge){
			varJudgePosY += tpf * CONST_SCREEN_HEIGHT/6;
			if(varJudgeAlpha < 1) {
				varJudgeAlpha += tpf * 2;
			} else if(varJudgeAlpha > 1) {
				varJudgeAlpha = 1;
				flgShowJudge = false;
			}
		} else {
			if(varJudgeAlpha > 0) {
				varJudgeAlpha -= tpf;
				varJudgePosY += tpf * CONST_SCREEN_HEIGHT/6;
			} else if(varJudgeAlpha < 0) {
				varJudgeAlpha = 0;
				varJudgePosY = 0;
			}
		}
		
		
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
			
			tmrRound += tpf;
			if(tmrRound > varTimePerRound) {
				tmrRound = varTimePerRound;
				varGameState = GAMESTATE.dead;
			}
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
			sprTitle.fade(1/60f, true);
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
			//Dont update enemies that are out of bounds
			enode.update(tpf);
			int rad = 2;
			float enemyX = enode.getX();
			float enemyZ = enode.getZ();
			
			if(Math.abs(enemyX - nodPlayer.getX()) < rad && Math.abs(enemyZ - nodPlayer.getZ()) < rad && nodPlayer.getX() < 52) {
				if(varPlantHeld != NOTHING) {
					sndDrop.play();
					varPlantHeld = NOTHING;
				}
			}
		}
		
		//Check player's location to determine which plant to give
		if(nodPlayer.getX() > 50) {
			float playerZ = nodPlayer.getZ();
			if(playerZ < -23 && playerZ > -27 && varPlantHeld != CARROT) {
				varPlantHeld = CARROT;
				sndPickup.play();
			} else if(playerZ > -20 && playerZ < -14 && varPlantHeld != PUMPKIN) {
				varPlantHeld = PUMPKIN;
				sndPickup.play();
			} else if(playerZ > -12 && playerZ < -3 && varPlantHeld != TOMATO) {
				varPlantHeld = TOMATO;
				sndPickup.play();
			} else if(playerZ < 6 && playerZ > 0 && varPlantHeld != WATERMELON) {
				varPlantHeld = WATERMELON;
				sndPickup.play();
			} else if(playerZ > 7 && playerZ < 14 && varPlantHeld != SCALLION) {
				varPlantHeld = SCALLION;
				sndPickup.play();
			} else if(playerZ > 15 && playerZ < 20 && varPlantHeld != SQUASH) {
				varPlantHeld = SQUASH;
				sndPickup.play();
			}
		}
		
		//Update veggies
		if(varPlantHeld >= 1 && varPlantHeld <= 6) {
			nodPlant[varPlantHeld].moveTo(tpf, nodPlayer.getX() + nodPlayer.getDirection().x * 8, 5, nodPlayer.getZ() + nodPlayer.getDirection().z * 8);
		}
		//Reset veggies
		for(int i = 1; i <= 6; i++) {
			//Behavior for plants that are not held
			if(i != varPlantHeld && nodPlant[i].getY() > -5) {
				nodPlant[i].pop(tpf);
			} else if(i != varPlantHeld && nodPlant[i].getY() < -3){
				//Reset it to row position
				switch(i) {
				case CARROT:
					nodPlant[CARROT].setPosition(52,-2,-25.5f);
					break;
				case PUMPKIN:
					nodPlant[PUMPKIN].setPosition(52,-2,-16);
					break;
				case TOMATO:
					nodPlant[TOMATO].setPosition(52,-2,-7);
					break;
				case WATERMELON:
					nodPlant[i].setPosition(52,-2,3);
					break;
				case SCALLION:
					nodPlant[i].setPosition(52,-2,11);
					break;
				case SQUASH:
					nodPlant[i].setPosition(52,-2, 17.5f);
					break;
				}
			}
		}
		//If the player makes it to stash with veggie, update accordingly
		if(nodPlayer.getX() < -3 && (nodPlayer.getZ() > -14 && nodPlayer.getZ() < 14)) {
			if(varPlantHeld != NOTHING) {
				varWeightProgress += PLANTWEIGHT[varPlantHeld];
				varWeightTextScale = 1.1f;
				sndCollect.play();
				if(varWeightProgress >= varRequestedWeight){
					//Add to score depending on weight gathered
					if(varWeightProgress == varRequestedWeight) {
						varGameScore += 200;
						varJudgeString = strBestJudge;
					} else if(varWeightProgress - varRequestedWeight <= 0.1) {
						varGameScore += 100;
						varJudgeString = strOkJudge;
					} else if(varWeightProgress - varRequestedWeight <= 0.2) {
						varGameScore += 20;
						varJudgeString = strBadJudge;
					} else {
						//No score award
						varJudgeString = strWorstJudge;
					}
					varScoreScale = 1.1f;
					//Request Completed
					flgShowJudge = true;
					
					varRequestedWeight = Math.round(((Math.random()) * 27) + 1) / 10f;
					cntRound++;
					varWeightProgress = 0;
					tmrRound = 0;
				}
				varPlantHeld = NOTHING;
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
				sprArrows.fade(tpf * 2, true);
				float angle = 0;
				float difX = (x - TOUCHX) - CONST_SCREEN_WIDTH / 2;
				float difY = (y - TOUCHY) - CONST_SCREEN_HEIGHT / 2;
				if(difX == 0) {
					difX = 1;
				}
				if(difX >= 0) { 
					angle = (float)(Math.toDegrees(Math.atan(difY/difX))) - 90;
				} else {
					angle = (float)(Math.toDegrees(Math.atan(difY/difX))) + 90;
				}
				nodPlayer.setRotation(-angle);
				if(varPlantHeld == 0) {
					nodPlayer.move(nodPlayer.getDirection());
				} else {
					nodPlayer.move(nodPlayer.getDirection().cpy().mul(PLANTRATE[varPlantHeld]));
				}
				nodPlayer.setAnim(PlayerNode.ANIM.walk);
			} else {
				nodPlayer.setAnim(PlayerNode.ANIM.stand);
				sprArrows.fade(tpf * 2, false);
			}
			/*
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
			}*/
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
		fntUi.setScale(varScoreScale);
		fntUi.draw(batch2d, varGameScore + "pts", -CONST_SCREEN_WIDTH/2 + fntUi.getSpaceWidth(), CONST_SCREEN_HEIGHT/2 - fntUi.getLineHeight()/2);
		fntUi.setColor(1,1,1, Math.min(varUiAlpha, varRequestAlpha));
		fntUi.setScale(0.8f);
		fntUi.draw(batch2d, "Gather " + Math.round(varRequestedWeight * 10) / 10f + " lbs", -CONST_SCREEN_WIDTH/2 + fntUi.getSpaceWidth(), CONST_SCREEN_HEIGHT/2 - fntUi.getLineHeight() * 3 / 2f);
		//fntUi.setScale(2);
		//String time = Math.round(varTimePerRound - tmrRound) + "s";
		//fntUi.draw(batch2d, time, -fntUi.getBounds(time).width/2f, CONST_SCREEN_HEIGHT/2 - fntUi.getLineHeight()/2);
		fntUi.setScale(varWeightTextScale);
		String weightStats = Math.round(varWeightProgress * 10) / 10f + " lbs";
		fntUi.draw(batch2d, weightStats, CONST_SCREEN_WIDTH/2 - fntUi.getBounds(weightStats).width - fntUi.getSpaceWidth(), CONST_SCREEN_HEIGHT/2 - fntUi.getLineHeight()/2);
		
		if(varPlantHeld != 0) {
			if(varWeightAlpha < 1) {
				varWeightAlpha += tpf * 2;
				if(varWeightAlpha > 1) {
					varWeightAlpha = 1;
				}
			}
		} else {
			if(varWeightAlpha > 0) {
				varWeightAlpha -= tpf * 2;
				if(varWeightAlpha < 0) {
					varWeightAlpha = 0;
				}
			}
		}
		if(Math.min(varUiAlpha, varWeightAlpha) > 0) {
			fntUi.setColor(1,1,1, Math.min(varUiAlpha, varWeightAlpha));
			fntUi.setScale(0.8f);
			String addStats = "+" + Math.round(PLANTWEIGHT[varPlantHeld] * 10) / 10f + "lbs";
			fntUi.draw(batch2d, addStats, CONST_SCREEN_WIDTH/2 - fntUi.getBounds(addStats).width - fntUi.getSpaceWidth(), CONST_SCREEN_HEIGHT/2 - fntUi.getLineHeight() * 3 / 2f);
		}
		fntUi.setColor(1,1,1,varUiAlpha);
		fntUi.setScale(1);
		//Debug
		//fntUi.draw(batch2d, "wvdn:" + flgWaveEnd + " sz:" + lstEnemies.size() + " lf:" + (varGardenHealth/varMaxGardenHealth), -CONST_SCREEN_WIDTH/2f,0);
		
		//Draw death text
		
		if(varGameState == GAMESTATE.dead) {
			fntUi.setColor(1,1,1,varDeathTextAlpha);
			fntUi.setScale(2);
			fntUi.draw(batch2d, "Request Failed!", -fntUi.getBounds("Request Failed!").width/2f, fntUi.getLineHeight());
			fntUi.setScale(1);
			fntUi.setColor(1,1,1,1);
		} else if(varGameState == GAMESTATE.score){
			fntUi.setColor(1,1,1,1);
			String score = "Requests Fulfilled: " + cntRound;
			fntUi.draw(batch2d, score, -fntUi.getBounds(score).width/2f, fntUi.getLineHeight() * 3);
			String score2 = "Score: " + varGameScore;
			fntUi.draw(batch2d, score2, -fntUi.getBounds(score2).width/2f, fntUi.getLineHeight() * 2);
			
			fntUi.draw(batch2d, "Touch to retry!", -fntUi.getBounds("Touch to retry!").width/2f, -fntUi.getLineHeight() * 3);
		}
		
		//Draw Touch Arrows
		sprArrows.draw(batch2d);
		
		//Draw bar time scale
		float barScale = ((varTimePerRound - (float)tmrRound )/ varTimePerRound) * ( (CONST_SCREEN_WIDTH - (fntUi.getSpaceWidth() * 2)) / sprBar.getWidth() );
		if(varBarScale < barScale && barScale - varBarScale > 1) {
			varBarScale += 1;
			if(varBarScale > (CONST_SCREEN_WIDTH - (fntUi.getSpaceWidth() * 2)) / sprBar.getWidth()) {
				varBarScale = ( (CONST_SCREEN_WIDTH - (fntUi.getSpaceWidth() * 2)) / sprBar.getWidth() );;
			}
		} else if(varBarScale > barScale) {
			varBarScale -= 1;
		}
		sprBar.setColor(1,1,1, varUiAlpha);
		varBarScale = Math.round(varBarScale);

		sprBar.setScale(varBarScale, 2);
		sprBar.setPosition(fntUi.getSpaceWidth() + sprBar.getBoundingRectangle().width/2 - CONST_SCREEN_WIDTH / 2f, CONST_SCREEN_HEIGHT / 2f - fntUi.getLineHeight() / 4f);
		sprBar.draw(batch2d);
		
		if(varJudgeAlpha > 0 && varJudgeString != null) {
			fntUi.setColor(1,1,1,varJudgeAlpha);
			fntUi.draw(batch2d, varJudgeString, -fntUi.getBounds(varJudgeString).width/2f, varJudgePosY);
			fntUi.setColor(1,1,1,1);
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
			StillModel model = ModelLoaderRegistry.loadStillModel(Gdx.files.internal(url + ".g3dt"));
			G3dExporter.export(model, Gdx.files.absolute(url + ".g3d"));
			return ModelLoaderRegistry.loadStillModel(Gdx.files.internal(url + ".g3dt"));
		} else {
			return ModelLoaderRegistry.loadStillModel(Gdx.files.internal(url + ".g3d"));
		}
	}
}
