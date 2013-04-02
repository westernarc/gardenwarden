package com.westernarc.gardenwarden;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.westernarc.gardenwarden.Node.PlayerNode;

public class GardenWarden implements ApplicationListener {
	private float SCREEN_WIDTH;
	private float SCREEN_HEIGHT;
	
	private OrthographicCamera cam2d;
	private PerspectiveCamera cam3d;
	private SpriteBatch batch;
	
	float camAngle;
	float camDistance;

	//Game flow:
	//Splash Screen: 
	private enum GAME_STATE {
		splash, title, play, 
	}
	private GAME_STATE gameState;
	
	PlayerNode nodPlayer;
	
	@Override
	public void create() {		
		SCREEN_WIDTH = Gdx.graphics.getWidth();
		SCREEN_HEIGHT = Gdx.graphics.getHeight();
		
		cam2d = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
		batch = new SpriteBatch();
		
		cam3d = new PerspectiveCamera(67, SCREEN_WIDTH, SCREEN_HEIGHT);
		camDistance = 100;
		cam3d.position.set((float)Math.sin(camAngle) * camDistance, 120, (float)Math.cos(camAngle) * camDistance);
		cam3d.lookAt(0, 70, 0);
		cam3d.far = 1000;
		
		
		nodPlayer = new PlayerNode();
	}

	@Override
	public void dispose() {
		batch.dispose();
	}

	@Override
	public void render() {
		float tpf = Gdx.graphics.getDeltaTime();
		
		Gdx.gl10.glClearColor(0.2f, 0.2f, 0.2f, 1);
		
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		Gdx.gl10.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl10.glEnable(GL10.GL_TEXTURE_2D);
		Gdx.gl10.glEnable(GL10.GL_COLOR_MATERIAL);
		
		Gdx.gl10.glScalef(20, 20, 20);
		nodPlayer.render();
		
		Gdx.gl10.glDisable(GL10.GL_DEPTH_TEST);
		Gdx.gl10.glDisable(GL10.GL_TEXTURE_2D);
		Gdx.gl10.glDisable(GL10.GL_COLOR_MATERIAL);
		
		//Draw 3d
		batch.setProjectionMatrix(cam2d.combined);
		batch.begin();
		batch.end();
		

		if(Gdx.input.isKeyPressed(Keys.A)){
			camAngle -= 0.1f;
		}
		if(Gdx.input.isKeyPressed(Keys.S)){
			camDistance -= 1;
		}
		if(Gdx.input.isKeyPressed(Keys.D)){
			camAngle += 0.1f;
		}
		if(Gdx.input.isKeyPressed(Keys.W)){
			camDistance += 1;
		}
		if(Gdx.input.isKeyPressed(Keys.R)){
			nodPlayer.setAnim(PlayerNode.ANIM.walk);
		}
		if(Gdx.input.isKeyPressed(Keys.E)){
			nodPlayer.setAnim(PlayerNode.ANIM.stand);
		}
		cam3d.position.set((float)Math.sin(camAngle) * camDistance, 120, (float)Math.cos(camAngle) * camDistance);
		cam3d.lookAt(0, 70, 0);
		cam3d.update(true);
		cam3d.apply(Gdx.gl10);
		
		
		
		nodPlayer.update(tpf);
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
