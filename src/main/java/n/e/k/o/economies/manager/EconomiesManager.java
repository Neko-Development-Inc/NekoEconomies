package n.e.k.o.economies.manager;

import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.utils.Config;
import org.apache.logging.log4j.Logger;

public class EconomiesManager {

    private final NekoEconomies nekoEconomies;
    private final Config config;
    private final Logger logger;

    public EconomiesManager(NekoEconomies nekoEconomies, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.config = config;
        this.logger = logger;
    }

    public boolean init() {
        try {

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

}
