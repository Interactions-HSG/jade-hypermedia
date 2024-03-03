package org.hyperagents.jade.graphs;

import jade.util.Logger;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.hyperagents.jade.vocabs.HMAS;
import org.hyperagents.jade.vocabs.JADE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The entity graph builder is used to construct RDF representations of entities in a JADE system.
 * The constructed RDF graph represents an information resource, which may describe a non-information
 * resource (e.g., an agent).
 *
 * This is an abstract class intended to be extended. The current implementation is based on RDF4J.
 */
public abstract class EntityGraphBuilder {
  protected final static Logger LOGGER = Logger.getJADELogger(EntityGraphBuilder.class.getName());

  protected final ValueFactory rdf;
  protected final ModelBuilder graphBuilder;

  private final String baseIRI;

  /**
   * Constructs an entity graph builder.
   * @param iri the IRI that identifies the main subject of this graph
   */
  public EntityGraphBuilder(String iri) {
    baseIRI = iri;
    rdf = SimpleValueFactory.getInstance();
    graphBuilder = new ModelBuilder();
  }

  /**
   * Serializes the constructed RDF graph as a string.
   * @param format the RDF serialization format to be used
   * @return a string serialization of the constructed graph
   */
  public String write(RDFFormat format) {
    OutputStream out = new ByteArrayOutputStream();

    graphBuilder.setNamespace("hmas", HMAS.PREFIX);
    graphBuilder.setNamespace("jade", JADE.PREFIX);

    try (out) {
      Rio.write(graphBuilder.build(), out, format,
        new WriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true));
    } catch (UnsupportedRDFormatException | IOException e) {
      LOGGER.log(Logger.SEVERE, e.getMessage());
    }

    return out.toString();
  }

  /**
   * Retrieves the IRI of the main subject of this graph. The main subject typically identifies an
   * information resources.
   * @return the document's IRI
   */
  protected String getDocumentIRI() {
    return baseIRI;
  }
}
