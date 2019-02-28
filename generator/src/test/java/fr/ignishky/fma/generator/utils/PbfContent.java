package fr.ignishky.fma.generator.utils;

import lombok.Value;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.List;

@Value
public class PbfContent {

    List<Way> ways;
    List<Relation> relations;
    List<Node> nodes;
}

