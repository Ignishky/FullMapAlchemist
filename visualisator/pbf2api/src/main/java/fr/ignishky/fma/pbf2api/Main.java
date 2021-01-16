package fr.ignishky.fma.pbf2api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import fr.ignishky.fma.pbf2api.api.BoundingBox;
import fr.ignishky.fma.pbf2api.api.BoundingBoxFilter;
import fr.ignishky.fma.pbf2api.split.MultiSplitFile;
import fr.ignishky.fma.pbf2api.split.SingleSplitFile;
import fr.ignishky.fma.pbf2api.split.SplitAreas;
import fr.ignishky.fma.pbf2api.split.SplitFile;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import spark.utils.IOUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkArgument;
import static fr.ignishky.fma.pbf2api.model.OSM.toOSM;
import static java.lang.Double.parseDouble;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.port;

@Slf4j
public final class Main {

    private static final String APPLICATION_XML = "application/xml";
    private static final String APPLICATION_JSON = "application/json";
    private static final SplitAreas SPLIT_AREAS = new SplitAreas();
    private static final Gson GSON = new Gson();

    private Main() {
    }

    public static void main(String[] args) {
        checkArgument(args.length == 1, "Usage : Main <splitterFolder>");

        LoadingCache<String, SingleSplitFile> cache = CacheBuilder.newBuilder().maximumSize(4).build(new CacheLoader<String, SingleSplitFile>() {
            @Override
            public SingleSplitFile load(String key) {
                return new SingleSplitFile(args[0] + "/" + key + ".osm.pbf");
            }
        });

        port(9090);

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "GET");
            response.header("Access-Control-Allow-Headers", "*");
        });

        get("/api/capabilities", (req, res) -> {
            log.debug("Calling /api/capabilities");
            res.type(APPLICATION_XML);
            return IOUtils.toString(Main.class.getResourceAsStream("/api/capabilities"));
        });

        get("/api/0.6/map.json", (Request req, Response res) -> {
            log.debug("Calling /api/0.6/map.json?bbox={}", req.queryParams("bbox"));
            res.type(APPLICATION_JSON);
            BoundingBox bBox = createBBox(req.queryParams("bbox"));
            return GSON.toJson(toOSM(new BoundingBoxFilter().filter(splitFile(cache, SPLIT_AREAS.getAreas(bBox)), bBox)));
        });

        exception(Exception.class, (exception, request, response) -> log.error("An error occurred", exception));

        log.info("Waiting for request on 9090 ...");
    }

    private static BoundingBox createBBox(String input) {
        String[] split = input.split(",");
        double lng1 = parseDouble(split[0]);
        double lat1 = parseDouble(split[1]);
        double lng2 = parseDouble(split[2]);
        double lat2 = parseDouble(split[3]);
        return new BoundingBox(min(lat1, lat2), min(lng1, lng2), max(lat1, lat2), max(lng1, lng2));
    }

    private static SplitFile splitFile(LoadingCache<String, SingleSplitFile> cache, List<String> areas) throws ExecutionException {
        if (areas.size() == 1) {
            return cache.get(areas.get(0));
        }
        return new MultiSplitFile(cache.get(areas.get(0)), splitFile(cache, areas.subList(1, areas.size())));
    }
}
