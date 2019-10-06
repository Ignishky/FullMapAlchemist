package fr.ignishky.fma.generator.merger;

import com.google.protobuf.InvalidProtocolBufferException;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.osmbinary.Fileformat.Blob;
import org.openstreetmap.osmosis.osmbinary.Osmformat.DenseInfo;
import org.openstreetmap.osmosis.osmbinary.Osmformat.DenseNodes;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Info;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Node;
import org.openstreetmap.osmosis.osmbinary.Osmformat.PrimitiveBlock;
import org.openstreetmap.osmosis.osmbinary.Osmformat.PrimitiveGroup;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Relation;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Relation.MemberType;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Way;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.PbfFieldDecoder;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.PbfRawBlob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static java.util.Collections.emptyList;

final class PbfDecoder {

    private static final int EMPTY_VERSION = -1;
    private static final Date EMPTY_TIMESTAMP = new Date(0L);
    private static final long EMPTY_CHANGE_SET = -1;

    private PbfDecoder() {
    }

    static List<EntityContainer> decode(PbfRawBlob data) {
        if (!"OSMData".equals(data.getType())) {
            return emptyList();
        }

        PrimitiveBlock block = inflate(data);
        PbfFieldDecoder fieldDecoder = new PbfFieldDecoder(block);

        List<EntityContainer> decodedEntities = new ArrayList<>();
        for (PrimitiveGroup primitiveGroup : block.getPrimitivegroupList()) {
            decodedEntities.addAll(processNodes(primitiveGroup.getDense(), fieldDecoder));
            decodedEntities.addAll(processNodes(primitiveGroup.getNodesList(), fieldDecoder));
            decodedEntities.addAll(processWays(primitiveGroup.getWaysList(), fieldDecoder));
            decodedEntities.addAll(processRelations(primitiveGroup.getRelationsList(), fieldDecoder));
        }
        return decodedEntities;
    }

