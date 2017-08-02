package com.jeffreywaring.TrumpGrab;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;


import java.util.ArrayList;
import java.util.Random;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {

	public int score = 0;
	public int misses = 0;
	public int level = 1;
	public int speed = 5;
	public int levelIndicator = 0;

	private SpriteBatch batch;
	private BitmapFont font;

	private Texture andrew;
	private Sprite drew_sprite;

	public ArrayList<mObject> bottles = new ArrayList<mObject>();
	public ArrayList<mObject> naps = new ArrayList<mObject>();

	private int screenWidth,screenHeight;
	private String message = "Grab Ivanka, but watch out for Putins!";

	float bottleYPosition = 900;
	float bottleXPosition = 100;

	float napYPosition = 900;
	float napXPosition = 450;

	private Rectangle rectangleDrew;

	public Sound andrewHappy;

	private class mObject{
		//An mObject is comprised of two parts: a sprite and a rectangle
		//the rectangle is used for collision detection,
		//both rectangle and sprite move together
		//two types of mObjects: bottles (designated by AmIaBottle = true
		//                       and naps, by AmIaBottle = false

		private Rectangle thisRectangle;
		private Sprite thisSprite;
		private Texture mTexture;
		private boolean AmIaBottle;

		public mObject(float initialX, float initialY, boolean isBottle){
			if(isBottle){
				mTexture = new Texture("ivanka.jpg");
			}else{
				mTexture = new Texture("putins.jpg");
			}
			AmIaBottle = isBottle;
			thisSprite = new Sprite(mTexture);
			thisSprite.setPosition(initialX,initialY);

			thisRectangle = new Rectangle(thisSprite.getX(),thisSprite.getY(),
					thisSprite.getWidth(),thisSprite.getHeight());

		}
		//Since sprites and rectangles always coincide, only need one set of getters and setters
		public float getX() {
			return thisSprite.getX();
		}
		public void setX(float X) {
			thisSprite.setX(X);
			thisRectangle.setX(X);
		}
		public float getY() {
			return thisSprite.getY();
		}
		public void setY(float Y) {
			thisSprite.setY(Y);
			thisRectangle.setY(Y);
		}
		public boolean getBottleStatus(){
			return AmIaBottle;
		}
		public void draw(Batch batch){
			thisSprite.draw(batch);
		}
		public Rectangle rectangle(){
			return thisRectangle;
		}
		public void dispose(){
			mTexture.dispose();
		}
	}
	private AdsController adsController;

	public MyGdxGame(AdsController adsController){
		this.adsController = adsController;
	}
	@Override
	public void create () {

		adsController.showBannerAd();

		batch = new SpriteBatch();

		//clump the creation of an Andrew sprite here
		andrew = new Texture("Trump_face.jpg");
		//this takes the raw image, and converts it to a "game" image
		drew_sprite = new Sprite(andrew);
		rectangleDrew = new Rectangle(drew_sprite.getX(),drew_sprite.getY(),drew_sprite.getWidth(),drew_sprite.getHeight());

		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();

		font = new BitmapFont();
		font.setColor(Color.GREEN);
		font.getData().scale(5);

		//identifies input methods as in this class
		Gdx.input.setInputProcessor(this);

		mObject bottle;
		mObject nap;
		//initial bottle
		bottle = new mObject(bottleXPosition,bottleYPosition,true);
		bottles.add(bottle);
		//initial nap
		nap = new mObject(napXPosition,napYPosition,false);
		naps.add(nap);

		//Try and load sound here
		AssetManager assetManager = new AssetManager();
		assetManager.load("happy.mp3", Sound.class);
		assetManager.finishLoading(); //Important!

		andrewHappy = Gdx.audio.newSound(Gdx.files.internal("happy.mp3"));
	}

	@Override
	public void render () {


		//boiler plate code that is needed
		//first line wipes screen and sets color
		Gdx.gl.glClearColor(1,1,1,1);

		//sets it to a blank white screen
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();


		//note: 0,0 corresponds to the bottom left
		//increase speed as score increases


			if(misses<5) {

				drew_sprite.draw(batch);

				font.draw(batch, message, 300, 600);
				font.draw(batch, "Score = "+String.valueOf(score), screenWidth-400, screenHeight-80);
				font.draw(batch, "Level = "+String.valueOf(level), screenWidth-400, screenHeight-280);

				if(levelIndicator == 5){
					andrewHappy.play();
					levelIndicator = 0;
					//LEVEL UP difficulty with more bottles
					level++;
					speed = speed+2;
					//speed = speed + 10;
					mObject bottle;
					mObject nap;
					bottle = new mObject(bottleXPosition,bottleYPosition,true);
					bottles.add(bottle);
					nap = new mObject(napXPosition,napYPosition,false);
					naps.add(nap);
				}

				int numStuff = bottles.size();

				for(int i = 0; i < numStuff; i++) {

					bottles.get(i).draw(batch);
					naps.get(i).draw(batch);

					bottleYPosition = moveObject(bottles.get(i), speed);
					score = checkHit(rectangleDrew, bottles.get(i), score);
					misses = checkMiss(bottles.get(i), misses);

					napYPosition = moveObject(naps.get(i), speed);
					score = checkHit(rectangleDrew, naps.get(i), score);
					checkMiss(naps.get(i), misses);
				}

			}else{
				message = "Game over loser";
			}
		batch.end();

	}
	public void resetObject(mObject thisObject){
		Random r = new Random();
		int Low = 10;
		int High = screenWidth-50;
		int Result = r.nextInt(High-Low) + Low;

		thisObject.setX(Result);
		thisObject.setY(screenHeight);

		bottleYPosition = screenHeight;
		bottleXPosition = Result;
	}
	public int checkHit(Rectangle drew,mObject thisObject,int score){

		Rectangle thisRectangle = thisObject.rectangle();

		if(drew.overlaps(thisRectangle)){
			if(thisObject.AmIaBottle) {
				font.setColor(Color.GREEN);
				score++;
				levelIndicator++;
				resetObject(thisObject);
			}else{
				font.setColor(Color.RED);
				score--;
			}

		}
		return score;
	}
	public int checkMiss(mObject thisObject,int misses){

		Rectangle thisRectangle = thisObject.rectangle();

		if(thisRectangle.getY()<0.0){
			//so an object has gotten to the bottom and not been captured it is reset
			//if the object is a bottle it counts as a miss

			resetObject(thisObject);
			if(thisObject.getBottleStatus()) {
				misses++;
				font.setColor(Color.RED);
			}
		}
		return misses;
	}

	public float moveObject(mObject thisObject,float speed){

		float newY = thisObject.getY()-speed;
		thisObject.setY(thisObject.getY()-speed);
		return newY;
	}

	@Override
	public boolean touchDown(int i, int i1, int i2, int i3) {

		return true;
	}

	@Override
	public boolean touchUp(int i, int i1, int i2, int i3) {

		return true;
	}

	@Override
	public boolean touchDragged(int i, int i1, int i2) {

		float oldX = drew_sprite.getX();
		float newX = i-oldX;

		drew_sprite.translateX(newX);
		rectangleDrew.setPosition(drew_sprite.getX(),drew_sprite.getY());

		if(newX == screenWidth || newX == 0) {
			drew_sprite.flip(true, false);
		}
		return true;
	}
	@Override
	public boolean keyDown(int i) {
		return false;
	}
	@Override
	public boolean keyUp(int i) {
		return false;
	}

	@Override
	public boolean keyTyped(char c) {
		return false;
	}

	@Override
	public boolean mouseMoved(int i, int i1) {
		return false;
	}

	@Override
	public boolean scrolled(int i) {
		return false;
	}

	@Override
	public void dispose () {
		batch.dispose();
		andrew.dispose();
		int numStuff = bottles.size();

		for(int i = 0; i < numStuff; i++) {
			bottles.get(i).dispose();
			naps.get(i).dispose();
		}
		font.dispose();
	}
}
