import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.spbpu.spaceshooter.EnemyShip;
import com.spbpu.spaceshooter.Laser;
import com.spbpu.spaceshooter.PlayerShip;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Tests {


    private TextureAtlas textureAtlas = new TextureAtlas("images.atlas");
    private TextureRegion playerLaserTextureRegion = textureAtlas.findRegion("laserGreen08");
    private TextureRegion playerShipTextureRegion = textureAtlas.findRegion("playerShip2_green");
    private TextureRegion playerShieldTextureRegion = textureAtlas.findRegion("shield2");


    Laser laser = new Laser(20, 20, 5,5, 40, playerLaserTextureRegion);

    PlayerShip playerShip = new PlayerShip(40, 30,
                10, 10, 40, 3,
                        0.4f, 4, 45, 0.5f,
                                playerShipTextureRegion, playerShieldTextureRegion, playerLaserTextureRegion);
    EnemyShip enemyShip = new EnemyShip(40, 30,
            10, 10, 40, 3,
            0.4f, 4, 45, 0.5f,
            playerShipTextureRegion, playerShieldTextureRegion, playerLaserTextureRegion);

    @Test
    public void getX() {
        assertEquals(35, playerShip.getBoundingBox().x);
    }

    @Test
    public void getY() {
        assertEquals(25, playerShip.getBoundingBox().y);
    }

    @Test
    public void getWidth() {
        assertEquals(10, playerShip.getBoundingBox().width);
    }

    @Test
    public void getHeight() {
        assertEquals(10, playerShip.getBoundingBox().height);
    }

    @Test
    public void getShield() {
        assertEquals(3, playerShip.getShield());
    }

    @Test
    public void getMovement() {
        assertEquals(40, playerShip.getMovementSpeed());
    }

    @Test
    public void getLaserWidth() {
        assertEquals(0.4f, playerShip.getLaserWidth());
    }

    @Test
    public void getLaserHeight() {
        assertEquals(4, playerShip.getLaserHeight());
    }
    @Test
    public void getLaserMovement() {
        assertEquals(45, playerShip.getLaserMovementSpeed());
    }

    @Test
    public void getTimeBetweenShots() {
        assertEquals(0.5f, playerShip.getTimeBetweenShots());
    }

    @Test
    public void getTimeSinceLastShot() {
        assertEquals(0, playerShip.getTimeSinceLastShot());
    }

    @Test
    public void getAndSetLives() {
        assertEquals(3, playerShip.getLives());
        playerShip.setLives(2);
        assertEquals(2, playerShip.getLives());
    }

    @Test
    public void update() {
        playerShip.update(0.5f);
        assertEquals(0.5f, playerShip.getTimeSinceLastShot());
    }

    @Test
    public void fire() {
        playerShip.fireLasers();
        assertEquals(0, playerShip.getTimeSinceLastShot());
    }

    @Test
    public void hit() {
        playerShip.setShield(1);
        assertFalse(playerShip.hitAndCheckDestroyed(laser));
        assertTrue(playerShip.hitAndCheckDestroyed(laser));
    }

    @Test
    public void translate() {
        playerShip.translate(30, 30);
        assertEquals(65, playerShip.getBoundingBox().x);
        assertEquals(55, playerShip.getBoundingBox().y);
    }

    @Test
    public void getBoundingBoxLaser() {
        Rectangle boundingBox = new Rectangle(17.5f, 20, 5, 5);
        assertEquals(boundingBox, laser.getBoundingBox());
    }
    Vector2 directionVector = new Vector2(0,-1);
    @Test
    public void getVector() {
        assertEquals(directionVector, enemyShip.getDirectionVector());
    }

    @Test
    public void randomize() {
        enemyShip.update(0.5f);
        assertNotEquals(directionVector, enemyShip.getDirectionVector());
    }


}
