package n.e.k.o.economies.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import n.e.k.o.economies.eco.EcoUser;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.utils.Config;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MysqlStorage extends IStorage {

    private final NekoEconomies nekoEconomies;
    private final UserManager userManager;
    private final EconomiesManager economiesManager;
    private final Config config;
    private final Logger logger;

    private final DataSource dataSource;

    private final String CREATE_TABLE_USERS;
    private final String GET_ALL_USERS;
    private final String SAVE_USERS;
    private final String DELETE_TABLE;

    public MysqlStorage(NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.userManager = userManager;
        this.economiesManager = economiesManager;
        this.config = config;
        this.logger = logger;
        this.gson = generateGson();

        Config.Settings.Storage.Mysql mysql = config.settings.storage.mysql;

        this.CREATE_TABLE_USERS = mysql.CREATE_TABLE_USERS;
        this.GET_ALL_USERS = mysql.GET_ALL_USERS;
        this.SAVE_USERS = mysql.SAVE_USERS;
        this.DELETE_TABLE = mysql.DELETE_TABLE;

        var properties = new Properties();
        properties.setProperty("user", mysql.user);
        properties.setProperty("password", mysql.password);
        var connectionFactory = new DriverManagerConnectionFactory(String.format("jdbc:mysql://%s:%d/%s", mysql.host, mysql.port, mysql.db), properties);
        var poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        GenericObjectPoolConfig<PoolableConnection> _config = new GenericObjectPoolConfig<>();
        _config.setMaxTotal(25);
        _config.setMaxIdle(10);
        _config.setMinIdle(5);
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory, _config);
        poolableConnectionFactory.setPool(connectionPool);
        dataSource = new PoolingDataSource<>(connectionPool);
    }

    @Override
    Gson generateGson() {
        return super.generateGson(config.settings.storage.mysql.minify);
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
            t.printStackTrace();
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
            var stmt = connection.prepareStatement(GET_ALL_USERS);
            var result = stmt.executeQuery();
            var list = new ArrayList<>();
            while (result.next()) {
                var map = new HashMap<>();
                map.put("uuid", result.getString("uuid"));
                map.put("balances", result.getString("balances"));
                list.add(map);
            }
            return list;
        }
        catch (Throwable t) {
            logger.error(t.getMessage());
            logger.error(t);
            t.printStackTrace();
            return null;
        }
        finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                logger.error(e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean save() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            var values = new StringJoiner(", ");
            var items = new LinkedList<>();
            var updatedUsers = new ArrayList<EcoUser>();
            for (var user : userManager.getAllUsers()) {
                logger.info("saving user " + user.getUuid() + ", hasUpdate: " + user.hasUpdate());
                if (!user.hasUpdate()) continue;
                values.add("(?, ?)");
                var uuid = user.getUuid();
                items.add(uuid.toString());
                items.add(gson.toJson(NekoEconomies.currenciesToMapString(user.balances)));
                updatedUsers.add(user);
            }
            if (!updatedUsers.isEmpty()) {
                var QUERY = SAVE_USERS.replace("%s", values.toString());
                var stmt = connection.prepareStatement(QUERY);
                for (int i = 0; i < items.size(); i += 2) {
                    stmt.setString(i + 1, (String) items.get(i));     // uuid
                    stmt.setString(i + 2, (String) items.get(i + 1)); // balances
                }
                logger.info("DEBUG 2: '" + stmt + "'");
                stmt.execute();
                for (var user : updatedUsers)
                    user.setSaved();
            }
        }
        catch (Throwable t) {
            logger.error(t.getMessage());
            logger.error(t);
            t.printStackTrace();
            return false;
        }
        finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                logger.error(e);
                e.printStackTrace();
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
            var QUERY = SAVE_USERS.replace("%s", "(?, ?)");
            var stmt = connection.prepareStatement(QUERY);
            stmt.setString(1, user.getUuid().toString()); // uuid
            stmt.setString(2, gson.toJson(NekoEconomies.currenciesToMapString(user.balances))); // balances
            logger.info("DEBUG: '" + stmt + "'");
            stmt.execute();
            user.setSaved();
        }
        catch (Throwable t) {
            logger.error(t.getMessage());
            logger.error(t);
            t.printStackTrace();
        }
        finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                logger.error(e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public CompletableFuture<Void> saveAsync() {
        return nekoEconomies.runAsync(this::save);
    }

    @Override
    public boolean load() {
        var result = getAllUsersFromSQL();
        if (!(result instanceof ArrayList)) {
            logger.error("Failed loading users from SQL.");
            return false;
        }
        var list = (List<Map<String, Object>>) result;
        for (var map : list)
            userManager.create(map, config, economiesManager);
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
            t.printStackTrace();
        }
        finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                logger.error(e);
                e.printStackTrace();
            }
        }
        return false;
    }

}
