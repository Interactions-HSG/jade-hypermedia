package org.hyperagents.jade;

import jade.util.Logger;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GraphBuilder {
  private final static Logger LOGGER = Logger.getJADELogger(HypermediaInterface.class.getName());

  protected final ValueFactory rdf;
  protected final ModelBuilder graphBuilder;

  protected final String localAddress;
  protected final int httpPort;
  protected final String relativePath;

  public GraphBuilder(String localAddress, int httpPort, String relativePath) {
    rdf = SimpleValueFactory.getInstance();
    graphBuilder = new ModelBuilder();

    this.localAddress = localAddress;
    this.httpPort = httpPort;
    this.relativePath = relativePath;
  }

  public String getSubjectIRI() {
    return constructRelativeIRI();
  }

  public String write(RDFFormat format) {
    OutputStream out = new ByteArrayOutputStream();

    graphBuilder.setNamespace("jade", JADE.PREFIX);

    try (out) {
      Rio.write(graphBuilder.build(), out, format,
        new WriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true));
    } catch (UnsupportedRDFormatException | IOException e) {
      LOGGER.log(Logger.SEVERE, e.getMessage());
    }

    return out.toString();
  }

  protected String constructRelativeIRI() {
    return "http://" + localAddress + ":" + httpPort + relativePath;
  }
}
