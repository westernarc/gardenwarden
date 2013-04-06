package com.westernarc.gardenwarden.Graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;

public class EffectDecal {
	Decal decal;
	
	int numOfFrames;
	float framerate;
	float tmrFrame;
	int curFrame;
	int frameWidth, frameHeight;
	
	Decal[] frames;
	
	private boolean playing;
	
	public EffectDecal(int n, float fr, int frameWidth, int frameHeight) {
		numOfFrames = n;
		framerate = fr;
		playing = false;
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
	}
	
	public void setAnimationFiles(String url) {
		frames = new Decal[numOfFrames];
		for(int i = 0; i < numOfFrames; i++) {
			frames[i] = Decal.newDecal(new TextureRegion(new Texture(Gdx.files.internal(url + i + ".png"))), true);
		}
		decal = frames[0];
	}
	public boolean isPlaying() {
		return playing;
	}
	public void play() {
		playing = true;
	}
	public void update(float tpf) {
		if(playing) {
		tmrFrame += tpf;
			if(tmrFrame > framerate) {
				tmrFrame = 0;
				curFrame++;
				if(curFrame >= numOfFrames) {
					playing = false;
					curFrame = 0;
				}
				decal = frames[curFrame];
			}
		}
	}
	public Decal getDecal() {
		return decal;
	}
	
	public void rotateX(float rot) {
		for(int i = 0; i < numOfFrames; i++) {
			frames[i].rotateX(rot);
		}
	}
	public void rotateY(float rot) {
		for(int i = 0; i < numOfFrames; i++) {
			frames[i].rotateY(rot);
		}
	}
	public void setPosition(float x, float y, float z) {
		for(int i = 0; i < numOfFrames; i++) {
			frames[i].setPosition(x,y,z);
		}
	}
	public void setRotationX(float r) {
		for(int i = 0; i < numOfFrames; i++) {
			frames[i].setRotationX(r);
		}
	}
	public void setRotationY(float r) {
		for(int i = 0; i < numOfFrames; i++) {
			frames[i].setRotationY(r);
		}
	}
	public void setRotationZ(float r) {
		for(int i = 0; i < numOfFrames; i++) {
			frames[i].setRotationZ(r);
		}
	}
	public void setScale(float s) {
		for(int i = 0; i < numOfFrames; i++) {
			frames[i].setScale(s);
		}
	}
	
}