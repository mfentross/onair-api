package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{Concurrent, Iteratee}
import play.api.libs.concurrent.Execution.Implicits._
//import akka.actor.IO.Iteratee

object Application extends Controller {


//  val videoIt: Iteratee[Array[Byte]]
  lazy val (out,channel) = Concurrent.broadcast[Array[Byte]]
  lazy val (clientOut, clientChannel) = Concurrent.broadcast[Array[Byte]]

  def index = Action {
    Ok("yeah, its onair")
  }

  def receive = WebSocket.using[Array[Byte]] { implicit request =>
    val in = Iteratee.foreach[Array[Byte]] { ar =>
      channel push ar
      clientChannel push ar
    }
//    val out = Enumerator(Array(20.toByte, 10.toByte))

    (in, out)
  }

  def send = WebSocket.using[Array[Byte]] { implicit request =>
    val in = Iteratee.consume[Array[Byte]]()

    (in, clientOut)
  }

  def view = Action {
    Ok(views.html.view())
  }

  def broadcast = Action {
    Ok(views.html.broadcast())
  }


  ///////########### TEXT ONLY #########################
  lazy val (tout,tchannel) = Concurrent.broadcast[String]
  lazy val (cout,cchannel) = Concurrent.broadcast[String]


  def pinger = WebSocket.using[String] { implicit request =>
    val in = Iteratee.foreach[String] { ar =>
      tchannel push ar
      cchannel push ar
    }

    (in, tout)
  }

  def pingSender = WebSocket.using[String] { implicit request =>
    val in = Iteratee.consume[String]()

    (in, cout)
  }


  def viewText = Action {
    Ok(views.html.viewText())
  }

  def viewTextSender = Action {
    Ok(views.html.client())
  }

}