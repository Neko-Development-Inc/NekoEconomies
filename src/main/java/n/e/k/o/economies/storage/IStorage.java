package n.e.k.o.economies.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import n.e.k.o.economies.api.EcoUser;

import java.util.concurrent.CompletableFuture;

public abstract class IStorage
{

    Gson gson;

    Gson generateGson() {
        return generateGson(true);
    }

    Gson generateGson(boolean minify) {
        var gson = new GsonBuilder();
        if (!minify) gson.setPrettyPrinting();
        return gson.create();
    }

    public boolean init() {
        return true;
    }

    public boolean save() {
        return true;
    }

    public void save(EcoUser user) { }

    public CompletableFuture<Void> saveAsync() {
        return null;
    }

    public boolean load() {
        return true;
    }

    public CompletableFuture<Void> loadAsync() {
        return null;
    }

    /**
     * Clear/delete the storage data
     * @return boolean true or false (success or fail)
     */
    public boolean clear() {
        return true;
    }

}
