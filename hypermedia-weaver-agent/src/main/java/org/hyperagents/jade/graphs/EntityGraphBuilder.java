package org.hyperagents.jade.graphs;

import jade.util.Logger;
import jade.util.leap.Properties;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.hyperagents.jade.HypermediaInterface;
import org.hyperagents.jade.vocabs.FIPA;
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
  protected final static Logger LOGGER = Logger.getJADELogger(HypermediaInterface.class.getName());

  protected final ValueFactory rdf;
  protected final ModelBuilder graphBuilder;

  protected final Properties config;
  protected String baseIRI = "http://localhost:3000/";

  /**
   * Constructs an entity graph builder.
   * @param config the set of properties provided as arguments to this container
   */
  public EntityGraphBuilder(Properties config) {
    rdf = SimpleValueFactory.getInstance();
    graphBuilder = new ModelBuilder();

    this.config = config;
    String httpAuthority = config.getProperty("http-host", "localhost");

    try {
      int httpPort = Integer.parseInt(config.getProperty("http-port", "3000"));
      baseIRI = "http://" + httpAuthority + ":" + httpPort + "/";
    } catch (NumberFormatException e) {
      LOGGER.log(Logger.SEVERE, "Provided HTTP port is not a number.");
    }
  }

  /**
   * Serializes the constructed RDF graph as a string.
   * @param format the RDF serialization format to be used
   * @return a string serialization of the constructed graph
   */
  public String write(RDFFormat format) {
    OutputStream out = new ByteArrayOutputStream();

    graphBuilder.setNamespace("jade", JADE.PREFIX);
    graphBuilder.setNamespace("fipa", FIPA.PREFIX);

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

  /**
   * Retrieves the IRI of the entity described by this graph. If the entity is an information resource,
   * this method should return the same value with the {@link #getDocumentIRI() getDocumentIRI} method.
   * @return the entity's IRI
   */
  protected String getEntityIRI() {
    return baseIRI;
  }

  /**
   * Convenience method used to create a non-information resource described by this graph.
   * @param property the property that relates the non-information resource to the information resource
   *                 describing it
   * @param fragment fragment to be used for creating a hash IRI that identifies the non-information
   *                 resource (see <a href="https://www.w3.org/TR/cooluris/">W3C IG note on Cool URIs
   *                 for the Semantic Web</a>
   * @return a hash IRI that identifies the described non-information resource
   */
  protected IRI createNonInformationResource(String property, String fragment) {
    IRI entityIRI = rdf.createIRI(getDocumentIRI() + fragment);
    graphBuilder.add(getDocumentIRI(), property, entityIRI);
    return entityIRI;
  }
}
