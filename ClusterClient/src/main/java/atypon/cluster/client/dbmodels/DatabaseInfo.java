package atypon.cluster.client.dbmodels;

public class DatabaseInfo {
    private static String name;

    public DatabaseInfo() {
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        DatabaseInfo.name = name;
    }
}
