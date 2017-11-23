package introsde.rest.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.glassfish.jersey.client.ClientConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestClient {

	private static Document doc;
	private static DocumentBuilder builder;
	private static DocumentBuilderFactory domFactory;
	private static ObjectMapper mapper;
	private static PrintStream printJSON;
	private static PrintStream printXML;
	private static Response resp;
	private static String activityId;
	private static String activityType;
	private static String content;
	private static String firstId;
	private static String json;
	private static String lastId;
	private static String request;
	private static String start;
	private static String type;
	private static String xml;
	private static WebTarget service;
	private static XPath xpath;
	private static boolean result;

	private static String clientServerXmlLog = "client-server-xml.log";
	private static String clientServerJsonlLog = "client-server-json.log";

	public static void main(String[] args) throws ParserConfigurationException, XPathExpressionException, SAXException,
			IOException, TransformerException {
		initialize();

		init();
		
		// JSON Requests
		api1JSON();
		printResult();

		api2JSON(firstId);
		printResult();

		api3JSON(firstId);
		printResult();

		String newPersonId = api4JSON();
		printResult();

		api5JSON(newPersonId);
		printResult();

		String[] activityType = api6JSON();
		printResult();

		api7JSON(activityType, firstId);
		printResult();
		api7JSON(activityType, lastId);
		printResult();

		api8JSON(lastId);
		printResult();

		api9JSON(activityType);
		printResult();

		api10JSON(firstId);
		printResult();

		api11JSON(lastId);
		printResult();
		
		// XML Requests
		api1Xml();
		printResult();
		
		api2XML(firstId);
		printResult();
		
		api3XML(firstId);
		printResult();
		
		newPersonId = api4XML(); 
		printResult();
		
		api5XML(newPersonId);
		printResult();
		
		activityType = api6XML();
		printResult();
		
		api7XML(activityType, firstId);
		printResult();
		api7XML(activityType, lastId); 
		printResult();
		
		api8XML(lastId);
		printResult();
		
		api9XML(activityType);
		printResult();
		
		api10XML(firstId); 
		printResult();
		
		api11XML(lastId);
		printResult();
	}

	private static URI getBaseURI() {
		//return UriBuilder.fromUri("http://127.0.1.1:5900/introsde").build();
		return
		UriBuilder.fromUri("https://introsde2017-assign-2-server.herokuapp.com/assignment/").build();
	}
	
	private static void initialize() throws ParserConfigurationException, FileNotFoundException {
		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		service = client.target(getBaseURI());
		System.out.println("Server respond at: " + getBaseURI());
		domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		builder = domFactory.newDocumentBuilder();
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		FileOutputStream filexml = new FileOutputStream(clientServerXmlLog);
		FileOutputStream filejson = new FileOutputStream(clientServerJsonlLog);
		printXML = new PrintStream(filexml);
		printJSON = new PrintStream(filejson);

		mapper = new ObjectMapper();
	}

	private static void printResult() throws TransformerException {
		PrintStream stream = null;
		if (type == MediaType.APPLICATION_XML) {
			stream = printXML;
		} else if (type == MediaType.APPLICATION_JSON) {
			stream = printJSON;
		}
		stream.print(start + request + " Accept: " + type);
		System.out.print(start + request + " Accept: " + type);

		if (content != null) {
			stream.println(" Content-Type: " + content);
			System.out.println(" Content-Type: " + content);
		} else {
			stream.println();
			System.out.println();
		}
		if (result) {
			stream.println("=> Result: OK");
			System.out.println("=> Result: OK");
		} else {
			stream.println("=> Result: ERROR");
			System.out.println("=> Result: ERROR");
		}
		if (resp != null) {
			stream.println("=> HTTP Status: " + resp.getStatus());
			System.out.println("=> HTTP Status: " + resp.getStatus());
			if (type == MediaType.APPLICATION_XML) {
				String xmlOut = printXML(doc);
				stream.println(xmlOut);
				System.out.println(xmlOut);
			} else if (type == MediaType.APPLICATION_JSON) {
				String jsonOut = printJSON();
				stream.println(jsonOut);
				System.out.println(jsonOut);
			}
		} else {
			stream.println("=> HTTP Status: NO RESPONSE");
			System.out.println("=> HTTP Status: NO RESPONSE");
		}
	}
	
	private static String printXML(Document doc) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		// initialize StreamResult with File object to save to file
		StreamResult result1 = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result1);
		String xmlString = result1.getWriter().toString();
		return xmlString;
	}

	private static String printJSON() {
		Object obj;
		String jsonString = null;
		try {
			if (!json.isEmpty()) {
				obj = mapper.readValue(json, Object.class);
				jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
	}
	
	private static void init() {
		// GET Request#0 --- GET BASEURL/person/init
		request = "person/init";
		resp = service.path(request).request().get();
		System.out.println(resp.readEntity(String.class));
	}

	public static void api1Xml() throws SAXException, IOException, XPathExpressionException {
		// GET Request#1 --- GET BASEURL/person
		// Accept: application/xml
		start = "Request #1: GET /";
		request = "person";
		type = MediaType.APPLICATION_XML;
		content = null;
		result = false;

		resp = service.path(request).request().accept(type).get();

		xml = resp.readEntity(String.class);
		doc = builder.parse(new InputSource(new StringReader(xml)));

		XPathExpression expr = xpath.compile("//*");
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		if (nodes.getLength() > 4)
			result = true;

		// First id
		expr = xpath.compile("//person[1]/idPerson");
		Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
		firstId = node.getTextContent();

		// Last id
		expr = xpath.compile("//person[last()]/idPerson");
		node = (Node) expr.evaluate(doc, XPathConstants.NODE);
		lastId = node.getTextContent();
	}

	private static void api2XML(String id)
			throws SAXException, IOException, XPathExpressionException, TransformerException {
		// GET Request #2 --- GET BASEURL/person/first_id
		// Accept: application/xml
		start = "Request #2: GET /";
		request = "person/" + id;
		type = MediaType.APPLICATION_XML;
		content = null;
		result = false;

		resp = service.path(request).request().accept(type).get();

		xml = resp.readEntity(String.class);
		if (!xml.isEmpty())
			doc = builder.parse(new InputSource(new StringReader(xml)));

		if (resp.getStatus() == 200 || resp.getStatus() == 202)
			result = true;
	}

	private static void api3XML(String first_id)
			throws SAXException, IOException, XPathExpressionException, TransformerException {
		// PUT Request #3 --- PUT BASEURL/person/first_id
		// Accept: application/xml
		start = "Request #3: PUT /";
		request = "person/" + first_id;
		type = MediaType.APPLICATION_XML;
		content = MediaType.APPLICATION_XML;
		result = false;

		String newName = "Massimo";
		String requestBody = "<person>" + "<idPerson>" + first_id + "</idPerson>" + "<firstname>" + newName
				+ "</firstname>" + "</person>";
		resp = service.path(request).request().accept(type).put(Entity.entity(requestBody, content));
		xml = resp.readEntity(String.class);
		doc = builder.parse(new InputSource(new StringReader(xml)));

		XPathExpression expr = xpath.compile("//firstname");
		String firstname = (String) expr.evaluate(doc, XPathConstants.STRING);
		if (newName.equals(firstname))
			result = true;
	}

	private static String api4XML() throws SAXException, IOException, XPathExpressionException, TransformerException {
		// POST Request #4 --- POST BASEURL/person
		// Accept: application/xml
		start = "Request #4: POST /";
		request = "person";
		type = MediaType.APPLICATION_XML;
		content = MediaType.APPLICATION_XML;
		result = false;
		String newPersonId = "";

		String requestBody = "<person>" + "<firstname>Antonio</firstname>" + "<lastname>Sierte</lastname>"
				+ "<birthdate>1992-10-17</birthdate>" + "<activitiesPreference>" + "<activity>" + "<name>Saxing</name>"
				+ "<description>Playing music with Sax</description>" + "<idActivityType>1</idActivityType>" + // Social
				"<place>City Centre Trento</place>" + "<startdate>2017-10-13T10:50:00.0</startdate>" + "</activity>"
				+ "</activitiesPreference>" + "</person>";

		resp = service.path(request).request().accept(type).post(Entity.entity(requestBody, content));
		xml = resp.readEntity(String.class);
		doc = builder.parse(new InputSource(new StringReader(xml)));

		XPathExpression expr = xpath.compile("//idPerson");
		newPersonId = (String) expr.evaluate(doc, XPathConstants.STRING);
		if ((resp.getStatus() == 200 || resp.getStatus() == 201 || resp.getStatus() == 202) && !newPersonId.isEmpty())
			result = true;
		return newPersonId;

	}

	private static void api5XML(String id)
			throws SAXException, IOException, XPathExpressionException, TransformerException {
		// DELETE Request #5 --- DELETE BASEURL/person/id
		// Accept: application/xml
		start = "Request #5: DELETE /";
		request = "person/" + id;
		type = MediaType.APPLICATION_XML;
		content = null;
		doc = null;

		Response this_resp = service.path(request).request().accept(type).delete();
		try {
			api2XML(id); //Tomcat return a 404 HTML page that mess up the api2JSON
		} catch (Exception ex) {}

		// reset variable
		start = "Request #5: DELETE /";
		request = "person/" + id;
		type = MediaType.APPLICATION_XML;
		content = null;
		result = false;

		if (resp.getStatus() == 404)
			result = true;
		resp = this_resp;
	}

	private static String[] api6XML() throws SAXException, IOException, XPathExpressionException, TransformerException {
		// GET Request #6 --- GET BASEURL/activity_types
		// Accept: application/xml
		start = "Request #6: GET /";
		request = "activity_types";
		type = MediaType.APPLICATION_XML;
		content = null;
		// result = false;

		resp = service.path(request).request().accept(type).get();

		xml = resp.readEntity(String.class);
		doc = builder.parse(new InputSource(new StringReader(xml)));

		XPathExpression expr = xpath.compile("//activity_type");
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		if (nodes.getLength() > 2)
			result = true;
		String[] activitiesType = new String[nodes.getLength()];
		for (int i = 0; i < nodes.getLength(); i++) {
			activitiesType[i] = nodes.item(i).getTextContent();
		}
		return activitiesType;
	}

	private static void api7XML(String[] vector, String id)
			throws SAXException, IOException, XPathExpressionException, TransformerException {
		// GET Request #7 --- GET BASEURL/person/{id}/{activity_type}
		// Accept: application/xml
		start = "Request #7: GET /";
		request = "person/" + id + "/";
		type = MediaType.APPLICATION_XML;
		content = null;
		result = false;
		Response this_res = null;
		String this_req = null;
		String this_xml = null;
		Document this_doc = null;

		for (int i = 0; i < vector.length; i++) {
			String request1 = request + vector[i];

			resp = service.path(request1).request().accept(type).get();
			if (resp.getStatus() == 200) {
				result = true;
				xml = resp.readEntity(String.class);
				doc = builder.parse(new InputSource(new StringReader(xml)));
				XPathExpression test = xpath.compile("//activities");
				Node nodetest = (Node) test.evaluate(doc, XPathConstants.NODE);
				String resultstring = nodetest.getTextContent();
				if (!resultstring.isEmpty()) {
					XPathExpression expr = xpath.compile("//activityType[1]");
					Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
					expr = xpath.compile("//activity_type");
					node = (Node) expr.evaluate(doc, XPathConstants.NODE);
					activityType = node.getTextContent();
					expr = xpath.compile("//idActivity[1]");
					node = (Node) expr.evaluate(doc, XPathConstants.NODE);
					activityId = node.getTextContent();
					id = firstId;
					this_res = resp;
					this_req = request1;
					this_xml = xml;
					this_doc = doc;
				}

			}

		}
		xml = this_xml;
		resp = this_res;
		request = this_req;
		doc = this_doc;
	}

	private static void api8XML(String id)
			throws SAXException, IOException, XPathExpressionException, TransformerException {
		// GET Request #8 --- GET BASEURL/person/{id}/{activity_type}/{activity_id}
		// Accept: application/xml
		start = "Request #8: GET /";
		request = "person/" + id + "/" + activityType + "/" + activityId;
		type = MediaType.APPLICATION_XML;
		content = null;
		result = false;

		resp = service.path(request).request().accept(type).get();

		xml = resp.readEntity(String.class);
		if (!xml.isEmpty())
			doc = builder.parse(new InputSource(new StringReader(xml)));

		if (resp.getStatus() == 200 || resp.getStatus() == 202)
			result = true;
	}

	private static void api9XML(String[] vector)
			throws SAXException, IOException, XPathExpressionException, TransformerException {
		// POST Request #9 --- POST BASEURL/person/{first_person_id}/{activityType}
		// Accept: application/xml

		/* First call */
		api7XML(vector, firstId);
		XPathExpression expr = xpath.compile("count(//activity)");
		int count = Integer.parseInt((String) expr.evaluate(doc, XPathConstants.STRING));

		start = "Request #9: POST /";
		type = MediaType.APPLICATION_XML;
		content = MediaType.APPLICATION_XML;
		result = false;
		request = "person/" + firstId + "/" + activityType;
		String requestBody = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<activity>"
				+ "<description>Swimming in the river</description>" + "<name>Swimming</name>"
				+ "<place>Adige river</place>" + "<startdate>2017-12-28T08:50:00.0</startdate>" + "</activity>";

		Response this_resp = service.path(request).request().accept(type).post(Entity.entity(requestBody, content));
		String this_xml = this_resp.readEntity(String.class);
		Document this_doc = builder.parse(new InputSource(new StringReader(this_xml)));

		/* Second call */
		api7XML(vector, firstId);
		expr = xpath.compile("count(//activity)");
		int second_count = Integer.parseInt((String) expr.evaluate(doc, XPathConstants.STRING));

		// Reset variable
		start = "Request #9: POST /";
		type = MediaType.APPLICATION_XML;
		content = MediaType.APPLICATION_XML;
		result = false;
		request = "person/" + firstId + "/" + activityType;
		resp = this_resp;
		xml = this_xml;
		doc = this_doc;

		if (count + 1 == second_count)
			result = true;
	}

	private static void api10XML(String id)
			throws SAXException, IOException, XPathExpressionException, TransformerException {
		// PUT Request #10 --- PUT BASEURL/person/{id}/{activity_type}/{activity_id}
		// Accept: application/xml
		// variable
		start = "Request #10: PUT /";
		request = "person/" + id + "/" + activityType + "/" + activityId;
		type = MediaType.APPLICATION_XML;
		content = MediaType.APPLICATION_XML;
		result = false;
		String requestBody = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<activity>"
				+ "<description>Go Carting with Friends</description>" + "<name>Go carting</name>"
				+ "<place>Affi cart centre</place>" + "<startdate>2017-12-28T08:50:00.0</startdate>" + "</activity>";
		Response this_resp = service.path(request).request().accept(type).put(Entity.entity(requestBody, content));
		String this_xml = this_resp.readEntity(String.class);
		Document this_doc = builder.parse(new InputSource(new StringReader(this_xml)));

		api8XML(id);
		XPathExpression expr = xpath.compile("//name");
		String newvalue = (String) expr.evaluate(doc, XPathConstants.STRING);

		// reset variable
		start = "Request #10: PUT /";
		request = "person/" + id + "/" + activityType + "/" + activityId;
		type = MediaType.APPLICATION_XML;
		content = MediaType.APPLICATION_XML;
		result = false;
		resp = this_resp;
		xml = this_xml;
		doc = this_doc;

		if ("Go carting".equals(newvalue))
			result = true;
	}

	private static void api11XML(String id)
			throws SAXException, IOException, XPathExpressionException, TransformerException {
		// GET Request #11 --- GET
		// BASEURL/person/{id}/{activity_type}?before={beforeDate}&after={afterDate}
		// Accept: application/xml
		// variable
		String before = "2017-10-15T00:00:00.0";
		String after = "2017-10-18T00:00:00.0";
		activityType = "Social";
		start = "Request #11: GET /";
		request = "person/" + id + "/" + activityType;
		type = MediaType.APPLICATION_XML;
		content = null;
		result = false;

		resp = service.path(request).queryParam("before", before).queryParam("after", after).request().accept(type)
				.get();
		xml = resp.readEntity(String.class);

		if (!xml.isEmpty()) {
			doc = builder.parse(new InputSource(new StringReader(xml)));
		}

		XPathExpression expr = xpath.compile("count(//activity)");
		int size = Integer.parseInt((String) expr.evaluate(doc, XPathConstants.STRING));

		if (resp.getStatus() == 200 && size > 0)
			result = true;
		request = "person/" + id + "/" + activityType + "?before=" + before + "&after=" + after;
	}

	private static void api1JSON() throws JsonProcessingException, IOException {
		// GET Request #1 --- GET BASEURL/person
		// Accept: application/json
		start = "Request #1: GET /";
		request = "person";
		type = MediaType.APPLICATION_JSON;
		content = null;
		result = false;

		resp = service.path(request).request().accept(type).get();

		json = resp.readEntity(String.class);
		JsonNode nodes = mapper.readTree(json);

		if (nodes.size() > 4)
			result = true;

		// first id
		firstId = nodes.get(0).path("idPerson").asText();

		// last id
		lastId = nodes.get(nodes.size() - 1).path("idPerson").asText();
	}

	private static void api2JSON(String id) {
		// GET Request #2 --- GET BASEURL/person/first_id
		// Accept: application/json
		start = "Request #2: GET /";
		request = "person/" + id;
		type = MediaType.APPLICATION_JSON;
		content = null;
		result = false;

		resp = service.path(request).request().accept(type).get();

		json = resp.readEntity(String.class);

		if (resp.getStatus() == 200 || resp.getStatus() == 202)
			result = true;
	}

	private static void api3JSON(String id) throws JsonProcessingException, IOException {
		// PUT Request #3 --- PUT BASEURL/person/first_id
		// Accept: application/json
		start = "Request #3: PUT /";
		request = "person/" + id;
		type = MediaType.APPLICATION_JSON;
		content = MediaType.APPLICATION_JSON;
		result = false;

		String newName = "Graziano";
		String requestBody = "{" + "\"firstname\": \"" + newName + "\"," + "\"idPerson\": \"" + id + "\"" + "}";

		resp = service.path(request).request().accept(type).put(Entity.entity(requestBody, content));
		json = resp.readEntity(String.class);
		JsonNode node = mapper.readTree(json);

		String firstname = node.path("firstname").asText();
		if (newName.equals(firstname))
			result = true;
	}

	private static String api4JSON() throws JsonProcessingException, IOException {
		// POST Request #4 --- POST BASEURL/person
		// Accept: application/json
		// variable
		start = "Request #4: POST /";
		request = "person";
		type = MediaType.APPLICATION_JSON;
		content = MediaType.APPLICATION_JSON;
		result = false;
		String newperson_id = "";

		String requestBody = "{" + "\"firstname\": \"Enrico\"," + "\"lastname\":\"Chiesa\","
				+ "\"birthdate\":\"1993-02-10\"," + "\"activity\":[" + "{" + "\"name\":\"Climbing\","
				+ "\"description\":\"Mountain Climbing\"," + "\"place\":\"Bondone Mountain\","
				+ "\"startdate\":\"2017-10-10T10:30:00.0\"," + "\"idActivityType\": 3" // Outdoor
				+ "}" + "]" + "}";

		resp = service.path(request).request().accept(type).post(Entity.entity(requestBody, content));
		json = resp.readEntity(String.class);
		JsonNode node = mapper.readTree(json);

		newperson_id = node.path("idPerson").asText();
		if ((resp.getStatus() == 200 || resp.getStatus() == 201 || resp.getStatus() == 202) && !newperson_id.isEmpty())
			result = true;
		return newperson_id;
	}

	private static void api5JSON(String id) {
		// DELETE Request #5 --- DELETE BASEURL/person/id
		// Accept: application/json
		// variable
		start = "Request #5: DELETE /";
		request = "person/" + id;
		type = MediaType.APPLICATION_JSON;
		content = null;
		doc = null;

		Response this_resp = service.path(request).request().accept(type).delete();
		try {
			api2JSON(id); //Tomcat return a 404 HTML page that mess up the api2JSON
		} catch (Exception ex) {}

		// reset variable
		start = "Request #5: DELETE /";
		request = "person/" + id;
		type = MediaType.APPLICATION_JSON;
		content = null;
		result = false;

		if (resp.getStatus() == 404)
			result = true;
		resp = this_resp;
	}

	private static String[] api6JSON() throws JsonProcessingException, IOException {
		// GET Request #6 --- GET BASEURL/activity_types
		// Accept: application/json
		// variable
		start = "Request #6: GET /";
		request = "activity_types";
		type = MediaType.APPLICATION_JSON;
		content = null;
		result = false;

		resp = service.path(request).request().accept(type).get();

		json = resp.readEntity(String.class);

		JsonNode node = mapper.readTree(json).get("activityTypes");

		int size = node.size();
		if (size > 2)
			result = true;

		String[] activityTypes = new String[size];
		for (int i = 0; i < size; i++) {
			activityTypes[i] = node.get(i).textValue();
		}
		return activityTypes;
	}

	private static void api7JSON(String[] vector, String id) throws JsonProcessingException, IOException {
		// GET Request #7 --- GET BASEURL/person/{id}/{activity_types}
		// Accept: application/json
		start = "Request #7: GET /";
		request = "person/" + id + "/";
		type = MediaType.APPLICATION_JSON;
		content = null;
		result = false;
		Response this_res = null;
		String this_req = null;
		String this_json = null;

		for (int i = 0; i < vector.length; i++) {
			String request1 = request + vector[i];

			resp = service.path(request1).request().accept(type).get();
			if (resp.getStatus() == 200) {
				result = true;
				json = resp.readEntity(String.class);
				JsonNode node = mapper.readTree(json);
				if (!"[]".equals(json)) {
					activityType = node.get(0).get("activityType").get("activity_type").textValue();
					activityId = node.get(0).path("idActivity").asText();
					id = firstId;
					this_res = resp;
					this_req = request1;
					this_json = json;
				}
			}
		}
		resp = this_res;
		request = this_req;
		json = this_json;
	}

	private static void api8JSON(String id) {
		// GET Request #8 --- GET BASEURL/person/{id}/{activity_type}/{activity_id}
		// Accept: application/json
		// variable
		start = "Request #8: GET /";
		request = "person/" + id + "/" + activityType + "/" + activityId;
		type = MediaType.APPLICATION_JSON;
		content = null;
		result = false;

		resp = service.path(request).request().accept(type).get();

		json = resp.readEntity(String.class);

		if ((resp.getStatus() == 200 || resp.getStatus() == 202) && !json.isEmpty())
			result = true;
	}

	private static void api9JSON(String[] vector) throws JsonProcessingException, IOException {
		// POST Request #9 --- POST BASEURL/person/{first_person_id}/{activityType}
		// Accept: application/json

		/* First call */
		api7JSON(vector, firstId);
		JsonNode node = mapper.readTree(json);
		int count = node.size();

		start = "Request #9: POST /";
		type = MediaType.APPLICATION_JSON;
		content = MediaType.APPLICATION_JSON;
		result = false;
		request = "person/" + firstId + "/" + activityType;
		String requestBody = "{" + "\"name\" : \"Swimming\"," + "\"description\" : \"Swimming in the river\" ,"
				+ "\"place\" : \"Adige river\"," + "\"startdate\" : \"2017-12-28T08:50:00.0\"" + "}";

		Response this_resp = service.path(request).request().accept(type).post(Entity.entity(requestBody, content));
		String this_json = this_resp.readEntity(String.class);

		/* Second call */
		api7JSON(vector, firstId);
		node = mapper.readTree(json);
		int second_count = node.size();

		// reset variable
		start = "Request #9: POST /";
		type = MediaType.APPLICATION_JSON;
		content = MediaType.APPLICATION_JSON;
		result = false;
		request = "person/" + firstId + "/" + activityType;
		resp = this_resp;
		json = this_json;

		if (count + 1 == second_count)
			result = true;
	}

	private static void api10JSON(String id) throws JsonProcessingException, IOException {
		// PUT Request #10 --- PUT BASEURL/person/{id}/{activity_type}/{activity_id}
		// Accept: application/json
		start = "Request #10: PUT /";
		request = "person/" + id + "/" + activityType + "/" + activityId;
		type = MediaType.APPLICATION_JSON;
		content = MediaType.APPLICATION_JSON;
		result = false;

		String requestBody = "{" + "\"name\" : \"Go carting\"," + "\"description\" : \"Go carting with friends\" ,"
				+ "\"place\" : \"Affi centre\"," + "\"startdate\" : \"2017-12-28T08:50:00.0\"" + "}";
		Response this_resp = service.path(request).request().accept(type).put(Entity.entity(requestBody, content));
		String this_json = this_resp.readEntity(String.class);

		api8JSON(id);
		JsonNode node = mapper.readTree(json);
		String newvalue = "";
		if (node != null)
			newvalue = node.path("name").asText();

		// reset variable
		start = "Request #10: PUT /";
		request = "person/" + id + "/" + activityType + "/" + activityId;
		type = MediaType.APPLICATION_JSON;
		content = MediaType.APPLICATION_JSON;
		result = false;
		resp = this_resp;
		json = this_json;

		if ("Go carting".equals(newvalue))
			result = true;
	}

	private static void api11JSON(String id) throws JsonProcessingException, IOException {
		// GET Request #11 --- GET
		// BASEURL/person/{id}/{activity_type}?before={beforeDate}&after={afterDate}
		// Accept: application/json
		String before = "2017-10-15T00:00:00.0";
		String after = "2017-10-18T00:00:00.0";
		activityType = "Social";
		start = "Request #11: GET /";
		request = "person/" + id + "/" + activityType;
		type = MediaType.APPLICATION_JSON;
		content = null;
		result = false;

		resp = service.path(request).queryParam("before", before).queryParam("after", after).request().accept(type)
				.get();
		json = resp.readEntity(String.class);
		JsonNode node = null;
		int size = 0;
		if (!json.isEmpty()) {
			node = mapper.readTree(json);
			size = node.size();
		}

		if (resp.getStatus() == 200 && size > 0)
			result = true;
		request = "person/" + id + "/" + activityType + "?before=" + before + "&after=" + after;
	}
}
