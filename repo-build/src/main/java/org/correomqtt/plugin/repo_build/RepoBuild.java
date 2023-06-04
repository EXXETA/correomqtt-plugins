package org.correomqtt.plugin.repo_build;

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
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RepoBuild {
    public static void main(String[] args)
            throws ParserConfigurationException, IOException, SAXException, NoSuchAlgorithmException {

        List<RepoPluginInfoDTO> list = getRepoPluginInfoList();

        System.out.println("Writing local repo to local-repo.json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(Paths.get("local-repo.json").toFile(),list);
    }

    private static List<RepoPluginInfoDTO> getRepoPluginInfoList()
            throws ParserConfigurationException, IOException, SAXException, NoSuchAlgorithmException {
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
            String moduleName = modules.item(i).getTextContent();
            if (moduleName.equals("repo-build")) {
                continue;
            }
            RepoPluginInfoDTO repoPluginInfoDTO = getRepoPluginInfoDto(moduleName, version);
            repoPluginInfoDTOList.add(repoPluginInfoDTO);
        }

        return repoPluginInfoDTOList;

    }

    private static RepoPluginInfoDTO getRepoPluginInfoDto(String moduleName, String version)
            throws ParserConfigurationException, IOException, SAXException {

        System.out.println("Generating Plugin Info for " + moduleName);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(moduleName + File.separator + "pom.xml"));
        doc.getDocumentElement().normalize();

        Element project = (Element) doc.getElementsByTagName("project").item(0);

        Element propertiesElement = (Element) project.getElementsByTagName("properties").item(0);

        File f = new File(moduleName + File.separator + "target");
        File[] files = f.listFiles((dir, name) -> name.startsWith(moduleName) && name.endsWith("jar"));

        if (files == null || files.length != 1) {
            throw new RuntimeException("None or more than one jars found.");
        }

        String jarFileName = files[0].getName();

        String sha512sum = DigestUtils.sha512Hex(
                new FileInputStream(moduleName + File.separator + "target" + File.separator + jarFileName));

        String compatibleVersionsString = readTagContentSafe(propertiesElement, "plugin.compatibleCorreoVersions");
        List<String> compatibleVersions = Arrays.stream(compatibleVersionsString.split(","))
                .map(String::trim)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        RepoPluginInfoDTO.PluginRelease release = RepoPluginInfoDTO.PluginRelease.builder()
                .url("file://" + files[0].getAbsolutePath())
                .requires("")
                .date(LocalDateTime.now())
                .sha512sum(sha512sum)
                .version(version)
                .compatibleCorreoVersions(compatibleVersions)
                .build();

        List<RepoPluginInfoDTO.PluginRelease> releaseList = new ArrayList<>();
        releaseList.add(release);

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