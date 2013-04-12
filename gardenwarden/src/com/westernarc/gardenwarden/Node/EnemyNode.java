package com.westernarc.gardenwarden.Node;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.westernarc.gardenwarden.GardenWarden;
import com.westernarc.gardenwarden.Node.PlayerNode.ANIM;

public class EnemyNode extends Node {
	//Use this to give a red tint when hit
	static StillModel framesWalkRoly[];
	static StillModel framesWalkLady[];
	static Texture bugtex;
	static Material bugmat;
	private int cntCurFrame;
	private float tmrFrame;
	private static final float CONST_FRAMERATE = 1/30f;
	
	public enum ANIM {walk}
	private ANIM varCurAnimation;
	
	private Vector3 direction;
	private float moveSpeed = 0.1f;
	
	private Node target;
	private Vector3 updateTarget;
	
	private static final float CONST_TARGET_CHANGE_TIME = 3;
	private float tmrTargetChange;
	
	private boolean fleeing;
	private boolean flinching;
	//private float flinchTime = 0.2f;
	//private float tmrFlinchRot;
	
	private float flinchVectorX;
	private float flinchVectorZ;
	
	public enum TYPE {roly, lady, grub}
	TYPE type;
	public EnemyNode(TYPE type) {
		this.type = type;
		if(bugtex == null)
			bugtex = new Texture(Gdx.files.internal("textures/gardentex.png"));
		if(bugmat == null)
			bugmat = new Material("mat", new TextureAttribute(bugtex, 0, "s_tex"), new ColorAttribute(Color.WHITE, ColorAttribute.diffuse));
		
		if(framesWalkRoly == null) {
			framesWalkRoly = new StillModel[10];
			for(int i = 0; i < 10; i++) {
				if(framesWalkRoly[i] == null) {
					framesWalkRoly[i] = GardenWarden.loadModel("models/roly/roly"+i);
					framesWalkRoly[i].setMaterial(bugmat);
				}
			}
		}
		if(framesWalkLady == null) {
			framesWalkLady = new StillModel[10];
			for(int i = 0; i < 10; i++) {
				if(framesWalkLady[i] == null) {
					framesWalkLady[i] = GardenWarden.loadModel("models/lady/lady"+i);
					framesWalkLady[i].setMaterial(bugmat);
				}
			}
		}
		varCurAnimation = ANIM.walk;
		switch(type) {
		case roly:
			model = framesWalkRoly[0];
			break;
		case lady:
			model = framesWalkLady[0];
			break;
		case grub:
			break;
		}
		cntCurFrame = 0;
		
		direction = new Vector3();
		rotation = 0;
		
		target = new Node();
		position.set(0,0,6);
		
		tmrTargetChange = 0;
		updateTarget = new Vector3();
		
		fleeing = false;
		flinching = false;
	}
	public void update(float tpf) {
		if(!fleeing && !flinching) {
			tmrTargetChange += tpf;
			if(tmrTargetChange > CONST_TARGET_CHANGE_TIME) {
				tmrTargetChange = 0;
				updateTarget.set((float)Math.random() * 20 + 2,0,(float)Math.random() * 70 - 35);
			}
		} else if(flinching) {
			direction.x = flinchVectorX;
			direction.z = flinchVectorZ;
			direction.y = direction.y - 0.01f;
		} else if(fleeing) {
			updateTarget.set(flinchVectorX * 1000, 0, flinchVectorZ * 1000);
		}
		if(!flinching) {
			Vector3 toTargetVec = updateTarget.cpy().sub(position);
			
			target.setPosition(toTargetVec.nor());
	
			float angle = 0;
			if(target.getX() >= 0) { 
				angle = (float)(Math.toDegrees(Math.atan(target.getZ()/target.getX())));
			} else {
				angle = (float)(Math.toDegrees(Math.atan(target.getZ()/target.getX()))) + 180;
			}
			if(angle <= 0) {
				angle += 360;
			}
			float rotAngle = 360 - rotation;
			if(rotAngle >= 360) rotAngle -= 360;
	
			float distLeft = 0;
			float distRight = 0;
			if(rotAngle < angle) {
				distLeft = rotAngle - angle + 360;
				distRight = angle - rotAngle;
			} else {
				distLeft = rotAngle - angle;
				distRight = angle - rotAngle + 360; 
			}
	
			if(distLeft > distRight) {
				rotation -= tpf * 60;
			} else {
				rotation += tpf * 60;
			}
	
			direction.set((float)Math.cos(rotation / 360 * Math.PI * 2), 0, -(float)Math.sin(rotation / 360 * Math.PI * 2));
	
			if(direction.x > 0) {
				rotation = (float)(Math.toDegrees(Math.atan(direction.z / -direction.x)));
			} else {
				rotation = (float)(Math.toDegrees(Math.atan(direction.z / -direction.x) + (Math.PI)));
			}
		}
		move(direction.x * moveSpeed, direction.y * moveSpeed, direction.z * moveSpeed);
		
		tmrFrame += tpf;
		if(tmrFrame > CONST_FRAMERATE) {
			tmrFrame = 0;
			cntCurFrame++;
		}
		switch(varCurAnimation) {
		case walk:
			if(cntCurFrame >= 10) {
				cntCurFrame = 1;
			}
			switch(type) {
			case roly:
				model = framesWalkRoly[cntCurFrame];
				break;
			case lady:
				model = framesWalkLady[cntCurFrame];
				break;
			case grub:
				break;
			}
			break;
		default:
			break;
		}
	}
	public Vector3 getDirection() {
		return direction;
	}
	
	public ANIM getCurrentAnimation() {
		return varCurAnimation;
	}
	public void onHit(Vector3 attackVec) {
		flinching = true;
		flinchVectorX = attackVec.x;
		flinchVectorZ = attackVec.z;
		moveSpeed = 3;
		direction.y = 0.2f;
	}
	public boolean isFleeing() {
		return fleeing;
	}
	public boolean isFlinching() {
		return flinching;
	}
}
