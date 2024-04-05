package org.hyperagents.jade.graphs;

import ch.unisg.ics.interactions.hmas.core.hostables.Agent;
import ch.unisg.ics.interactions.hmas.core.hostables.HypermediaMASPlatform;
import ch.unisg.ics.interactions.hmas.core.hostables.ProfiledResource;
import ch.unisg.ics.interactions.hmas.interaction.shapes.IntegerSpecification;
import ch.unisg.ics.interactions.hmas.interaction.shapes.QualifiedValueSpecification;
import ch.unisg.ics.interactions.hmas.interaction.shapes.ValueSpecification;
import ch.unisg.ics.interactions.hmas.interaction.signifiers.ActionSpecification;
import ch.unisg.ics.interactions.hmas.interaction.signifiers.Form;
import ch.unisg.ics.interactions.hmas.interaction.signifiers.Signifier;
import jade.domain.FIPAAgentManagement.APService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.hyperagents.jade.platform.PlatformState;
import org.hyperagents.jade.platform.WebAID;
import org.hyperagents.jade.platform.WebAPDescription;
import org.hyperagents.jade.vocabs.FIPA;
import org.hyperagents.jade.vocabs.HMAS;
import org.hyperagents.jade.vocabs.JADE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * Constructs an <a href="https://purl.org/hmas/ResourceProfile">hmas:ResourceProfile</a> of a JADE agent.
 */
public class AgentProfileBuilder extends ResourceProfileBuilder {
  private final WebAID agentID;
  private final DFAgentDescription agentDFD;

  private final Agent.Builder builder;

  /**
   * Constructs an agent profile builder. Unlike {@link PlatformProfileBuilder}, this builder uses as
   * the HTTP authority for exposed IRIs the address specified by the JADE container identifier passed
   * as a parameter, which identifies the agent's container. This is necessary for distributed deployments.
   * @param agentID the identifier of the JADE agent
   */
  public AgentProfileBuilder(WebAID agentID, DFAgentDescription dfd) {
    super(agentID.getIRI());

    this.agentID = agentID;
    this.agentDFD = dfd;

    builder = new Agent.Builder().setIRIAsString(agentID.getAgentIRI());
  }

  @Override
  protected ProfiledResource build() {
    return builder.build();
  }

  /**
   * Adds any agent metadata (e.g., local name, fully qualified name).
   * @return this instance of agent graph builder (fluid API)
   */
  public AgentProfileBuilder addMetadata() {
    ValueFactory rdf = SimpleValueFactory.getInstance();

    builder.addTriple(FIPA.agentName, rdf.createLiteral(agentID.getName()));
    builder.addTriple(JADE.localName, rdf.createLiteral(agentID.getLocalName()));

    PlatformState state = PlatformState.getInstance();
    String mainEndpoint  = state.getMainContainerEndpoint();
    WebAPDescription apDesc = state.getAPDescription();

    if (apDesc != null) {
      if (mainEndpoint != null) {
        apDesc = new WebAPDescription(apDesc.getApDescription(), mainEndpoint);
      }

      builder.addHMASPlatform(new HypermediaMASPlatform.Builder().setIRIAsString(apDesc.getPlatformIRI())
        .build());
    }

    builder.addTriple(FIPA.homeAgentPlatform, rdf.createLiteral(agentID.getAID().getHap()));
    builder.addTriple(HMAS.isContainedIn, rdf.createIRI(agentID.getContainerIRI()));

    return this;
  }

  /**
   * Adds all message transport addresses defined in this agent's FIPA Agent Identifier. The
   * addresses are exposed via signifiers.
   * @return this instance of agent profile builder (fluid API)
   */
  @SuppressWarnings("unchecked")
  public AgentProfileBuilder addAddresses() {
    addSignifiersForTransportAddresses(agentID.getAllAddresses());
    return this;
  }

