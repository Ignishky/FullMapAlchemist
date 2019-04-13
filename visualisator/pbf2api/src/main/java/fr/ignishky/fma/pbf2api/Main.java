package fr.ignishky.fma.pbf2api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import fr.ignishky.fma.pbf2api.api.BoundingBox;
import fr.ignishky.fma.pbf2api.api.BoundingBoxFilter;
import fr.ignishky.fma.pbf2api.split.MultiSplitFile;
import fr.ignishky.fma.pbf2api.split.SingleSplitFile;
import fr.ignishky.fma.pbf2api.split.SplitAreas;
import fr.ignishky.fma.pbf2api.split.SplitFile;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.xml.v0_6.impl.OsmWriter;
import spark.Request;
import spark.Response;
import spark.utils.IOUtils;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Double.parseDouble;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.jetty.http.HttpStatus.Code.NOT_FOUND;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.port;

@Slf4j
public final class Main {

    private static final String APPLICATION_XML = "application/xml";

    private Main() {
    }

    public static void main(String[] args) {
        checkArgument(args.length == 1, "Usage : Main <splitterFolder>");

        SplitAreas kml = new SplitAreas();
        LoadingCache<String, SingleSplitFile> cache = CacheBuilder.newBuilder().maximumSize(4).build(new CacheLoader<String, SingleSplitFile>() {
            @Override
            public SingleSplitFile load(String key) {
                return new SingleSplitFile(args[0] + "/" + key);
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

        get("/api/0.6/map", (Request req, Response res) -> {
            log.debug("Calling /api/0.6/map?bbox={}", req.queryParams("bbox"));
            res.type(APPLICATION_XML);
            BoundingBox boundingBox = createBBox(req.queryParams("bbox"));
            return xml(new BoundingBoxFilter().filter(splittedFile(cache, kml.file(boundingBox)), boundingBox));
        });

        get("/api/0.6/node/:id", (req, res) -> {
            res.type(APPLICATION_XML);
            Long id = Long.valueOf(req.params("id"));
            return xml(new NodeContainer(cache.get(kml.file(id)).getNodeById(id)));
        });

        get("/api/0.6/node/:id/ways", (req, res) -> {
            res.type(APPLICATION_XML);
            Long id = Long.valueOf(req.params("id"));
            return xml(streamIterator(cache.get(kml.file(id)).getWays())
                    .filter(way -> way.getWayNodes().stream().map(WayNode::getNodeId).collect(toSet()).contains(id))
                    .map(WayContainer::new)
                    .collect(toList()));
        });

        get("/api/0.6/way/:id", (req, res) -> {
            res.type(APPLICATION_XML);
            Long id = Long.valueOf(req.params("id"));
            Optional<Way> way = streamIterator(cache.get(kml.file(id)).getWays()).filter(w -> w.getId() == id).findFirst();
            if (way.isPresent()) {
                return xml(new WayContainer(way.get()));

            } else {
                res.status(NOT_FOUND.getCode());
                return "Not found";
            }
        });

        get("/api/0.6/way/:id/relations", (req, res) -> {
            res.type(APPLICATION_XML);
            Long id = Long.valueOf(req.params("id"));
            return xml(streamIterator(cache.get(kml.file(id)).getRelations())
                    .filter(rel -> rel.getMembers().stream()
                            .filter(member -> member.getMemberType() == Way)
                            .map(RelationMember::getMemberId)
                            .collect(toSet())
                            .contains(id))
                    .map(RelationContainer::new)
                    .collect(toList()));
        });

        get("/api/0.6/relation/:id", (req, res) -> {
            res.type(APPLICATION_XML);
            Long id = Long.valueOf(req.params("id"));
            Optional<Relation> relation = streamIterator(cache.get(kml.file(id)).getRelations()).filter(rel -> rel.getId() == id).findFirst();
            if (relation.isPresent()) {
                return xml(new RelationContainer(relation.get()));

            } else {
                res.status(NOT_FOUND.getCode());
                return "Not found";
            }
        });

        get("/api/0.6/relation/:id/full", (req, res) -> {
            res.type(APPLICATION_XML);
            Long id = Long.valueOf(req.params("id"));
            SingleSplitFile file = cache.get(kml.file(id));
            Optional<Relation> relation = streamIterator(file.getRelations()).filter(rel -> rel.getId() == id).findFirst();
            if (relation.isPresent()) {
                EntityContainer rel = new RelationContainer(relation.get());
                List<EntityContainer> entities = relation.get().getMembers().stream()
                        .filter(rm -> rm.getMemberType() == Way)
                        .map(RelationMember::getMemberId)
                        .map(geohash -> {
                            try {
                                return streamIterator(cache.get(kml.file(geohash)).getWays()).filter(w -> w.getId() == geohash).findFirst();

                            } catch (ExecutionException e) {
                                throw new IllegalStateException(e);
                            }
                        })
                        .filter(Optional::isPresent)
                        .flatMap(o -> {
                            try {
                                List<EntityContainer> containers = Lists.newArrayList();
                                for (WayNode wn : o.get().getWayNodes()) {
                                    containers.add(new NodeContainer(cache.get(kml.file(wn.getNodeId())).getNodeById(wn.getNodeId())));
                                }
                                containers.add(new WayContainer(o.get()));
                                return containers.stream();

                            } catch (ExecutionException e) {
                                throw new IllegalStateException(e);
                            }
                        })
                        .collect(toList());
                entities.add(rel);
                return xml(entities);

            } else {
                res.status(NOT_FOUND.getCode());
                return "Not found";
            }
        });

        exception(Exception.class, (exception, request, response) -> log.error("An error occurred", exception));

        log.info("Waiting for request on 9090 ...");
    }

    private static BoundingBox createBBox(String input) {
        String[] split = input.split(",");
        double lat1 = parseDouble(split[1]);
        double lat2 = parseDouble(split[3]);
        double lng1 = parseDouble(split[0]);
        double lng2 = parseDouble(split[2]);
        return new BoundingBox(min(lat1, lat2), min(lng1, lng2), max(lat1, lat2), max(lng1, lng2));
    }

    private static <T> Stream<T> streamIterator(Iterator<T> it) {
        return StreamSupport.stream(((Iterable<T>) () -> it).spliterator(), false);
    }

    private static String xml(List<EntityContainer> containers) {
        OsmWriter osmWriter = new OsmWriter("osm", 0, true, false);
        StringWriter stringWriter = new StringWriter();
        osmWriter.setWriter(stringWriter);
        osmWriter.begin();
        for (EntityContainer container : containers) {
            osmWriter.process(container);
        }
        osmWriter.end();
        return stringWriter.toString();
    }

    private static String xml(EntityContainer container) {
        OsmWriter osmWriter = new OsmWriter("osm", 0, true, false);
        StringWriter stringWriter = new StringWriter();
        osmWriter.setWriter(stringWriter);
        osmWriter.begin();
        osmWriter.process(container);
        osmWriter.end();
        return stringWriter.toString();
    }

    private static SplitFile splittedFile(LoadingCache<String, SingleSplitFile> cache, List<String> fileNames) throws ExecutionException {
        if (fileNames.size() == 1) {
            return cache.get(fileNames.get(0));
        }
        return new MultiSplitFile(cache.get(fileNames.get(0)), splittedFile(cache, fileNames.subList(1, fileNames.size())));
    }
}
