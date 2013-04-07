package com.westernarc.gardenwarden.Graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class SpriteFade extends Sprite {
	public SpriteFade(Texture tex) {
		super(tex);
	}
	public void fade(float rate, boolean fadeIn) {
		//Rate is rate that alpha changes
		//fadeIn is whether to fade in or fade out
		if(fadeIn) {
			if(getColor().a < 1) {
				if(getColor().a + rate <= 1) {
					setColor(getColor().r,getColor().b,getColor().g, getColor().a + rate);
				} else {
					setColor(getColor().r, getColor().b, getColor().g, 1);
				}
			}
		} else {
			if(getColor().a > 0) {
				if(getColor().a - rate >= 0) {
					setColor(getColor().r, getColor().b, getColor().g, getColor().a - rate);
				} else {
					setColor(getColor().r, getColor().b, getColor().g, 0);
				}
			}
		}
	}
	public float getAlpha() {
		return getColor().a;
	}
}
