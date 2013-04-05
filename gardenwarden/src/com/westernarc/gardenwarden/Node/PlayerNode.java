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

public class PlayerNode extends Node {
	private StillModel framesWalk[];
	private StillModel framesStand[];
	private StillModel framesStandL[];
	private StillModel framesAttack[];
	private StillModel framesAttack2[];
	private StillModel framesAttack3[];
	private StillModel framesRoll[];
	
	private int cntCurFrame;
	private float tmrFrame;
	private static final float CONST_FRAMERATE = 1/31f;
	
	public enum ANIM {walk, stand, standL, attack, attack2, attack3, roll}
	private ANIM varCurAnimation;
	
	private boolean flgWalkToStand;
	private boolean flgAttack2;
	private boolean flgAttack3;
	
	private Vector3 direction;
	public static final float CONST_SPEED = 0.2f;
	
	public PlayerNode() {
		texture = new Texture(Gdx.files.internal("textures/playergardentex.png"));
		material = new Material("mat", new TextureAttribute(texture, 0, "s_tex"), new ColorAttribute(Color.WHITE, ColorAttribute.diffuse));
		
		framesWalk = new StillModel[20];
		for(int i = 0; i < 20; i++) {
			framesWalk[i] = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("models/playergarden"+i+".g3dt"));
			framesWalk[i].setMaterial(material);
		}
		framesStand = new StillModel[6];
		for(int i = 0; i < 6; i++) {
			framesStand[i] = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("models/playerstand"+i+".g3dt"));
			framesStand[i].setMaterial(material);
		}
		framesStandL = new StillModel[6];
		for(int i = 0; i < 6; i++) {
			framesStandL[i] = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("models/playerstandL"+i+".g3dt"));
			framesStandL[i].setMaterial(material);
		}
		framesAttack = new StillModel[16];
		for(int i = 0; i < 16; i++) {
			framesAttack[i] = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("models/playerattack"+i+".g3dt"));
			framesAttack[i].setMaterial(material);
		}
		framesAttack2 = new StillModel[16];
		for(int i = 0; i < 16; i++) {
			framesAttack2[i] = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("models/attack3/attack2"+i+".g3dt"));
			framesAttack2[i].setMaterial(material);
		}
		framesAttack3 = new StillModel[21];
		for(int i = 0; i < 21; i++) {
			framesAttack3[i] = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("models/attack3/attack3"+i+".g3dt"));
			framesAttack3[i].setMaterial(material);
		}
		framesRoll = new StillModel[21];
		for(int i = 0; i < 21; i++) {
			framesRoll[i] = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("models/attack3/roll"+i+".g3dt"));
			framesRoll[i].setMaterial(material);
		}
		
		varCurAnimation = ANIM.stand;
		
		model = framesStand[5];
		
		cntCurFrame = 5;
		
		flgWalkToStand = false;
		flgAttack2 = false;
		flgAttack3 = false;
		
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
		case attack:
			if(cntCurFrame >= 16) {
				cntCurFrame = 1;
			}
			break;
		case attack2:
			if(cntCurFrame >= 16) {
				cntCurFrame = 1;
			}
			break;
		case attack3:
			if(cntCurFrame >= 21) {
				cntCurFrame = 1;
			}
			break;
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
		case roll:
			if(cntCurFrame > 20) {
				cntCurFrame = 1;
			}
		default:
			break;
		
		}

		switch(varCurAnimation) {
		case attack:
			if(cntCurFrame == 15) {
				//When the attack is over, branch.
				cntCurFrame = 1;
				//Depending on flgAttack2
				if(flgAttack2) {
					flgAttack2 = false;
					varCurAnimation = ANIM.attack2;
					setModel(framesAttack2[cntCurFrame]);
				} else {
					varCurAnimation = ANIM.stand;
					setModel(framesStand[cntCurFrame]);
				}
			} else {
				//During attacks, move forward for the first 5 frames
				if(cntCurFrame < 6) {
					move(direction.cpy().mul(1.5f));
				}
				setModel(framesAttack[cntCurFrame]);
			}
			break;
		case attack2:
			if(cntCurFrame == 15) {
				cntCurFrame = 1;
				if(flgAttack3) {
					flgAttack3 = false;
					varCurAnimation = ANIM.attack3;
					setModel(framesAttack3[cntCurFrame]);
				} else {
					varCurAnimation = ANIM.standL;
					setModel(framesStandL[cntCurFrame]);
				}
			} else {
				//During attacks, move forward for the first 5 frames
				if(cntCurFrame < 9 && cntCurFrame > 2) {
					move(direction.cpy().mul(1.5f));
				}
				setModel(framesAttack2[cntCurFrame]);
			}
			break;
		case attack3:
			if(cntCurFrame == 20) {
				cntCurFrame = 1;
				varCurAnimation = ANIM.stand;
				setModel(framesStand[cntCurFrame]);
			} else {
				if(cntCurFrame < 9 && cntCurFrame > 3) {
					move(direction.cpy().mul(1.5f));
				}
				setModel(framesAttack3[cntCurFrame]);
			}
			break;
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
		case roll:
			if(cntCurFrame == 20) {
				cntCurFrame = 1;
				varCurAnimation = ANIM.stand;
				setModel(framesStand[cntCurFrame]);
			} else {
				if(cntCurFrame < 18 && cntCurFrame > 3) {
					if(cntCurFrame < 9)
						move(direction.cpy().mul(3f));
					else
						move(direction.cpy().mul(1.5f));
				}
				setModel(framesRoll[cntCurFrame]);
			}
		default:
			break;

		}
	}
	
	public void setAnim(ANIM anim) {
		switch(anim){
		case attack:
			//From standing or running
			if(varCurAnimation != ANIM.roll  && varCurAnimation != ANIM.attack  && varCurAnimation != ANIM.attack2 && varCurAnimation != ANIM.attack3){
				varCurAnimation = ANIM.attack;
				cntCurFrame = 1;
			} else {
				//During 1st attack
				if(!flgAttack2 && !flgAttack3 && varCurAnimation != ANIM.attack2) {
					if(cntCurFrame > 5) {
						flgAttack2 = true;
					}
				}
				//During 2nd attack
				else if(!flgAttack2 && !flgAttack3 && varCurAnimation != ANIM.attack3) {
					if(cntCurFrame > 5) {
						flgAttack3 = true;
					}
				}
			}
			break;
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
		case roll:
			varCurAnimation = ANIM.roll;
			cntCurFrame = 1;
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
}
