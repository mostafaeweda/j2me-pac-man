# Game Components #
Game Components
  * Characters: There should be 5 moving characters in the game, Pac-Man and four ghosts.
  * Maze and pac-dots: The layout of the maze and all pac-dots should be the same as the screenshot illustrated below. The maze is filled with dots. The small dots are ordinary pac-dots, and the four big pills are power pellets that can temporarily enable Pac-Man to eat the ghosts.
  * Labels: On the top of the playfield, there are two labels indicating the current score and the remaining number of lives.

# Game Rules #

Most game rules of the original game can be found in http://en.wikipedia.org/wiki/Pac-Man. In this project, some of the original rules are eliminated or altered.
  * Basic gameplay: The player controls Pac-Man through a maze, eating pac-dots. Four ghosts roam the maze, trying to catch Pac-Man. If a ghost touches Pac-Man, a life is lost. The initial number of lives is three.
  * Power pellets: At the corners of the maze are four larger dots known as power pellets that
provide Pac-Man with the temporary ability to eat the ghosts. The ghosts turn pale, close
their eyes and reverse their direction when Pac-Man eats a power pellet. When a ghost is
eaten, it turns into a pair of eyes and after a few seconds it is regenerated in its normal
color. The effect of power pellet lasts at least 5 seconds but no more than 10 seconds.
  * Character behaviors: The ghosts keep moving in the same direction until they reach a cross where they can make a random choice, except when Pac-Man eats a power pellet; they all turn back immediately. Pac-Man moves at a constant speed. Player can use the arrow keys to control the Pac-Man's moving direction, not speed. If no key is pressed, the Pac-Man continues going forward until it meets a wall and then stops. If two ghosts meet, they keep on moving forward and cross each other. The ghosts turn back when they hit the wall.
  * You should show only part of the world not the whole maze i.e. the view window is smaller than the actual scene.
  * Change the shape of Pac-Man, and the ghosts while moving. Also notice that the shapes of ghosts and Pac-Man are changed during their death.
  * Scores: Each pac-dot is worth 10 pts, each power pellets worth 50 pts. When a power pellet is eaten, each ghost can be eaten giving a score of 200 pts.
  * Completing a level: When all pac-dots are eaten, the level is completed and a congratulation. message is displayed within the playfield.
  * Game over: When all lives have been lost, the game ends and a game over message is
shown in the playfield.