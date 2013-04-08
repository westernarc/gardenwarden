package com.westernarc.gardenwarden.Node;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.math.Vector3;
import com.westernarc.gardenwarden.GardenWarden;
import com.westernarc.gardenwarden.Graphics.EffectDecal;

public class PlayerNode extends Node {
	private StillModel framesWalk[];
	private StillModel framesStand[];
	private StillModel framesStandL[];
	
	private int cntCurFrame;
	private float tmrFrame;
	private static final float CONST_FRAMERATE = 1/31f;
	
	public enum ANIM {walk, stand, standL}
	private ANIM varCurAnimation;
	
	private boolean flgWalkToStand;

	private Vector3 direction;
	public static final float CONST_SPEED = 0.2f;

	
	public PlayerNode() {
		texture = new Texture(Gdx.files.internal("textures/playergardentex.png"));
		material = new Material("mat", new TextureAttribute(texture, 0, "s_tex"), new ColorAttribute(Color.WHITE, ColorAttribute.diffuse));
		
		framesWalk = new StillModel[20];
		for(int i = 0; i < 20; i++) {
			framesWalk[i] = GardenWarden.loadModel("models/playergarden" + i);
			framesWalk[i].setMaterial(material);
		}
		framesStand = new StillModel[6];
		for(int i = 0; i < 6; i++) {
			framesStand[i] = GardenWarden.loadModel("models/playerstand" + i);
			framesStand[i].setMaterial(material);
		}
		framesStandL = new StillModel[6];
		for(int i = 0; i < 6; i++) {
			framesStandL[i] = GardenWarden.loadModel("models/playerstandL" + i);
			framesStandL[i].setMaterial(material);
		}
		
		varCurAnimation = ANIM.stand;
		
		model = framesStand[5];
		
		cntCurFrame = 5;
		
		flgWalkToStand = false;
		direction = new Vector3();
	}
	
	public void update(float tpf) {
		direction.set(-(float)Math.cos(rotation / 360 * Math.PI * 2) * CONST_SPEED, 0, (float)Math.sin(rotation / 360 * Math.PI * 2) * CONST_SPEED);
		
		tmrFrame += tpf;
		if(tmrFrame > CONST_FRAMERATE) {
			tmrFrame = 0;
			cntCurFrame++;
		}
		switch(varCurAnimation) {
		case stand:
		case standL:
			if(cntCurFrame >= 5) {
				cntCurFrame = 5;
			}
			break;
		case walk:
			if(cntCurFrame >= 20) {
				cntCurFrame = 1;
			}
			break;
		default:
			break;
		
		}

		switch(varCurAnimation) {
		case stand:
			setModel(framesStand[cntCurFrame]);
			break;
		case standL:
			setModel(framesStandL[cntCurFrame]);
			break;
		case walk:
			if(flgWalkToStand && cntCurFrame == 5) {
				cntCurFrame = 1;
				varCurAnimation = ANIM.stand;
				setModel(framesStand[cntCurFrame]);
				flgWalkToStand = false;
			} else if(flgWalkToStand && cntCurFrame == 15) {
				cntCurFrame = 1;
				varCurAnimation = ANIM.standL;
				setModel(framesStandL[cntCurFrame]);
				flgWalkToStand = false;
			} else {
				setModel(framesWalk[cntCurFrame]);
			}
			break;
		}
	}
	
	public void setAnim(ANIM anim) {
		switch(anim){
		case stand:
			if(varCurAnimation == ANIM.walk) 
				flgWalkToStand = true;
			break;
		case walk:
			if(varCurAnimation == ANIM.stand){
				cntCurFrame = 5;
			} else if(varCurAnimation == ANIM.standL) {
				cntCurFrame = 15;
			}
			varCurAnimation = anim;
			break;		
		}
	}
	public Vector3 getDirection() {
		return direction;
	}
	
	public ANIM getCurrentAnimation() {
		return varCurAnimation;
	}
	public void setFrame(int frame) {
		cntCurFrame = frame;
	}
}
