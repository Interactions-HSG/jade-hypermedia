package org.hyperagents.jade;

import jade.core.ContainerID;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PlatformGraphBuilder {
  private final ValueFactory rdf = SimpleValueFactory.getInstance();
  private final ModelBuilder graphBuilder;

  private final int httpPort;
  private final String subjectIRI;

  public PlatformGraphBuilder(String subjectIRI, int httpPort) {
    this.httpPort = httpPort;
    this.subjectIRI = subjectIRI;

    graphBuilder = new ModelBuilder();
    graphBuilder.add(subjectIRI, RDF.TYPE, rdf.createIRI(JADE.Platform));
  }

  public PlatformGraphBuilder addMetadata() {
    graphBuilder.add(subjectIRI, JADE.hasName, PlatformState.getInstance().getPlatformName());
    return this;
  }

  public PlatformGraphBuilder addContainers(List<ContainerID> containerIDs) {
    // Add containment triples from main-container to all containers
    for (ContainerID cid : containerIDs) {
      if (cid.getMain()) {
        graphBuilder.add(subjectIRI, JADE.hasMainContainer, rdf.createIRI(getContainerIRI(cid)));
      } else {
        graphBuilder.add(subjectIRI, JADE.hasContainer, rdf.createIRI(getContainerIRI(cid)));
      }
    }

    return this;
  }

  public String write(RDFFormat format) {
    OutputStream out = new ByteArrayOutputStream();

    graphBuilder.setNamespace("jade", JADE.PREFIX);

    try (out) {
      Rio.write(graphBuilder.build(), out, format,
        new WriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true));
    } catch (UnsupportedRDFormatException | IOException e) {
//      LOGGER.log(Level.WARNING, e.getMessage());
    }

    return out.toString();
  }

  private String getContainerIRI(ContainerID containerID) {
    return "http://" + containerID.getAddress() + ":" + httpPort + "/containers/"
        + containerID.getName();
  }
}
