package n.e.k.o.economies.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import n.e.k.o.economies.eco.EcoUser;

import java.util.concurrent.CompletableFuture;

public interface IStorage
{

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    default boolean init()
    {
        return true;
    }

    boolean save();

    void save(EcoUser user);

    CompletableFuture<Void> saveAsync();

    boolean load();

    CompletableFuture<Void> loadAsync();

    /**
     * Clear/delete the storage data
     * @return boolean true or false (success or fail)
     */
    boolean clear();

}
