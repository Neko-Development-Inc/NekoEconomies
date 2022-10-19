package n.e.k.o.economies.exceptions;

public class ApiNotYetAvailable extends RuntimeException {

    public static final ApiNotYetAvailable THROW = new ApiNotYetAvailable();

    private ApiNotYetAvailable() {
        super();
    }

}