  public AgentProfileBuilder addProtocolSignifiers() {
    if (agentDFD == null) {
      return this;
    }

    Iterator<String> it = agentDFD.getAllProtocols();
//    while (it.hasNext()) {
//      System.out.println("Supports protocol: " + it.next());
      addSignifiersForProtocol(it.next());
//    }

    return this;
  }

  private void addSignifiersForProtocol(String protocol) {
    Iterator<APService> services = PlatformState.getInstance().getAPDescription().getAllAPServices();
    Iterator<String> addresses = services.next().getAllAddresses();

    while (addresses.hasNext()) {
      String address = addresses.next();
      int count = 0;

      Form form = new Form.Builder(address)
        .setContentType("multipart-mixed")
        .addSubProtocol("fipa.mts.mtp.http.std")
        .setIRIAsString(getDocumentIRI() + "#form" + ++count)
        .build();

      QualifiedValueSpecification inputSpecification = new QualifiedValueSpecification.Builder()
        .addSemanticType(FIPA.ACLMessage.stringValue())
        .setRequired(true)
        .addPropertySpecification(FIPA.hasLanguage.stringValue(),
          new ValueSpecification.Builder()
            .setValue(FIPA.FIPASL)
            .setRequired(true)
            .build())
        .addPropertySpecification(FIPA.hasSender.stringValue(),
          new ValueSpecification.Builder()
            .setRequired(true)
            .build())
        .addPropertySpecification(FIPA.hasReceiver.stringValue(),
          new ValueSpecification.Builder()
            .setValueAsString(agentID.getAgentIRI())
            .setRequired(true)
            .build())
        .addPropertySpecification(FIPA.hasContent.stringValue(),
          new QualifiedValueSpecification.Builder()
            .setRequired(true)
            .build())
        .addPropertySpecification(FIPA.hasPerformative.stringValue(),
          new ValueSpecification.Builder()
            .setValue(FIPA.Request)
            .setRequired(true)
            .build())
        .build();

      ValueFactory rdf = SimpleValueFactory.getInstance();
      ModelBuilder builder = new ModelBuilder();

      BNode outerBnode = rdf.createBNode("outerbnode");
      BNode innerBnode = rdf.createBNode("innerbnode");

      IRI actionSpecIRI = rdf.createIRI(this.getDocumentIRI() + "#requestAction");
      builder.add(actionSpecIRI, rdf.createIRI("http://www.w3.org/ns/shacl#property"),
        outerBnode);
      builder.add(outerBnode, rdf.createIRI("http://www.w3.org/ns/shacl#path"),
        rdf.createIRI("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#isActionIncludedIn"));
      builder.add(outerBnode, rdf.createIRI("http://www.w3.org/ns/shacl#qualifiedMinCount"), rdf.createLiteral(0));
      builder.add(outerBnode, rdf.createIRI("http://www.w3.org/ns/shacl#qualifiedMaxCount"), rdf.createLiteral(1));
      builder.add(outerBnode, rdf.createIRI("http://www.w3.org/ns/shacl#qualifiedValueShape"), innerBnode);

      builder.add(innerBnode, rdf.createIRI("http://www.w3.org/ns/shacl#class"),
        FIPA.RequestInteractionProtocolEnactment);
      builder.add(innerBnode, rdf.createIRI("http://www.w3.org/ns/shacl#class"),
        rdf.createIRI("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#PlanExecution"));

      builder.setNamespace("fipa", FIPA.PREFIX);
      builder.setNamespace("sh", "http://www.w3.org/ns/shacl#");
      builder.setNamespace("dul", "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#");

      addSignifier(new Signifier.Builder(new ActionSpecification.Builder(form)
          .setIRI(actionSpecIRI)
          .setInputSpecification(inputSpecification)
          .build())
        .addGraph(builder.build())
        .build()
      );
    }
  }

  private String writeToString(Model model) {
    OutputStream out = new ByteArrayOutputStream();

    try {
      Rio.write(model, out, RDFFormat.TURTLE,
        new WriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true));
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, e.getMessage());
      }
    }

    return out.toString();
  }
}
