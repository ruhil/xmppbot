package de.raion.xmppbot.plugin;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.raion.xmppbot.XmppBot;
import de.raion.xmppbot.XmppContext;
import de.raion.xmppbot.command.JiraConfig;
import de.raion.xmppbot.filter.MessageBodyMatchesFilter;

/**
 * listen for messages with jira related issues and posts summary and links into chat.
 *
 */
@MessageListenerPlugin(name="jira-issues", description="provides summary and link to jira issues when mentioned in chat")
public class JiraIssuePlugin extends AbstractMessageListenerPlugin<JiraIssuePlugin> {

	private static Logger log = LoggerFactory.getLogger(JiraIssuePlugin.class);

	private ObjectMapper mapper;

	private MessageBodyMatchesFilter acceptFilter;;

	private Pattern pattern;

	private JiraConfig config;


	/**
	 * constructor
	 * @param aXmppBot reference
	 */
	public JiraIssuePlugin(XmppBot aXmppBot) {
		super(aXmppBot);

		mapper = new ObjectMapper();
		acceptFilter = new MessageBodyMatchesFilter(""); // correct initialization init
		init();
	}


	/**
	 * reloads configuration
	 */
	public void updateConfiguration() {
		init();
	}


	@Override
	public PacketFilter getAcceptFilter() {
		return acceptFilter;
	}


	@Override
	public void processMessage(XmppContext xmppContext, Chat chat,
			Message message) {
		processMessage(xmppContext, message);
	}


	@Override
	public void processMessage(XmppContext xmppContext, MultiUserChat muc,
			Message message) {
		processMessage(xmppContext, message);
	}


	/**
	 * retrieves key and name of jira projects
	 * @param projectsUri the uri of the projects resource
	 * @return key and name of jira projects
	 */
	public Map<String, String> getProjects(URI projectsUri) {

		TreeMap<String, String> map = new TreeMap<String, String>();

		try {
			JsonNode rootNode = mapper.readValue(projectsUri.toURL(), JsonNode.class);
			List<String> keyList = rootNode.findValuesAsText("key");
			List<String> nameList = rootNode.findValuesAsText("name");

			for(int i=0; i<keyList.size(); i++) {
				map.put(keyList.get(i), nameList.get(i));
			}
		}catch(Exception e) {
			log.error("getProjects(URI)", e);
			return map;
		}
		return map;
	}


	/**
	 * creates a matching pattern
	 * @param keySet source
	 * @return pattern
	 */
	public String createMatchingPattern(Set<String> keySet) {
		StringBuilder builder = new StringBuilder("(");

		Iterator<String> it = keySet.iterator();

		while(it.hasNext()) {
			builder.append(it.next()).append("-\\d+");
			if(it.hasNext()) {
				builder.append("|");
			}
		}
		builder.append(")");

		Pattern aPattern = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
		return aPattern.pattern();
	}


	private void processMessage(XmppContext xmppContext, Message message) {

		Matcher matcher = pattern.matcher(message.getBody());

		while(matcher.find()) {

			String issue = matcher.group();

			try {

				URI issueUri = config.getIssueURI(issue);
				JsonNode issueNode = mapper.readValue(issueUri.toURL(), JsonNode.class);
				String issueSummary = issueNode.findValue("summary").textValue();

				StringBuilder builder = new StringBuilder();
				builder.append("[").append(issue).append("] - ");
				builder.append(issueSummary).append(" : ");
				builder.append(config.getIssueBrowseURI(issue).toString()).append("\n");

				xmppContext.println(builder.toString());

			} catch (Exception e) {
				log.error("processMessage(XmppContext, Message) - {}", e.getMessage());
			}
		}
	}


	private void init() {

		config  = getContext().loadConfig(JiraConfig.class);

		String regex = config.getMatchingPattern();

		if(regex != null) {
			acceptFilter.setPattern(regex);
			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			log.info("using pattern '{}' for matching", regex);
		}
	}
}
