package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import org.junit.runners.Parameterized;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the controller.
    User user = UserController.getUser(idUser);

    // TODO: Add Encryption to JSON,   FIX
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);

    //add encryption to json rawString object(ref.utils Encryption)
    json = Encryption.encryptDecryptXOR(json);

    // TODO: What should happen if something breaks down? FIX
    //Return data to user
    if (user != null ) {
      // Return the user with the status code 200, its working (sucsess)
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      //Return response to user, its not working....(client failure)
      return Response.status(400).entity("Could not identify user, feel free to try again, friend").build();
    } //else {
      //return Response.status(501).entity("Server error, this may or may not take some time! Try again, friend").build();
      //}


  }

  UserCache userCache = new UserCache();

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    ArrayList<User> users = userCache.getUsers(true);

    // TODO: Add Encryption to JSON, FIX
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);

    //add encryption to json rawString object(ref.utils Encryption)
    json = Encryption.encryptDecryptXOR(json);

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system.
    @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String email) {
      User currentUser = new Gson().fromJson(email, User.class);
      User databaseUser = UserController.getUserEmail(currentUser.getEmail());

      if (currentUser == databaseUser) {
        //Return a response with status 200 and JSON as type
        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(databaseUser).build();
      } else {
        // Return a response with status 400 and JSON as type
        return Response.status(400).entity("Server error, user not found").build();
      }
    }



  // TODO: Make the system able to delete users FIX
  @POST
  @Path("/delete/{idUser}")
  public Response deleteUser(@PathParam("idUser") int id) {

    if (UserController.deleteUser(id)) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User is sucsessfully deleted, friend!").build();
    } else{
    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
     }
  }

  // TODO: Make the system able to update users
  public Response updateUser(String x) {

    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
  }
}
