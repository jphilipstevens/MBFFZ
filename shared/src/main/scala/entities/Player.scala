package entities

import gamestate.GameState
import physics.Complex
import physics.quadtree.ShapeQT
import physics.shape.{BoundingBox, Circle}
import utils.Colour

import scala.util.Random

final class Player(
                    val id: Long,
                    val time: Long,
                    val pos: Complex,
                    val direction: Double,
                    val moving: Boolean,
                    val colour: String
                  ) extends MovingBody {

  val shape: Circle = Player.shape

  val speed: Double = Player.playerSpeed

  val rotation: Double = 0

}

object Player {

  val playerRadius: Double = 5
  val playerSpeed: Double = 100

  val shape: Circle = new Circle(playerRadius)

  import Complex.i
  private val zero: Complex = 0

  sealed trait Direction {
    val z: Complex
    val keys: List[String]
  }
  case object Up extends Direction {
    val z: Complex = i
    val keys: List[String] = List("z", "ArrowUp")
  }
  case object Down extends Direction {
    val z: Complex = -i
    val keys: List[String] = List("s", "ArrowDown")
  }
  case object Right extends Direction {
    val z: Complex = 1
    val keys: List[String] = List("d", "ArrowRight")
  }
  case object Left extends Direction {
    val z: Complex = -1
    val keys: List[String] = List("q", "ArrowLeft")
  }

  private def findDirection(directions: List[Direction]): (Double, Boolean) = {
    val towards = directions.map(_.z).sum
    (towards.arg, towards != zero)
  }

  def updatePlayer(
                    time: Long,
                    player: Player,
                    directions: List[Direction],
                    quadTree: ShapeQT,
                    worldBox: BoundingBox
                  ): Player = {
    val (direction, moving) = findDirection(directions)

    if (moving) {
      val newPos = player.lastValidPos(time - player.time, quadTree, direction)

      val x = if (newPos.re > worldBox.right) worldBox.right
      else if (newPos.re < worldBox.left) worldBox.left
      else newPos.re
      val y = if (newPos.im > worldBox.top) worldBox.top
      else if (newPos.im < worldBox.bottom) worldBox.bottom
      else newPos.im

      new Player(player.id, time, Complex(x, y), direction, moving, player.colour)
    } else new Player(player.id, time, player.pos, direction, moving, player.colour)
  }

  def startingPlayer(time: Long, quadTree: ShapeQT, colour: String): Player = {

    def findPosition(): Complex = {
      val w = GameState.worldBox.width - playerRadius
      val h = GameState.worldBox.height - playerRadius
      val tryNext = Complex(
        Random.nextDouble() * w - w / 2, Random.nextDouble() * h - h / 2
      )

      if (quadTree.collides(shape, tryNext, 0)) findPosition()
      else tryNext
    }

    new Player(Entity.newId(), time, findPosition(), 0, moving = false, colour = colour)

  }

  val playerColours: Map[String, String] = Map(
    "Red" -> Colour(255, 0, 0),
    "Green" -> Colour(0, 255, 0),
    "Blue" -> Colour(0, 0, 255),
    "Yellow" -> Colour(255, 255, 0),
    "Fuchsia" -> Colour(255, 0, 255),
    "Orange" -> Colour(255, 153, 0),
    "Aqua" -> Colour(0, 255, 255),
    "Light Blue" -> Colour(51, 153, 255)
  ).mapValues(_.cssString)

  @inline def cssColour(colourName: String): String = playerColours(colourName)

}
