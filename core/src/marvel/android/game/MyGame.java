package marvel.android.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class MyGame extends ApplicationAdapter {
	OrthographicCamera camera;
	SpriteBatch batch;
	Texture dropImg;
	Texture bucketImg;
	Sound dropSound;
	Music rainMusic;
	Rectangle bucket;
	Vector3 touchPos;
	Array<Rectangle> raindrops;
	long lastDropTime;
	
	@Override
	public void create () {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();
		dropImg = new Texture("droplet.png");
		bucketImg = new Texture("bucket.png");

		dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));

		rainMusic.setLooping(true);
		rainMusic.play();

		bucket = new Rectangle();
		bucket.width = 64;
		bucket.height = 64;
		bucket.x = 800/2 - bucket.width/2;
		bucket.y = 20;

		raindrops = new Array<Rectangle>();
		spawnRaindrop();
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.width = 64;
		raindrop.height = 64;
		raindrop.x = MathUtils.random(0, 800 - raindrop.width);
		raindrop.y = 480;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(bucketImg, bucket.x, bucket.y);
		for (Rectangle raindrop : raindrops) {
			batch.draw(dropImg, raindrop.x, raindrop.y);
		}
		batch.end();

		if (Gdx.input.isTouched()) {
			touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			float v = (touchPos.x - (bucket.x+bucket.width/2))*4;
			if (v > 200) v = 200;
			if (v < -200) v = -200;
			bucket.x += v * Gdx.graphics.getDeltaTime();
		}
		else {
			if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
			if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();
		}


		if (bucket.x < 0) bucket.x = 0;
		if (bucket.x > 800 - bucket.width) bucket.x = 800 - bucket.width;

		if (TimeUtils.nanoTime() - lastDropTime >= 1000000000) {
			spawnRaindrop();
		}

		Iterator<Rectangle> iter = raindrops.iterator();
		while (iter.hasNext()) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y+raindrop.height < 0) {
				iter.remove();
			}
			if (raindrop.overlaps(bucket)) {
				if (raindrop.y+raindrop.height/2 > bucket.height) {
					dropSound.play();
					iter.remove();
				}
			}
		}
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		bucketImg.dispose();
		dropImg.dispose();

		dropSound.dispose();
		rainMusic.dispose();

		batch.dispose();
	}
}