    private static PrimitiveBlock inflate(PbfRawBlob rawBlob) {
        try {
            Blob blob = Blob.parseFrom(rawBlob.getData());
            byte[] blobData;
            if (blob.hasRaw()) {
                blobData = blob.getRaw().toByteArray();
            } else if (blob.hasZlibData()) {
                Inflater inflater = new Inflater();
                inflater.setInput(blob.getZlibData().toByteArray());
                blobData = new byte[blob.getRawSize()];
                try {
                    inflater.inflate(blobData);
                } catch (DataFormatException e) {
                    throw new OsmosisRuntimeException("Unable to decompress PBF blob.", e);
                }

                if (!inflater.finished()) {
                    throw new OsmosisRuntimeException("PBF blob contains incomplete compressed data.");
                }
            } else {
                throw new OsmosisRuntimeException("PBF blob uses unsupported compression, only raw or zlib may be used.");
            }
            return PrimitiveBlock.parseFrom(blobData);

        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void buildTags(CommonEntityData entityData, List<Integer> keys, List<Integer> values, PbfFieldDecoder fieldDecoder) {
        Collection<Tag> tags = entityData.getTags();

        // Ensure parallel lists are of equal size.
        if (keys.size() != values.size()) {
            throw new OsmosisRuntimeException("Number of tag keys (" + keys.size() + ") and tag values (" + values.size() + ") don't match");
        }

        Iterator<Integer> keyIterator = keys.iterator();
        Iterator<Integer> valueIterator = values.iterator();
        while (keyIterator.hasNext()) {
            String key = fieldDecoder.decodeString(keyIterator.next());
            String value = fieldDecoder.decodeString(valueIterator.next());
            Tag tag = new Tag(key, value);
            tags.add(tag);
        }
    }

    private static CommonEntityData toCommonEntityData(long entityId, List<Integer> keys, List<Integer> values, Info info,
                                                       PbfFieldDecoder fieldDecoder) {
        OsmUser user;

        // Build the user, but only if one exists.
        if (info.hasUid() && info.getUid() >= 0 && info.hasUserSid()) {
            user = new OsmUser(info.getUid(), fieldDecoder.decodeString(info.getUserSid()));
        } else {
            user = OsmUser.NONE;
        }

        CommonEntityData entityData = new CommonEntityData(entityId, info.getVersion(),
                fieldDecoder.decodeTimestamp(info.getTimestamp()), user, info.getChangeset());

        buildTags(entityData, keys, values, fieldDecoder);

        return entityData;
    }

    private static CommonEntityData toCommonEntityData(long entityId, List<Integer> keys, List<Integer> values, PbfFieldDecoder fieldDecoder) {
        CommonEntityData entityData = new CommonEntityData(entityId, EMPTY_VERSION, EMPTY_TIMESTAMP, OsmUser.NONE, EMPTY_CHANGE_SET);

        buildTags(entityData, keys, values, fieldDecoder);

        return entityData;
    }

    private static List<EntityContainer> processNodes(List<Node> nodes, PbfFieldDecoder fieldDecoder) {
        List<EntityContainer> decodedEntities = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            org.openstreetmap.osmosis.core.domain.v0_6.Node osmNode;
            CommonEntityData entityData;

            if (node.hasInfo()) {
                entityData = toCommonEntityData(node.getId(), node.getKeysList(), node.getValsList(), node.getInfo(), fieldDecoder);

            } else {
                entityData = toCommonEntityData(node.getId(), node.getKeysList(), node.getValsList(), fieldDecoder);
            }

            osmNode = new org.openstreetmap.osmosis.core.domain.v0_6.Node(entityData, fieldDecoder.decodeLatitude(node
                    .getLat()), fieldDecoder.decodeLatitude(node.getLon()));

            // Add the bound object to the results.
            decodedEntities.add(new NodeContainer(osmNode));
        }
        return decodedEntities;
    }

    private static List<EntityContainer> processNodes(DenseNodes nodes, PbfFieldDecoder fieldDecoder) {
        List<EntityContainer> decodedEntities = new ArrayList<>(nodes.getIdCount());
        List<Long> idList = nodes.getIdList();
        List<Long> latList = nodes.getLatList();
        List<Long> lonList = nodes.getLonList();

        // Ensure parallel lists are of equal size.
        if (idList.size() != latList.size() || idList.size() != lonList.size()) {
            throw new OsmosisRuntimeException("Number of ids (" + idList.size() + "), latitudes (" + latList.size()
                    + "), and longitudes (" + lonList.size() + ") don't match");
        }

        Iterator<Integer> keysValuesIterator = nodes.getKeysValsList().iterator();

        DenseInfo denseInfo = nodes.hasDenseinfo() ? nodes.getDenseinfo() : null;

        long nodeId = 0;
        long latitude = 0;
        long longitude = 0;
        int userId = 0;
        int userSid = 0;
        long timestamp = 0;
        long changesetId = 0;
        for (int i = 0; i < idList.size(); i++) {
            CommonEntityData entityData;

            // Delta decode node fields.
            nodeId += idList.get(i);
            latitude += latList.get(i);
            longitude += lonList.get(i);

            if (denseInfo != null) {
                // Delta decode dense info fields.
                userId += denseInfo.getUid(i);
                userSid += denseInfo.getUserSid(i);
                timestamp += denseInfo.getTimestamp(i);
                changesetId += denseInfo.getChangeset(i);

                // Build the user, but only if one exists.
                OsmUser user;
                if (userId >= 0) {
                    user = new OsmUser(userId, fieldDecoder.decodeString(userSid));
                } else {
                    user = OsmUser.NONE;
                }

                entityData = new CommonEntityData(nodeId, denseInfo.getVersion(i), fieldDecoder.decodeTimestamp(timestamp), user, changesetId);
            } else {
                entityData = new CommonEntityData(nodeId, EMPTY_VERSION, EMPTY_TIMESTAMP, OsmUser.NONE, EMPTY_CHANGE_SET);
            }

            // Build the tags. The key and value string indexes are sequential
            // in the same PBF array. Each set of tags is delimited by an index
            // with a value of 0.
            Collection<Tag> tags = entityData.getTags();
            while (keysValuesIterator.hasNext()) {
                int keyIndex = keysValuesIterator.next();
                if (keyIndex == 0) {
                    break;
                }
                if (!keysValuesIterator.hasNext()) {
                    throw new OsmosisRuntimeException(
                            "The PBF DenseInfo keys/values list contains a key with no corresponding value.");
                }
                int valueIndex = keysValuesIterator.next();

                Tag tag = new Tag(fieldDecoder.decodeString(keyIndex), fieldDecoder.decodeString(valueIndex));
                tags.add(tag);
            }

            org.openstreetmap.osmosis.core.domain.v0_6.Node node = new org.openstreetmap.osmosis.core.domain.v0_6.Node(entityData,
                    fieldDecoder.decodeLatitude(latitude), fieldDecoder.decodeLongitude(longitude));

            // Add the bound object to the results.
            decodedEntities.add(new NodeContainer(node));
        }
        return decodedEntities;
    }

    private static List<EntityContainer> processWays(List<Way> ways, PbfFieldDecoder fieldDecoder) {
        List<EntityContainer> decodedEntities = new ArrayList<>(ways.size());
        for (Way way : ways) {

            CommonEntityData entityData;

            if (way.hasInfo()) {
                entityData = toCommonEntityData(way.getId(), way.getKeysList(), way.getValsList(), way.getInfo(), fieldDecoder);
            } else {
                entityData = toCommonEntityData(way.getId(), way.getKeysList(), way.getValsList(), fieldDecoder);
            }

            org.openstreetmap.osmosis.core.domain.v0_6.Way osmWay = new org.openstreetmap.osmosis.core.domain.v0_6.Way(entityData);

            // Build up the list of way nodes for the way. The node ids are
            // delta encoded meaning that each id is stored as a delta against
            // the previous one.
            long nodeId = 0L;
            List<WayNode> wayNodes = osmWay.getWayNodes();
            for (long nodeIdOffset : way.getRefsList()) {
                nodeId += nodeIdOffset;
                wayNodes.add(new WayNode(nodeId));
            }

            decodedEntities.add(new WayContainer(osmWay));
        }
        return decodedEntities;
    }

    private static void buildRelationMembers(org.openstreetmap.osmosis.core.domain.v0_6.Relation relation,
                                             List<Long> memberIds, List<Integer> memberRoles, List<MemberType> memberTypes, PbfFieldDecoder fieldDecoder) {

        // Ensure parallel lists are of equal size.
        if (memberIds.size() != memberRoles.size() || memberIds.size() != memberTypes.size()) {
            throw new OsmosisRuntimeException("Number of member ids (" + memberIds.size() + "), member roles ("
                    + memberRoles.size() + "), and member types (" + memberTypes.size() + ") don't match");
        }

        Iterator<Long> memberIdIterator = memberIds.iterator();
        Iterator<Integer> memberRoleIterator = memberRoles.iterator();
        Iterator<MemberType> memberTypeIterator = memberTypes.iterator();

        // Build up the list of relation members for the way. The member ids are
        // delta encoded meaning that each id is stored as a delta against
        // the previous one.
        long memberId = 0L;
        while (memberIdIterator.hasNext()) {
            MemberType memberType = memberTypeIterator.next();
            memberId += memberIdIterator.next();
            EntityType entityType;

            if (memberType == MemberType.NODE) {
                entityType = EntityType.Node;
            } else if (memberType == MemberType.WAY) {
                entityType = EntityType.Way;
            } else if (memberType == MemberType.RELATION) {
                entityType = EntityType.Relation;
            } else {
                throw new OsmosisRuntimeException("Member type of " + memberType + " is not supported.");
            }

            relation.getMembers().add(new RelationMember(memberId, entityType, fieldDecoder.decodeString(memberRoleIterator.next())));
        }
    }

    private static List<EntityContainer> processRelations(List<Relation> relations, PbfFieldDecoder fieldDecoder) {
        List<EntityContainer> decodedEntities = new ArrayList<>(relations.size());

        for (Relation relation : relations) {
            CommonEntityData entityData;

            if (relation.hasInfo()) {
                entityData = toCommonEntityData(relation.getId(), relation.getKeysList(), relation.getValsList(), relation.getInfo(), fieldDecoder);
            } else {
                entityData = toCommonEntityData(relation.getId(), relation.getKeysList(), relation.getValsList(), fieldDecoder);
            }

            org.openstreetmap.osmosis.core.domain.v0_6.Relation osmRelation = new org.openstreetmap.osmosis.core.domain.v0_6.Relation(entityData);

            buildRelationMembers(osmRelation, relation.getMemidsList(), relation.getRolesSidList(), relation.getTypesList(), fieldDecoder);

            // Add the bound object to the results.
            decodedEntities.add(new RelationContainer(osmRelation));
        }
        return decodedEntities;
    }
}
