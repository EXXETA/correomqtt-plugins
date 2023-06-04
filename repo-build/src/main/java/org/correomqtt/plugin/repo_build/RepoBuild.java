package org.correomqtt.plugin.repo_build;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class RepoBuild {
    public static void main(String[] args)
            throws ParserConfigurationException, IOException, SAXException {

        Properties properties = System.getProperties();

        String repoBaseUrl = properties.getProperty("repoBaseUrl");
        String keepOldReleasesFromUrl = properties.getProperty("keepOldReleasesFromUrl");

        Map<String, RepoPluginInfoDTO> oldPluginInfo = new HashMap<>();

        if (keepOldReleasesFromUrl != null) {
            oldPluginInfo = getOldPluginInfo(keepOldReleasesFromUrl);
        }

        List<RepoPluginInfoDTO> list = getRepoPluginInfoList(repoBaseUrl, oldPluginInfo);

        System.out.println("Writing local repo to local-repo.json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(Paths.get("local-repo.json").toFile(), list);
    }

    private static Map<String, RepoPluginInfoDTO> getOldPluginInfo(String keepOldReleasesFromUrl)
            throws IOException {
        Map<String, RepoPluginInfoDTO> pluginInfos = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode pluginsNode = objectMapper.readTree(new URL(keepOldReleasesFromUrl));
        Iterator<JsonNode> plugins = pluginsNode.elements();
        while (plugins.hasNext()) {
            JsonNode plugin = plugins.next();
            RepoPluginInfoDTO pluginInfo = objectMapper.treeToValue(plugin, RepoPluginInfoDTO.class);
            pluginInfos.put(pluginInfo.getId(), pluginInfo);
        }
        return pluginInfos;
    }

    private static List<RepoPluginInfoDTO> getRepoPluginInfoList(String repoBaseUrl,
                                                                 Map<String, RepoPluginInfoDTO> oldPluginInfo)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File("pom.xml"));
        doc.getDocumentElement().normalize();

        Element project = (Element) doc.getElementsByTagName("project").item(0);
        Element modulesElement = (Element) project.getElementsByTagName("modules").item(0);
        NodeList modules = modulesElement.getElementsByTagName("module");

        Element versionElement = (Element) project.getElementsByTagName("version").item(0);
        String version = versionElement.getTextContent();

        List<RepoPluginInfoDTO> repoPluginInfoDTOList = new ArrayList<>();

        for (int i = 0; i < modules.getLength(); i++) {
            String moduleId = modules.item(i).getTextContent();
            if (moduleId.equals("repo-build")) {
                continue;
            }
            RepoPluginInfoDTO repoPluginInfoDTO = getRepoPluginInfoDto(moduleId,
                    version,
                    repoBaseUrl,
                    oldPluginInfo.get(moduleId));
            repoPluginInfoDTOList.add(repoPluginInfoDTO);
        }

        return repoPluginInfoDTOList;

    }

    private static RepoPluginInfoDTO getRepoPluginInfoDto(String moduleId,
                                                          String version,
                                                          String repoBaseUrl,
                                                          RepoPluginInfoDTO oldPluginInfo)
            throws ParserConfigurationException, IOException, SAXException {

        System.out.println("Generating Plugin Info for " + moduleId);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(moduleId + File.separator + "pom.xml"));
        doc.getDocumentElement().normalize();

        Element project = (Element) doc.getElementsByTagName("project").item(0);

        Element propertiesElement = (Element) project.getElementsByTagName("properties").item(0);

        File f = new File(moduleId + File.separator + "target");
        File[] files = f.listFiles((dir, name) -> name.startsWith(moduleId) && name.endsWith("jar"));

        if (files == null || files.length != 1) {
            throw new RuntimeException("None or more than one jars found.");
        }

        String jarFileName = files[0].getName();

        String sha512sum = DigestUtils.sha512Hex(
                new FileInputStream(moduleId + File.separator + "target" + File.separator + jarFileName));

        String compatibleVersionsString = readTagContentSafe(propertiesElement, "plugin.compatibleCorreoVersions");
        List<String> compatibleVersions = Arrays.stream(compatibleVersionsString.split(","))
                .map(String::trim)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        String url;

        if (repoBaseUrl != null) {
            url = repoBaseUrl + jarFileName;
        } else {
            url = "file://" + files[0].getAbsolutePath();
        }

        List<RepoPluginInfoDTO.PluginRelease> releaseList = oldPluginInfo
                .getReleases()
                .stream()
                .filter(r -> !r.getVersion().equals(version))
                .distinct()
                .collect(Collectors.toList());
        releaseList.add(RepoPluginInfoDTO.PluginRelease.builder()
                .url(url)
                .requires("")
                .date(LocalDateTime.now().toLocalDate())
                .sha512sum(sha512sum)
                .version(version)
                .compatibleCorreoVersions(compatibleVersions)
                .build());

        return RepoPluginInfoDTO.builder()
                .id(readTagContentSafe(propertiesElement, "plugin.id"))
                .name(readTagContentSafe(propertiesElement, "plugin.name"))
                .description(readTagContentSafe(propertiesElement, "plugin.description"))
                .provider(readTagContentSafe(propertiesElement, "plugin.provider"))
                .repositoryId(readTagContentSafe(propertiesElement, "plugin.repositoryId"))
                .projectUrl(readTagContentSafe(propertiesElement, "plugin.projectUrl"))
                .releases(releaseList)
                .build();
    }

    private static String readTagContentSafe(Element element, String tag) {
        NodeList elements = element.getElementsByTagName(tag);
        if (elements.getLength() == 0) {
            throw new RuntimeException(tag + " is missing");
        }
        return elements.item(0).getTextContent();
    }
}