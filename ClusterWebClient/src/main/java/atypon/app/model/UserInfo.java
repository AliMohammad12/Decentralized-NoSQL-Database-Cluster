package atypon.app.model;

public class UserInfo {
    private static String username;
    private static String password;

    public UserInfo() {
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        UserInfo.username = username;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        UserInfo.password = password;
    }
}
