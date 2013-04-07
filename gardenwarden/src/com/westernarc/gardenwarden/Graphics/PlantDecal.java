package com.westernarc.gardenwarden.Graphics;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;

public class PlantDecal {
	Decal decal;
	
	//Plants rotate back and forth clockwise and clockwise
	//on a small arc when they are idle.
	//When they are being eaten, they shake left and right
	float life;
	
	boolean shakeDirection;
	float shakePosition;
	float shakeRate = 10;
	float shakeMax = 0.1f;
	
	boolean rotationDirection;
	float idleRotation;
	float  idleRotationRate = 10;
	float idleRotationMax = 10;
	
	boolean isBeingEaten;
	
	public PlantDecal(TextureRegion texRegion) {
		decal = Decal.newDecal(texRegion);
	}
	
	public boolean isBeingEaten() {
		return isBeingEaten;
	}
	
	public void update(float tpf) {
		//Deal with plant rotation while idle
		if(rotationDirection) {
			idleRotation += tpf * idleRotationMax;
		} else {
			idleRotation -= tpf * idleRotationMax;
		}
		if(idleRotation > idleRotationMax) {
			idleRotation = idleRotationMax;
			rotationDirection = false;
		} else if(idleRotation < -idleRotationMax) {
			idleRotation = -idleRotationMax;
			rotationDirection = true;
		}
		decal.rotateZ(idleRotation);
		//If it's being eaten, shake it back and forth
		if(isBeingEaten) {
			
		} else {
			//If it's not, recenter it
			
		}
	}
	
	public void onEaten() {
		isBeingEaten = true;
	}
}
