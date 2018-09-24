package com.maxwellhealth

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn
import scala.concurrent.Future


object WebServer {

  // needed to run service
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  // in-mem products...
  var products: List[Item] = Nil

  // domain model
  final case class Item(id: Long, name: String, price: Double, description: String = "")

  // formats for unmarshalling and marshalling
  implicit val itemFormat = jsonFormat4(Item)

  def listProducts(): Future[List[Item]] = Future {
    products
  }

  def addProduct(product: Item): Future[Done] = {
    products = product match {
      case Item(id, name, price, description) => product :: products
      case _ => products
    }

    Future { Done }
  }

  def getProduct(productId: Long): Future[Option[Item]] = Future {
    products.find(p => p.id == productId)
  }

  def updateProduct(product: Item): Future[Done] = {
    products = products.filterNot(p => p.id == product.id) :::
      products.filter(p => p.id == product.id).map(p => product)

    Future { Done }
  }

  def removeProduct(productId: Long): Future[Done] = {
    products = products.filterNot(p => p.id == productId)

    Future { Done }
  }



  def main(args: Array[String]): Unit = {

    val route: Route =
      get { // get all products
        path("products") {
          val promise: Future[List[Item]] = listProducts()
          onComplete(promise) { p => complete(products) }
        }
      } ~
      post { // create a new product
        path("products") {
          entity(as[Item]) { item =>
            val promise: Future[Done] = addProduct(item)
            onComplete(promise) { done =>
              complete("Product Added")
            }
          }
        }
      } ~
      get { // get a single product
        pathPrefix("products" / LongNumber) { id =>
          val promise: Future[Option[Item]] = getProduct(id)

          onSuccess(promise) {
            case Some(item) => complete(item)
            case None => complete(StatusCodes.NotFound)
          }
        }
      } ~
      put { // update a product
        pathPrefix("products" / LongNumber) { _ =>
          entity(as[Item]) { item =>
            val promise: Future[Done] = updateProduct(item)
            onComplete(promise) { done =>
              complete("Product Updated")
            }
          }
        }
      } ~
      delete { // remove a product
        pathPrefix("products" / LongNumber) { id =>
          val promise: Future[Done] = removeProduct(id)
          onComplete(promise) { done =>
            complete("Product Removed")
          }
        }
      }


    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
