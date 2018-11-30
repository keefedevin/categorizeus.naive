package us.categorize.naive;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import us.categorize.api.UserStore;
import us.categorize.model.User;

public class NaiveUserStore implements UserStore {

	private Connection connection;
		
	public NaiveUserStore(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public User getPrincipal(String sessionKey) {
		String findSessionUser = "select * from user_sessions where session_uuid=?";
		try {
			PreparedStatement stmt = connection.prepareStatement(findSessionUser);
			stmt.setString(1,  sessionKey);
			ResultSet rs = stmt.executeQuery();
			if(rs!=null && rs.next()){
				return find(rs.getLong("user_id"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private User find(long id)  {
		String findUser = "select * from users where id=?";
		PreparedStatement stmt;
		try {
			stmt = connection.prepareStatement(findUser);
			stmt.setLong(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs!=null && rs.next()){
				User user = new User();
				user.setId(rs.getLong("id"));
				user.setEmail(rs.getString("email"));
				user.setPasshash(rs.getString("passhash"));
				user.setUsername(rs.getString("username"));
				return user;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean registerUser(User user) {
		String insertUser = "insert into users(username, passhash, email) values (?,?,?)";
		PreparedStatement stmt;
		try {
			stmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getPasshash());
			stmt.setString(3, user.getEmail());
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();
			long newId = rs.getLong(1);
			user.setId(newId);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public static String sha256hash(String password) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedhash = digest.digest(
					password.getBytes(StandardCharsets.UTF_8));
			return bytesToHex(encodedhash);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static String bytesToHex(byte[] hash) {
	    StringBuffer hexString = new StringBuffer();
	    for (int i = 0; i < hash.length; i++) {
	    String hex = Integer.toHexString(0xff & hash[i]);
	    if(hex.length() == 1) hexString.append('0');
	        hexString.append(hex);
	    }
	    return hexString.toString();
	}
	
	
	//TODO the login validation part probably doesn't belong here, think about this after doing the OAuth integration
	@Override
	public boolean establishUserSession(User user, String sessionKey) {
		String createUserSession = "insert into user_sessions(session_uuid, user_id) values (?,?)";
		String findUser = "select * from users where username=? and passhash=?";
		String sessionExists = "select * from user_sessions where session_uuid = ?";
		try{
			PreparedStatement stmt = connection.prepareStatement(findUser);
			stmt.setString(1, user.getUsername());
			stmt.setString(2, sha256hash(user.getPasshash()));
			ResultSet rs = stmt.executeQuery();
			if(!(rs!=null && rs.next())){
				return false;		
			}
			user.setId(rs.getLong("id"));
			stmt = connection.prepareStatement(sessionExists);
			stmt.setString(1, sessionKey);
			rs = stmt.executeQuery();
			if(rs!=null && rs.next()) return true;
			stmt = connection.prepareStatement(createUserSession);
			stmt.setString(1,sessionKey);
			stmt.setLong(2, user.getId());
			stmt.executeUpdate();
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean destroySessionUser(String sessionUUID) {
		String deleteUserSession = "delete from user_sessions where session_uuid = ?";
		try {
			PreparedStatement stmt = connection.prepareStatement(deleteUserSession);
			stmt.setString(1, sessionUUID);
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public User getUser(long id) {
		return find(id);
	}

}
