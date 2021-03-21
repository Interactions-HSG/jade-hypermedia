package org.hyperagents.jade.graphs;

import jade.util.Logger;
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

public class GraphBuilder {
  protected final static Logger LOGGER = Logger.getJADELogger(HypermediaInterface.class.getName());

  protected final ValueFactory rdf;
  protected final ModelBuilder graphBuilder;

  protected final String localAddress;
  protected final int httpPort;

  public GraphBuilder(String localAddress, int httpPort) {
    rdf = SimpleValueFactory.getInstance();
    graphBuilder = new ModelBuilder();

    this.localAddress = localAddress;
    this.httpPort = httpPort;
  }

  public String getSubjectIRI() {
    return getBaseIRI();
  }

  public String getEntityIRI() {
    return getBaseIRI();
  }

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

  protected String getBaseIRI() {
    return "http://" + localAddress + ":" + httpPort + "/";
  }

  protected IRI addNonInformationResource(String property, String fragment) {
    IRI entityIRI = rdf.createIRI(getSubjectIRI() + fragment);
    graphBuilder.add(getSubjectIRI(), property, entityIRI);
    return entityIRI;
  }
}
