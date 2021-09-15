package com.spbpu.spaceshooter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

class GameScreen implements Screen {

    //Screen
    private Camera camera;
    private Viewport viewport;
    private Game game;

    //graphics
    private SpriteBatch batch;
    private TextureAtlas textureAtlas;
    private TextureRegion background, playerShipTextureRegion, enemyShipTextureRegion,
            playerLaserTextureRegion, enemyLaserTextureRegion,
    mediumMeteorTextureRegion, bigMeteorTextureRegion, enemyShieldTextureRegion, playerShieldTextureRegion;
    private Texture explosionTexture;
    private Image playAgain;


    //World parameters
    private final float WORLD_WIDTH = 72;
    private final float WORLD_HEIGHT = 128;
    private final float TOUCH_MOVEMENT_THRESHOLD = 0.5f;
    private float timeBetweenEnemySpawns = 3f;
    private float enemySpawnTimer = 0;
    private boolean gameOver = false;
    private float timeSinceDeath = 0;

    //game objects
    private PlayerShip playerShip;
    private LinkedList<EnemyShip> enemyShipList;
    private LinkedList<Laser> playerLaserList;
    private LinkedList<Laser> enemyLaserList;
    private LinkedList<Explosion> explosionList;

    private int score = 0;

    //font
    BitmapFont font;
    BitmapFont gameOverFont;
    float hudVerticalMargin, hudLeftX, hudRightX, hudCentreX, hudRow1Y, hudRow2Y, hudSectionWidth;
    float gameOverX, gameOverY, gameOverSectionWidth;

    GameScreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT,  camera);
        textureAtlas = new TextureAtlas("images.atlas");
        background = textureAtlas.findRegion("darkPurple");
        playerShipTextureRegion = textureAtlas.findRegion("playerShip2_green");
        enemyShipTextureRegion = textureAtlas.findRegion("enemyRed1");
        playerLaserTextureRegion = textureAtlas.findRegion("laserGreen08");
        enemyLaserTextureRegion = textureAtlas.findRegion("laserRed03");
        mediumMeteorTextureRegion = textureAtlas.findRegion("playerShip2_green");
        bigMeteorTextureRegion = textureAtlas.findRegion("playerShip2_green");
        enemyShieldTextureRegion = textureAtlas.findRegion("shield3");
        enemyShieldTextureRegion.flip(false, true);
        playerShieldTextureRegion = textureAtlas.findRegion("shield2");

        explosionTexture = new Texture("explosion.png");

        playerShip = new PlayerShip(WORLD_WIDTH/2, WORLD_HEIGHT/4,
                10, 10, 40, 3,
                0.4f, 4, 45, 0.5f,
                playerShipTextureRegion, playerShieldTextureRegion, playerLaserTextureRegion);
        enemyShipList = new LinkedList<>();


        playerLaserList = new LinkedList<>();
        enemyLaserList = new LinkedList<>();

        explosionList = new LinkedList<>();

        batch = new SpriteBatch();

        prepareHUD();
        prepareGameOver();
    }


    public void prepareHUD() {
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("EdgeOfTheGalaxyRegular-OVEa6.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        fontParameter.size = 72;
        fontParameter.borderWidth = 3.6f;
        fontParameter.color = new Color(1,1,1, 0.3f);
        fontParameter.borderColor = new Color(0,0,0,0.3f);

        font = fontGenerator.generateFont(fontParameter);

        font.getData().setScale(0.08f);

        hudVerticalMargin = font.getCapHeight() / 2;
        hudLeftX = hudVerticalMargin;
        hudRightX = WORLD_WIDTH * 2 / 3 - hudLeftX;
        hudCentreX = WORLD_WIDTH / 3;
        hudRow1Y = WORLD_HEIGHT - hudVerticalMargin;
        hudRow2Y = hudRow1Y - hudVerticalMargin - font.getCapHeight();
        hudSectionWidth = WORLD_WIDTH / 3;
    }

    public void prepareGameOver() {
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("EdgeOfTheGalaxyRegular-OVEa6.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = 110;
        fontParameter.borderWidth = 6f;
        fontParameter.color = new Color(1,1,1, 0.8f);
        fontParameter.borderColor = new Color(0,0,0,0.8f);

        gameOverFont = fontGenerator.generateFont(fontParameter);
        gameOverFont.getData().setScale(0.11f);

        gameOverX = WORLD_WIDTH * 1.3f / 4;
        gameOverY = WORLD_HEIGHT * 2 / 3;
        gameOverSectionWidth = WORLD_WIDTH / 3;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float deltaTime) {
        batch.begin();
        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        if (!gameOver) {
            detectInput(deltaTime);

            playerShip.update(deltaTime);
            playerShip.draw(batch);

            spawnEnemyShips(deltaTime);

            ListIterator<EnemyShip> enemyShipListIterator = enemyShipList.listIterator();
            while (enemyShipListIterator.hasNext()){
                EnemyShip enemyShip = enemyShipListIterator.next();
                moveEnemy(enemyShip, deltaTime);
                enemyShip.update(deltaTime);
                enemyShip.draw(batch);
            }

            renderExplosions(deltaTime);

            renderLasers(deltaTime);

            detectCollisions();

            updateAndRenderHUD();
        }
        else {
            renderGameOver(deltaTime);
        }
        batch.end();
    }

    private void renderGameOver(float deltaTime) {
        timeSinceDeath += deltaTime;
        if (timeSinceDeath <8f) {
            gameOverFont.draw(batch, "Game Over", gameOverX, gameOverY, gameOverSectionWidth, Align.center, false);
            ListIterator<EnemyShip> enemyShipListIterator = enemyShipList.listIterator();
            while (enemyShipListIterator.hasNext()) {
                enemyShipListIterator.next();
                enemyShipListIterator.remove();
            }
        }
        else {
            playerShip.boundingBox.x = WORLD_WIDTH/2;
            playerShip.boundingBox.y = WORLD_HEIGHT/4;
            playerShip.lives = 3;
            playerShip.shield = 3;
            score = 0;
            gameOver = false;
            timeSinceDeath = 0;
        }
    }

    private void updateAndRenderHUD() {
        font.draw(batch, "Score", hudLeftX, hudRow1Y, hudSectionWidth, Align.left, false);
        font.draw(batch, "Shield", hudCentreX, hudRow1Y, hudSectionWidth, Align.center, false);
        font.draw(batch, "Lives", hudRightX, hudRow1Y, hudSectionWidth, Align.right, false);

        font.draw(batch, String.format(Locale.getDefault(), "%06d", score),
                hudLeftX, hudRow2Y, hudSectionWidth, Align.left, false);
        font.draw(batch, String.format(Locale.getDefault(), "%02d", playerShip.shield),
                hudCentreX, hudRow2Y, hudSectionWidth, Align.center, false);
        font.draw(batch, String.format(Locale.getDefault(), "%02d", playerShip.lives),
                hudRightX, hudRow2Y, hudSectionWidth, Align.right, false);
    }

    private void spawnEnemyShips(float deltaTime) {
        enemySpawnTimer += deltaTime;

        if (enemySpawnTimer > timeBetweenEnemySpawns) {
            enemyShipList.add(new EnemyShip(SpaceShooterGame.random.nextFloat() * WORLD_WIDTH,
                    WORLD_HEIGHT - 10,
                    10, 10, 15, 1,
                    0.3f, 5, 55, 0.8f,
                    enemyShipTextureRegion, enemyShieldTextureRegion, enemyLaserTextureRegion));
            enemySpawnTimer -= timeBetweenEnemySpawns;
        }
    }

    private void detectInput(float deltaTime) {
        float leftLimit, rightLimit, upLimit, downLimit;
        leftLimit = -playerShip.boundingBox.x;
        downLimit = -playerShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - playerShip.boundingBox.x - playerShip.boundingBox.width;
        upLimit = WORLD_HEIGHT /2 - playerShip.boundingBox.y - playerShip.boundingBox.height;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && rightLimit > 0) {
            playerShip.translate(Math.min(playerShip.movementSpeed * deltaTime, rightLimit), 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) && upLimit > 0) {
            playerShip.translate(0f, Math.min(playerShip.movementSpeed * deltaTime, upLimit));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && leftLimit < 0) {
            playerShip.translate(Math.max(-playerShip.movementSpeed * deltaTime, leftLimit),0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && downLimit < 0) {
            playerShip.translate(0f, Math.max(-playerShip.movementSpeed * deltaTime, downLimit));
        }

        if (Gdx.input.isTouched()) {
            float xTouchPixels = Gdx.input.getX();
            float yTouchPixels = Gdx.input.getY();

            Vector2 touchPoint = new Vector2(xTouchPixels, yTouchPixels);
            touchPoint = viewport.unproject(touchPoint);

            Vector2 playerShipCentre = new Vector2(
                    playerShip.boundingBox.x + playerShip.boundingBox.width / 2,
                    playerShip.boundingBox.y + playerShip.boundingBox.height /2
            );
            float touchDistance = touchPoint.dst(playerShipCentre);

            if (touchDistance > TOUCH_MOVEMENT_THRESHOLD) {
                float xTouchDifference  = touchPoint.x - playerShipCentre.x;
                float yTouchDifference  = touchPoint.y - playerShipCentre.y;

                float xMove = xTouchDifference / touchDistance * playerShip.movementSpeed * deltaTime;
                float yMove = yTouchDifference / touchDistance * playerShip.movementSpeed * deltaTime;

                if (xMove > 0) xMove = Math.min(xMove, rightLimit);
                else xMove = Math.max(xMove, leftLimit);
                if (yMove > 0) yMove = Math.min(yMove, upLimit);
                else yMove = Math.max(yMove, downLimit);

                playerShip.translate(xMove, yMove);
            }
        }
    }

    private void moveEnemy(EnemyShip enemyShip, float deltaTime) {
        float leftLimit, rightLimit, upLimit, downLimit;
        leftLimit = -enemyShip.boundingBox.x;
        downLimit = WORLD_HEIGHT *3/4 - enemyShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - enemyShip.boundingBox.x - enemyShip.boundingBox.width;
        upLimit = WORLD_HEIGHT - enemyShip.boundingBox.y - enemyShip.boundingBox.height;

        float xMove = enemyShip.getDirectionVector().x * enemyShip.movementSpeed * deltaTime;
        float yMove = enemyShip.getDirectionVector().y * enemyShip.movementSpeed * deltaTime;

        if (xMove > 0) xMove = Math.min(xMove, rightLimit);
        else xMove = Math.max(xMove, leftLimit);
        if (yMove > 0) yMove = Math.min(yMove, upLimit);
        else yMove = Math.max(yMove, downLimit);

        enemyShip.translate(xMove, yMove);
    }

    private void detectCollisions() {
        ListIterator<Laser> laserListIterator = playerLaserList.listIterator();
        while (laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            ListIterator<EnemyShip> enemyShipListIterator = enemyShipList.listIterator();
            while (enemyShipListIterator.hasNext()) {
                EnemyShip enemyShip = enemyShipListIterator.next();
                if (enemyShip.intersects(laser.getBoundingBox())) {
                    if (enemyShip.hitAndCheckDestroyed(laser)) {
                        enemyShipListIterator.remove();
                        explosionList.add(new Explosion(explosionTexture,
                                new Rectangle(enemyShip.boundingBox), 0.7f));
                        score += 100;
                    }
                    laserListIterator.remove();
                    break;
                }
            }
        }

        laserListIterator = enemyLaserList.listIterator();
        while (laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            if (playerShip.intersects(laser.getBoundingBox())) {
                if (playerShip.hitAndCheckDestroyed(laser)) {
                    explosionList.add(new Explosion(explosionTexture,
                            new Rectangle(playerShip.boundingBox), 1.6f));
                    playerShip.lives--;
                    if (playerShip.lives == 0) {
                        gameOver = true;
                    }
                }
                laserListIterator.remove();
            }
        }
    }

    private void renderExplosions(float deltaTime) {
        ListIterator<Explosion> explosionListIterator = explosionList.listIterator();
        while (explosionListIterator.hasNext()) {
            Explosion explosion = explosionListIterator.next();
            explosion.update(deltaTime);
            if (explosion.isFinished()) {
                explosionListIterator.remove();
            }
            else {
                explosion.draw(batch);
            }
        }
    }

    private void renderLasers(float deltaTime) {
        if (playerShip.canFireLaser()) {
            Laser[] lasers = playerShip.fireLasers();
            playerLaserList.addAll(Arrays.asList(lasers));
        }
        ListIterator<EnemyShip> enemyShipListIterator = enemyShipList.listIterator();
        while (enemyShipListIterator.hasNext()) {
            EnemyShip enemyShip = enemyShipListIterator.next();
            if (enemyShip.canFireLaser()) {
                Laser[] lasers = enemyShip.fireLasers();
                enemyLaserList.addAll(Arrays.asList(lasers));
            }
        }


        ListIterator<Laser> iterator = playerLaserList.listIterator();
        while (iterator.hasNext()){
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y += laser.movementSpeed*deltaTime;
            if (laser.boundingBox.y > WORLD_HEIGHT) {
                iterator.remove();
            }
        }
        iterator = enemyLaserList.listIterator();
        while (iterator.hasNext()){
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y -= laser.movementSpeed*deltaTime;
            if (laser.boundingBox.y + laser.boundingBox.height < 0) {
                iterator.remove();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(camera.combined);

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
    }
}
