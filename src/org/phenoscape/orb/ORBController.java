package org.phenoscape.orb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.input.DOMBuilder;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.OBOClassImpl;
import org.obo.datamodel.impl.OBORestrictionImpl;
import org.phenoscape.controller.PhenexController;
import org.xml.sax.SAXException;

public class ORBController {

	private static final String SERVICE = "http://rest.bioontology.org/bioportal/provisional";
	private static final String APIKEY = "37697970-f916-40fe-bfeb-46aadbd07dba";
	private final PhenexController controller;


	public ORBController(PhenexController controller) {
		this.controller = controller;
	}

	public void runORBTermRequest() {
		final NewTermRequestPanel panel = new NewTermRequestPanel(this.controller);
		panel.init();
		panel.setSize(400, 250);
		final int result = JOptionPane.showConfirmDialog(null, panel, "Submit new term request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			final ORBTerm requestedTerm = panel.getTerm();
			try {
				this.requestTerm(requestedTerm);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private OBOClass requestTerm(ORBTerm term) throws ClientProtocolException, IOException, ParserConfigurationException, IllegalStateException, SAXException {
		final OBOSession session = this.controller.getOntologyController().getOBOSession();
		final HttpPost post = new HttpPost(SERVICE);
		post.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
		post.setEntity(this.createPostEntity(term));
		final DefaultHttpClient client = new DefaultHttpClient();
		final HttpResponse response = new DefaultHttpClient().execute(post);
		client.getConnectionManager().shutdown();
		final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		final Document xmlDoc = new DOMBuilder().build(docBuilder.parse(response.getEntity().getContent()));
		final String termID = xmlDoc.getRootElement().getChild("data").getChild("classBean").getChild("id").getValue();
		final OBOClass newTerm = new OBOClassImpl(termID);
		newTerm.setName(term.getLabel());
		newTerm.setDefinition(term.getDefinition());
		if (term.getParent() != null) {
			newTerm.addParent(new OBORestrictionImpl(newTerm, term.getParent(), (OBOProperty)(session.getObject("OBO_REL:is_a"))));
		}
		if (session.getNamespace("bioportal_provisional") == null) {
			session.addNamespace(new Namespace("bioportal_provisional"));
		}
		newTerm.setNamespace(session.getNamespace("bioportal_provisional"));
		session.addObject(newTerm);
		this.controller.getOntologyController().invalidateAllTermSets();
		return newTerm;
	}

	private HttpEntity createPostEntity(ORBTerm term) throws UnsupportedEncodingException {
		final List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("apikey", APIKEY));
		values.add(new BasicNameValuePair("preferredname", term.getLabel()));
		values.add(new BasicNameValuePair("definition", term.getDefinition()));
		values.add(new BasicNameValuePair("submittedby", "39814"));
		return new UrlEncodedFormEntity(values);
	}

	@SuppressWarnings("unused")
	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
