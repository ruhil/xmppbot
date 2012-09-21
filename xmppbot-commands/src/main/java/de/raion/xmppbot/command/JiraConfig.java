package de.raion.xmppbot.command;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@SuppressWarnings("javadoc")
public class JiraConfig {



	private String jiraDomain;

	private String projectPath = "/rest/api/latest/project";

	private String issuePath = "/rest/api/latest/issue";

	private String browsePath = "/browse";

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public String getIssuePath() {
		return issuePath;
	}

	public void setIssuePath(String issuePath) {
		this.issuePath = issuePath;
	}

	public String getBrowsePath() {
		return browsePath;
	}

	public void setBrowsePath(String browsePath) {
		this.browsePath = browsePath;
	}

	private Map<String, String> projects;

	private String matchingPattern;



	public String getJiraDomain() {
		return jiraDomain;
	}

	public void setJiraDomain(String jiraDomain) {
		this.jiraDomain = jiraDomain;
	}

	public URI getProjectURI() throws URISyntaxException {
		return new URI("https", jiraDomain, projectPath, null);
	}

	public void setProjectURI(String dummy) {};

	public void setProjects(Map<String, String> projectMap) {
		projects = projectMap;

	}

	public Map<String, String> getProjects() {
		return projects;
	}

	public void setMatchingPattern(String aPattern) {
		matchingPattern = aPattern;
	}

	public String getMatchingPattern() {
		return matchingPattern;
	}

	public URI getIssueURI(String issue) throws URISyntaxException {
		return new URI("https", jiraDomain, issuePath+"/"+issue, null );
	}

	public URI getIssueBrowseURI(String issue) throws URISyntaxException {
		return new URI("https", jiraDomain, browsePath+"/"+issue, null);
	}
}
