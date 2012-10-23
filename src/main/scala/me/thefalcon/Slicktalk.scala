package me.thefalcon.slicktrial

import scala.slick.driver.MySQLDriver
import MySQLDriver.simple._
import Database.{threadLocalSession => session}
import scala.slick.direct._
import scala.slick.direct.AnnotationMapper._


object Slicktalk extends App {

  val db = Database.forURL(
    "jdbc:mysql://localhost/baseball?user=baseball&password=playball", 
    driver = "com.mysql.jdbc.Driver")

  // Represents one player's batting statistics on one team in one year
  object Batting extends Table[(String, Int, String, Int)]("Batting") {
    def playerId = column[String]("playerID")
    def year     = column[Int]   ("yearID")
    def teamId   = column[String]("teamID")
    def homeRuns = column[Int]   ("HR")
    def *        = playerId ~ year ~ teamId ~ homeRuns
    def playerInfo = foreignKey("Master", playerId, Master){ _.playerId }
  }
  
  // Information for players, managers, etc
  object Master extends Table[(String, String, String)]("Master") {
    def playerId  = column[String]("playerID")
    def nameLast  = column[String]("nameLast")
    def nameFirst = column[String]("nameFirst")
    def *         = playerId ~ nameLast ~ nameFirst
  }
  
  def lifted = db withSession {

    //It's a Query, not an Iterable[(Int, String, String)]
    val q1 = for {
      b <- Batting if b.homeRuns >= 50
      //Foreign key lookup!
      i <- b.playerInfo
    } yield (b.year, i.nameLast, i.nameFirst)

    println(q1.selectStatement)

     //Work with Scala types inside the Query monad
     q1 sortBy { _._1 } foreach {
       case (year, last, first) =>
         println(year + ": " + first + " " + last)
     } 
    
    // ...or don't. 
    val returnFromMonad: List[(Int, String, String)] = q1.elements.toList

  }

  // Represents one player's batting statistics on one team in one year
  @table(name="Batting") // for macros!
  case class BattingDirect(
     @column(name="playerID") playerId: String,
     @column(name="yearID")   year:     Int,
     @column(name="teamID")   team:     String,
     @column(name="HR")       homeRuns: Int
  ) 
  
  // Information for players, managers, etc
  @table(name="Master")
  case class MasterDirect(
     @column(name="playerID")  playerId:  String,
     @column(name="nameLast")  nameLast:  Int,
     @column(name="nameFirst") nameFirst: String
  )

  def direct = db withSession {
    val backend = new SlickBackend(MySQLDriver, AnnotationMapper)
  
    val battingYears = Queryable[BattingDirect]
    // Macros!
      // > 49 because >= 50 will break at -runtime-!
    val q2 = battingYears filter { _.homeRuns > 49 } map 
      { b: BattingDirect => (b.year, b.playerId) } 

    //I get the sense the call on `backend` is a concession.
    //In an earlier talk the backend could be applied to the Queryable. 
    backend.result(q2, session) sortBy { _._1 } foreach {
      case (year, playerId) => println(year + ": " + playerId)
    }

  }

  lifted
  println("\n") 
  direct

}
