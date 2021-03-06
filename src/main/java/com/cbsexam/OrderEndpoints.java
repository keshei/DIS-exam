package com.cbsexam;

import cache.OrderCache;
import com.google.gson.Gson;
import controllers.OrderController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.Order;
import utils.Encryption;

@Path("order")
public class OrderEndpoints {
  // Call our controller-layer in order to get the right methodes
  public static OrderCache orderCache = new OrderCache();

  /**
   * @Param idOrder
   * @return Responses
   */

  @GET
  @Path("/{idOrder}")
  public Response getOrder (@PathParam ("idOrder") int idOrder) {

    // Call our controller-layer in order to get the order from the DB
    Order order = OrderController.getOrder(idOrder);

    // TODO: Add Encryption to JSON: FIXED
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(order);

    // encryption to json rwaString object(ret. utils Encryption)
    json = Encryption.encryptDecryptXOR(json);

    // Return a response with status 200 and JSON as type
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();

  }

  /** @return Responsens */
  @GET
  @Path ("/")
  public Response getOrders() {

    ArrayList<Order> orders = orderCache.getOrders(true);

    // TODO: Add Encryption to JSON, FIXED
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(orders);

    //add encryption to json rawString object(ref.utils Encryption)
    json = Encryption.encryptDecryptXOR(json);

    // Return a response with status 200 and JSON as type
    return Response.status(200).type(MediaType.TEXT_PLAIN_TYPE).entity(json).build();
  }

  @POST
  @Path("/create")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createOrder(String body) {

    // Read the json from body and transfer it to a order class
    Order newOrder = new Gson().fromJson(body, Order.class);

    // Use the controller to add the order
    Order createdOrder = OrderController.createOrder(newOrder);

    // Get the order back with the added ID and return it to the user
    String json = new Gson().toJson(createdOrder);

    // Return the data to the user
    if (createdOrder != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {

      // Return a response with status 400 and a message in text
      return Response.status(400).entity("Could not create order").build();
    }
  }
}