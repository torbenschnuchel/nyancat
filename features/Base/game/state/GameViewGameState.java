package Base.game.state;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

import Base.TWLSlick.BasicTWLGameState;
import Base.game.Constants;
import Base.game.DataBaseManager;
import Base.game.InputManager;
import Base.game.KeyboardHandler;
import Base.game.ResourceManager;
import Base.level.Level;
import Base.logic.Cat;
/*if[JazzMode]*/
import Base.logic.CatListener.CatMode;
/*end[JazzMode]*/
import Base.logic.Difficulty;
import Base.logic.Note;
import Base.logic.Obstacle;
/* if[Hintergrundmusik] */
import Base.music.MusicManager;
/* else[Soundeffekte] */
import Base.music.MusicManager;
/*end[Soundeffekte]*/

public class GameViewGameState extends BasicTWLGameState implements
		KeyboardHandler {

	private int id;
	/* if[Hintergrundmusik] */
	private MusicManager musicManager;
	/* else[Soundeffekte] */
	private MusicManager musicManager;
	/*end[Soundeffekte]*/
	private InputManager inputManager;
	private Cat cat;
	private int score;
	private Level level;
	private int bgCounter = 1000;
	private boolean paused = false;

	/* if[JazzMode] */
	private int murphyCounter = 0;
	private boolean rotate = false;
	/* end[JazzMode] */

	/* if[Pause_Hilfe] */
	/* if_not[Noten_Schiessen] */
	private static final int SMALLHELPPICTURE_X = 1440;
	private static final int SMALLHELPPICTURE_Y = 1060;
	/*end[Noten_Schiessen]*/
	/* end[Pause_Hilfe] */

	public GameViewGameState(int uniqueID) {

		this.id = uniqueID;
	}

	private void initialize(GameContainer container, StateBasedGame game) {

		cat = new Cat();
		score = 0;
		level = new Level(cat);

		/* if[Hintergrundmusik] */
		musicManager = MusicManager.getDefaultMusicManager();
		/*else[Soundeffekte]*/
		musicManager = MusicManager.getDefaultMusicManager();
		/* end[Soundeffekte] */

		inputManager = new InputManager(container, game);

		int[] catKeys = { Input.KEY_UP, Input.KEY_DOWN, Input.KEY_LEFT,
				Input.KEY_RIGHT, Input.KEY_SPACE };
		inputManager.register(cat, catKeys);

		inputManager.register(this, Input.KEY_F);
		inputManager.register(this, Input.KEY_P);
		inputManager.register(this, Input.KEY_SPACE);
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {

		container.setMusicVolume(0.1f);
		ResourceManager.getDefaultManager().loadAll();

		container
				.setMinimumLogicUpdateInterval(Constants.GAME_UPDATE_INTERVAL_MIN);
		container
				.setMaximumLogicUpdateInterval(Constants.GAME_UPDATE_INTERVAL_MAX);
		container.setVSync(true);
		container.setTargetFrameRate(60);

		this.initialize(container, game);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {

		if (!paused) {

			if (!level.isGameOver()) {

				updateCat(delta);
				updateLevel(delta);
				updateScore(delta);
				updateBackgroundCounter(delta);
				/* if[JazzMode] */

				// update Murphy picture during Jazz mode
				updateMurphy(delta);

				/* end[JazzMode] */
				checkCollisions();

			} else {
				/*if[Highscore]*/
				DataBaseManager.getDefaultManager().uploadScore(score);
				/*end[Highscore]*/
				GameOverGameState gameOverState = (GameOverGameState) game
						.getState(Constants.ID_GAMEOVER);
				gameOverState.setScore(score);

				FadeOutTransition leave = new FadeOutTransition(Color.black,
						100);
				FadeInTransition enter = new FadeInTransition(Color.black, 100);
				game.enterState(Constants.ID_GAMEOVER, leave, enter);
			}
		}
	}

	/* if[JazzMode] */
    private void updateMurphy(int delta) {

            murphyCounter -= delta;

            if (cat.getMode() == CatMode.JAZZ) {

                    if (murphyCounter <= 0) {
                            rotate = !rotate;
                            murphyCounter = 1000;
                    }
            }
    }

    /* end[JazzMode] */

	private void updateBackgroundCounter(int delta) {

		bgCounter -= delta;
		if (bgCounter <= 0) {

			bgCounter += 1000;
		}
	}

	private void checkCollisions() {

		level.getCollisionGraph().checkCollisions();
	}

	private void updateLevel(int delta) {

		level.update(delta);
	}

	private void updateCat(int delta) {

		cat.updatePosition(delta);
	}

	private void updateScore(int delta) {

		score += delta;
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {

		// draw background
		Image bgImage = (Image) ResourceManager.getDefaultManager()
				.getResourceNamed(ResourceManager.BACKGROUND);
		// TODO wtf -28 ??? to get to 100? image is 128 by 128
		float offset = (bgImage.getWidth() - 28) * bgCounter / 1000f;
		g.translate(+offset, 0);
		g.texture(new Rectangle(-offset, 0, Constants.SCREEN_WIDTH,
				Constants.SCREEN_HEIGHT), bgImage);
		g.translate(-offset, 0);

		/* if[JazzMode] */
        // draw murphy
        if (cat.getMode() == CatMode.JAZZ) {

                Image murphyImage = (Image) ResourceManager.getDefaultManager()
                                .getResourceNamed(ResourceManager.MURPHY);
                float angle = 0;
                if (rotate) {

                        angle = 10;
                }
                murphyImage.rotate(angle);
                g.drawImage(murphyImage, 200, 200);
                murphyImage.rotate(-angle);

        }
        /* end[JazzMode] */
		
		

		// draw obstacles
		g.setColor(Color.white);
		for (Obstacle obstacle : level.getObstacles()) {

			obstacle.draw(g);
		}

		// drawCat
		cat.draw(g);

		// drawScore
		g.setColor(Color.white);
		String scoreText = new Integer(score).toString();
		g.drawString(scoreText, Constants.SCREEN_WIDTH - 70, 30);

		// draw help
		/* if[Pause_Hilfe] */
        if (paused) {

                // help if cat fire is avtivated
                /* if[Noten_schiessen] */
                 Image helpImage = (Image)
                ResourceManager.getDefaultManager().getResourceNamed(ResourceManager.PAUSE);

                // help if cat fire is deactivated

                /* else[Noten_schiessen] */
                Image helpImage = ((Image) ResourceManager.getDefaultManager()
                                .getResourceNamed(ResourceManager.PAUSE)).getSubImage(0, 0,
                                SMALLHELPPICTURE_X, SMALLHELPPICTURE_Y);
                /* end[Noten_schiessen] */

                float xMargin = 200;

                float width = Constants.SCREEN_WIDTH - 2 * xMargin;

                float height = width * helpImage.getHeight()
                                / (float) (helpImage.getWidth());

                float yMargin = (Constants.SCREEN_HEIGHT - height) / 2f;

                System.out.println("yMargin: " + yMargin);
                g.drawImage(helpImage, xMargin, yMargin, Constants.SCREEN_WIDTH
                                - xMargin, Constants.SCREEN_HEIGHT - yMargin, 0, 0,
                                helpImage.getWidth(), helpImage.getHeight());

        }
        /* end[Pause_Hilfe] */
	}

	@Override
	public void keyPressed(int key, char c) {

		inputManager.processKey(key, false);
		super.keyPressed(key, c);
	}

	@Override
	public void keyReleased(int key, char c) {

		inputManager.processKey(key, true);
		super.keyReleased(key, c);
	}

	@Override
	public int getID() {

		return id;
	}

	@Override
	public void handle(GameContainer container, StateBasedGame game, int key,
			boolean released) {

		switch (key) {
		case Input.KEY_F:

			if (released) {

				try {
					container.setFullscreen(!container.isFullscreen());
				} catch (SlickException e) {

					e.printStackTrace();
				}
			}

			break;

		case Input.KEY_P:

			if (released) {

				paused = !paused;
			}

			break;

		default:
			break;
		}
	}


	/*if[Schwierigkeitsgrade]*/
	public Difficulty getDifficulty(){
		return this.level.getDifficulty();
	}
	public void toggleDifficulty() {
		this.level.toggleDifficulty();
	}
	/*end[Schwierigkeitsgrade]*/
	
	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
//		MusicManager.getDefaultMusicManager().loadSoundEffect("OGG", "/res/sound/nyan_loop_original.ogg");
//		MusicManager.getDefaultMusicManager().playMusic("/res/sound/nyan_loop_original.ogg");
		/*if[Hintergrundmusik]*/
		this.musicManager.playOriginal();
		/*end[Hintergrundmusik]*/
		
		
		
		super.enter(container, game);
	}

}
