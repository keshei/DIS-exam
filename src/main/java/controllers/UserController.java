package controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.org.apache.xml.internal.security.algorithms.JCEMapper;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }
  public static User getUserEmail(String email) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where email=" + email;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }
  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it. FIXED
    int userID = dbCon.insert(
        "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
            + user.getFirstname()
            + "', '"
            + user.getLastname()
            + "', '"
            + Hashing.shaWithSalt(user.getPassword())
            + "', '"
            + user.getEmail()
            + "', "
            + user.getCreatedTime()
            + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else{
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }

  public static void deleteUser (User user) {
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }
    try {
        PreparedStatement deleteUser = dbCon.getConnection().prepareStatement("DELETE FROM user WHERE id = ?");
        deleteUser.setInt(1, user.getId());

        deleteUser.executeUpdate();

    }
    catch (SQLException sql){
      sql.printStackTrace();
    }
  }
  public static User updateUser(User user) {

    try {

      PreparedStatement updateUser = dbCon.getConnection().prepareStatement( "UPDATE user SET first_name = ?, last_name = ?, password =?, email = ? WHERE id =?");

      updateUser.setString(1,user.getFirstname());
      updateUser.setString(2,user.getLastname());
      updateUser.setString(3, user.getPassword());
      updateUser.setString(4, user.getPassword());
      updateUser.setInt( 5, user.getId());

      updateUser.executeUpdate();
    } catch (SQLException sql) {
      sql.printStackTrace();
    }
    return user;
  }

  public static String getLogin(User user){
    //Check for connection to DB
    if (dbCon == null){
      dbCon= new DatabaseController();
    }

    //Build the query for DB

    String sql = "SELECT * FROM user WHERE email= '" + user.getEmail() + " 'AND (password=' " + Hashing.shaWithSalt(user.getPassword()) + " ' OR password = ' " + user.getPassword() + " ' )";

    //Here is where the query executes
    ResultSet rs = dbCon.query(sql);
    User userLogin;
    String token = null;

    try {
      //Get first object, since we only have one
      if (rs.next()) {
        userLogin =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));
        if (userLogin != null){
          try {
            Date expire = new Date();
            expire.setTime(System.currentTimeMillis() + 1000000);
            Algorithm algorithm = Algorithm.HMAC256("secret");
            token = JWT.create()
                    .withClaim("userID", user.getId())
                    .withExpiresAt(expire)
                    .withIssuer("auth0")
                    .sign(algorithm);
          } catch (JWTCreationException exception ){
            //Invalid signing configuration /could not convert Claims
          } finally {
            return token;
          }
        }
      } else {
        System.out.println("No user found, my friend");
      }
    } catch (SQLException ex){
      System.out.println(ex.getMessage());
    }
      //Return null
    return  "";
  }


  public static String getTokenVerifier(User user){
    //Check for connection to DB
    if (dbCon == null){
      dbCon= new DatabaseController();
    }

    String token = user.getToken();
          try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            Claim claim = jwt.getClaim("userID");

            if (user.getId() == claim.asInt()) {
              return token;
            }

          } catch (JWTVerificationException e){
            System.out.println(e.getMessage());
            //Invalid signing configuration /could not convert Claims
          }

    //Return null
    return  "";
  }

}
