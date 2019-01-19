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
				return find(""+rs.getLong("user_id"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private User find(String id)  {
		String findUser = "select * from users where id=?";
		PreparedStatement stmt;
		try {
			stmt = connection.prepareStatement(findUser);
			stmt.setLong(1, Long.parseLong(id));
			ResultSet rs = stmt.executeQuery();
			User user = null;
			user = mapUserRow(rs);
			return user;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private User mapUserRow(ResultSet rs) throws SQLException {
		User user = null;
		if(rs!=null && rs.next()){
			user = new User();
			user.setId(rs.getLong("id")+"");
			user.setEmail(rs.getString("email"));
			user.setPasshash(rs.getString("passhash"));
			user.setUsername(rs.getString("username"));
			user.setName(rs.getString("name"));
			user.setGivenName(rs.getString("given_name"));
			user.setFamilyName(rs.getString("family_name"));
			user.setAuthorized(rs.getBoolean("authorized"));
		}
		return user;
	}

	@Override
	public boolean registerUser(User user) {
		String insertUser = "insert into users(username, passhash, email, name, given_name, family_name, authorized) values (?,?,?,?,?,?,?)";
		PreparedStatement stmt;
		try {
			stmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getPasshash());
			stmt.setString(3, user.getEmail());
			stmt.setString(4, user.getName());
			stmt.setString(5, user.getGivenName());
			stmt.setString(6, user.getFamilyName());
			stmt.setBoolean(7, user.isAuthorized());
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();
			String newId = ""+rs.getLong(1);
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
	
	
	@Override
	public boolean establishUserSession(User user, String sessionKey) {
		String createUserSession = "insert into user_sessions(session_uuid, user_id) values (?,?)";
		String sessionExists = "select * from user_sessions where session_uuid = ?";
		try{
			PreparedStatement stmt = connection.prepareStatement(sessionExists);
			stmt.setString(1, sessionKey);
			ResultSet rs = stmt.executeQuery();
			if(rs!=null && rs.next()) return true;
			stmt = connection.prepareStatement(createUserSession);
			stmt.setString(1,sessionKey);
			stmt.setLong(2, Long.parseLong(user.getId()));
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
	public User getUser(String id) {
		return find(id);
	}

	@Override
	public User getUserByUserName(String userName) {
		String findUser = "select * from users where username=?";
		PreparedStatement stmt;
		try {
			stmt = connection.prepareStatement(findUser);
			stmt.setString(1, userName);
			ResultSet rs = stmt.executeQuery();
			User user = null;
			user = mapUserRow(rs);
			return user;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean validateUser(User user) {
		String findUser = "select * from users where username=? and passhash=?";
		PreparedStatement stmt;
		try {
			stmt = connection.prepareStatement(findUser);
			stmt.setString(1, user.getUsername());
			stmt.setString(2, sha256hash(user.getPasshash()));
			ResultSet rs = stmt.executeQuery();
			if(!(rs!=null && rs.next())){
				return false;		
			}
			user.setId(rs.getLong("id")+"");
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
