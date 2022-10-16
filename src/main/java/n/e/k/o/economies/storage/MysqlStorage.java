package n.e.k.o.economies.storage;

import n.e.k.o.economies.EcoUser;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.utils.Config;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MysqlStorage implements IStorage {

    private final NekoEconomies nekoEconomies;
    private final Config config;
    private final Logger logger;

    private final DataSource dataSource;

    private final String CREATE_TABLE_USERS;
    private final String GET_ALL_USERS;
    private final String SAVE_USERS;
    private final String DELETE_TABLE;

    public MysqlStorage(NekoEconomies nekoEconomies, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.config = config;
        this.logger = logger;

        Config.Settings.Storage.Mysql mysql = config.settings.storage.mysql;

        this.CREATE_TABLE_USERS = mysql.CREATE_TABLE_USERS;
        this.GET_ALL_USERS = mysql.GET_ALL_USERS;
        this.SAVE_USERS = mysql.SAVE_USERS;
        this.DELETE_TABLE = mysql.DELETE_TABLE;

        Properties properties = new Properties();
        properties.setProperty("user", mysql.user);
        properties.setProperty("password", mysql.password);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(String.format("jdbc:mysql://%s:%d/%s", mysql.host, mysql.port, mysql.db), properties);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        GenericObjectPoolConfig<PoolableConnection> _config = new GenericObjectPoolConfig<>();
        _config.setMaxTotal(25);
        _config.setMaxIdle(10);
        _config.setMinIdle(5);
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory, _config);
        poolableConnectionFactory.setPool(connectionPool);
        dataSource = new PoolingDataSource<>(connectionPool);
    }

    @Override
    public boolean init() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.prepareStatement(CREATE_TABLE_USERS).executeUpdate();
            return true;
        }
        catch (Throwable t) {
            logger.error(t.getMessage());
            logger.error(t);
            return false;
        }
        finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                logger.error(e);
            }
        }
    }

    public Object getAllUsersFromSQL() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement stmt = connection.prepareStatement(GET_ALL_USERS);
            ResultSet result = stmt.executeQuery();
            List<Map<String, Object>> list = new ArrayList<>();
            while (result.next()) {
                Map<String, Object> map = new HashMap<>();
                // TODO: Add user variables
//                map.put("uuid", result.getString("uuid"));
                list.add(map);
            }
            return list;
        }
        catch (Throwable t) {
            logger.error(t.getMessage());
            logger.error(t);
            return null;
        }
        finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                logger.error(e);
            }
        }
    }

    @Override
    public boolean save() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            StringJoiner values = new StringJoiner(", ");
            List<Object> items = new LinkedList<>();
            List<EcoUser> updatedUsers = new ArrayList<>();
            for (EcoUser user : UserManager.getAllUsers()) {
                logger.info("saving user " + user.getUuid() + ", hasUpdate: " + user.hasUpdate());
                if (!user.hasUpdate()) continue;
                values.add("(?, ?, ?, ?, ?)");
                UUID uuid = user.getUuid();
                items.add(uuid.toString());
//                items.add(user.getElo());
//                items.add(user.getWins());
//                items.add(user.getLosses());
//                items.add(getLastBattlesString(user.getLastBattles()));
                updatedUsers.add(user);
            }
            if (!updatedUsers.isEmpty()) {
                String QUERY = SAVE_USERS.replace("%s", values.toString());
                PreparedStatement stmt = connection.prepareStatement(QUERY);
                for (int i = 0; i < items.size(); i += 5) { // TODO: Change number '5' to the amount of variables!
                    stmt.setString(i + 1, (String) items.get(i));                            // uuid
//                    stmt.setBigDecimal(i + 2, BigDecimal.valueOf((float) items.get(i + 1))); // elo
//                    stmt.setInt(   i + 3, (int) items.get(i + 2));                           // wins
//                    stmt.setInt(   i + 4, (int) items.get(i + 3));                           // losses
//                    stmt.setString(i + 5, (String) items.get(i + 4));                        // lastBattles
                }
                logger.info("DEBUG 2: '" + stmt + "'");
                stmt.execute();
                for (EcoUser user : updatedUsers)
                    user.setSaved();
            }
        }
        catch (Throwable t) {
            logger.error(t.getMessage());
            logger.error(t);
            return false;
        }
        finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                logger.error(e);
            }
        }
        return true;
    }

    @Override
    public void save(EcoUser user) {
        if (!user.hasUpdate()) return;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            String QUERY = SAVE_USERS.replace("%s", "(?, ?, ?, ?, ?)");
            PreparedStatement stmt = connection.prepareStatement(QUERY);
            stmt.setString(1, user.getUuid().toString());                   // uuid
//            stmt.setBigDecimal(2, BigDecimal.valueOf(user.getElo()));       // elo
//            stmt.setInt(   3, user.getWins());                              // wins
//            stmt.setInt(   4, user.getLosses());                            // losses
//            stmt.setString(5, getLastBattlesString(user.getLastBattles())); // lastBattles
            logger.info("DEBUG: '" + stmt + "'");
            stmt.execute();
            user.setSaved();
        }
        catch (Throwable t) {
            logger.error(t.getMessage());
            logger.error(t);
        }
        finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                logger.error(e);
            }
        }
    }

    @Override
    public CompletableFuture<Void> saveAsync() {
        return nekoEconomies.runAsync(this::save);
    }

    @Override
    public boolean load() {
        Object result = getAllUsersFromSQL();
        if (!(result instanceof ArrayList)) {
            logger.error("Failed loading users from SQL.");
            return false;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) result;
        for (Map<String, Object> map : list)
            UserManager.create(map, config);
        return true;
    }

    @Override
    public CompletableFuture<Void> loadAsync() {
        return nekoEconomies.runAsync(this::load);
    }

    @Override
    public boolean clear() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.prepareStatement(DELETE_TABLE).execute();
            return true;
        }
        catch (Throwable t) {
            logger.error(t.getMessage());
            logger.error(t);
        }
        finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                logger.error(e);
            }
        }
        return false;
    }

}
