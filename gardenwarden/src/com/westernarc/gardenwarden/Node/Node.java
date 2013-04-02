package com.westernarc.gardenwarden.Node;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;

public class Node {
	protected Texture texture;
	protected Material material;
	protected StillModel model;
	
	public StillModel getModel() {
		return model;
	}
	public void setModel(StillModel model) {
		this.model = model;
	}
	public void render() {
		model.render();
	}
}
