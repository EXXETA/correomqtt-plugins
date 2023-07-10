package org.correomqtt.plugins.systopic.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.correomqtt.business.dispatcher.SubscribeDispatcher;
import org.correomqtt.business.dispatcher.SubscribeObserver;
import org.correomqtt.business.model.ConnectionConfigDTO;
import org.correomqtt.business.model.MessageDTO;
import org.correomqtt.business.model.Qos;
import org.correomqtt.business.model.SubscriptionDTO;
import org.correomqtt.business.provider.SettingsProvider;
import org.correomqtt.business.utils.ConnectionHolder;
import org.correomqtt.gui.business.MessageTaskFactory;
import org.correomqtt.gui.helper.ClipboardHelper;
import org.correomqtt.gui.model.SubscriptionPropertiesDTO;
import org.correomqtt.gui.model.WindowProperty;
import org.correomqtt.gui.model.WindowType;
import org.correomqtt.gui.utils.WindowHelper;
import org.correomqtt.plugins.systopic.model.SysTopic;
import org.correomqtt.plugins.systopic.model.SysTopicPropertiesDTO;
import org.correomqtt.plugins.systopic.model.SysTopicTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class SysTopicViewController implements SubscribeObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SysTopicViewController.class);
    private static final String SYS_TOPIC = "$SYS/#";
    private static final ResourceBundle resources = ResourceBundle.getBundle("org.correomqtt.plugins.systopic.i18n", SettingsProvider.getInstance().getSettings().getCurrentLocale());

    private final SubscriptionPropertiesDTO subscriptionDTO = SubscriptionPropertiesDTO.builder()
            .topic(SYS_TOPIC)
            .qos(Qos.AT_LEAST_ONCE)
            .hidden(true)
            .build();
    @FXML
    private Label connectionStatusLabel;
    @FXML
    private Label lastUpdateLabel;
    @FXML
    private ListView<SysTopicPropertiesDTO> listView;
    @FXML
    private Button copyToClipboardButton;

    private static String connectionId ;

    private ObservableList<SysTopicPropertiesDTO> sysTopics;

    private final Set<SysTopicPropertiesDTO> plainSysTopics = new HashSet<>();

    protected SysTopicViewController() { SubscribeDispatcher.getInstance().addObserver(this); }

    static void showAsDialog(final String connectionId) throws IOException {
        setConnectionId(connectionId);
        Map<Object, Object> properties = new HashMap<>();
        properties.put(WindowProperty.WINDOW_TYPE, WindowType.SYSTOPIC);
        properties.put(WindowProperty.CONNECTION_ID, connectionId);

        if (WindowHelper.focusWindowIfAlreadyThere(properties)) {
            return;
        }
        SysTopicViewController sysTopicViewController = new SysTopicViewController();
        FXMLLoader loader = new FXMLLoader(SysTopicViewController.class.getResource("/org/correomqtt/plugins/systopic/controller/sysTopicsView.fxml"), resources);
        loader.setController(sysTopicViewController);
        Parent parent= loader.load();

        showAsDialog((Pane) parent,
                resources.getString("sysTopicsViewControllerTitle") + " " + ConnectionHolder.getInstance().getConfig(connectionId).getName(),
                properties,
                event ->sysTopicViewController.onCloseDialog());
    }

    private void onCloseDialog() {
        MessageTaskFactory.unsubscribe(getConnectionId(), subscriptionDTO);
    }

    @FXML
    private void initialize() {

        ConnectionConfigDTO configDTO = ConnectionHolder.getInstance().getConfig(getConnectionId());
        connectionStatusLabel.setText(configDTO.getUrl() + ":" + configDTO.getPort());

        lastUpdateLabel.setText("Last Update: no update yet");
        sysTopics = FXCollections.observableArrayList(SysTopicPropertiesDTO.extractor());

        listView.setItems(sysTopics);
        listView.setCellFactory(this::createCell);

        MessageTaskFactory.subscribe(getConnectionId(), subscriptionDTO);
    }

    private ListCell<SysTopicPropertiesDTO> createCell(ListView<SysTopicPropertiesDTO> listView) {
        return new SysTopicCellController(listView);
    }

    @FXML
    void copyToClipboard() {
        ClipboardHelper.addToClipboard(
                plainSysTopics.stream()
                        .sorted(Comparator.comparingInt(st -> {
                            int sortIndex = SysTopic.getSortIndex(st.getTopic());
                            if (st.getSysTopic() != null && st.getSysTopic().isAggregated()) {
                                String parsedComponent = st.getSysTopic().parseComponent(st.getTopic());
                                return sortIndex * 20 + ((parsedComponent == null) ? 0 : Integer.parseInt(parsedComponent));
                            } else {
                                return sortIndex * 20;
                            }
                        }))
                        .map(st -> st.getTopic() + "\t" + st.getPayload())
                        .collect(Collectors.joining("\n"))
        );
    }

    @Override
    public void onMessageIncoming(MessageDTO messageDTO, SubscriptionDTO subscriptionDTO) {

        if (!messageDTO.getTopic().startsWith("$SYS")) {
            return;
        }
        Platform.runLater(() -> insertIncomingMessage(messageDTO));
    }

    private void insertIncomingMessage(MessageDTO messageDTO) {

        SysTopic sysTopic = SysTopic.getSysTopicByTopic(messageDTO.getTopic());

        SysTopicPropertiesDTO newSysTopicDTO = SysTopicTransformer.dtoToProps(messageDTO, sysTopic);

        plainSysTopics.remove(newSysTopicDTO);
        plainSysTopics.add(newSysTopicDTO);

        SysTopicPropertiesDTO sysTopicDTO = sysTopics.stream()
                .filter(st ->
                        sysTopic == null && st.getTopic().equals(messageDTO.getTopic()) ||
                                sysTopic == st.getSysTopic())
                .findFirst()
                .orElse(null);

        if (sysTopicDTO == null) {
            sysTopicDTO = newSysTopicDTO;
            sysTopics.add(sysTopicDTO);
            sysTopics.sort(Comparator.comparingInt(st -> SysTopic.getSortIndex(st.getTopic())));
        }

        if (sysTopic != null && sysTopic.isAggregated()) {
            String parsedComponent = sysTopic.parseComponent(messageDTO.getTopic());
            if ("1".equals(parsedComponent)) {
                sysTopicDTO.setMin1(messageDTO.getPayload());
            } else if ("5".equals(parsedComponent)) {
                sysTopicDTO.setMin5(messageDTO.getPayload());
            } else if ("15".equals(parsedComponent)) {
                sysTopicDTO.setMin15(messageDTO.getPayload());
            }
        } else {
            sysTopicDTO.setPayload(messageDTO.getPayload());
        }
        lastUpdateLabel.setText(resources.getString("sysTopicsViewUpdateLabel") + ": " + LocalDateTime.now(ZoneOffset.UTC).toString()); //format
    }

    @Override
    public void onSubscribedSucceeded(SubscriptionDTO subscriptionDTO) {
        if (SYS_TOPIC.equals(subscriptionDTO.getTopic())) {
            lastUpdateLabel.setText(resources.getString("sysTopicsViewControllerSubscriptionTo") + " " + subscriptionDTO.getTopic() + " " + resources.getString("sysTopicsViewControllerSucceeded"));
        }
    }

    @Override
    public void onSubscribedCanceled(SubscriptionDTO subscriptionDTO) {
        if (SYS_TOPIC.equals(subscriptionDTO.getTopic())) {
            lastUpdateLabel.setText(resources.getString("sysTopicsViewControllerSubscriptionTo") + " " + subscriptionDTO.getTopic() + " " + resources.getString("sysTopicsViewControllerCancelled"));
        }
    }

    @Override
    public void onSubscribedFailed(SubscriptionDTO subscriptionDTO, Throwable exception) {
        if (SYS_TOPIC.equals(subscriptionDTO.getTopic())) {
            lastUpdateLabel.setText(resources.getString("sysTopicsViewControllerSubscriptionTo") + " " + subscriptionDTO.getTopic() + " " + resources.getString("sysTopicsViewControllerFailed"));
        }
    }


    static <SysTopicViewController> void showAsDialog(Pane Pane, String title, Map<Object, Object> windowProperties, final EventHandler<WindowEvent> closeHandler) {

        String cssPath = SettingsProvider.getInstance().getCssPath();
        Scene scene = new Scene(Pane);
        if(cssPath != null) scene.getStylesheets().add(cssPath);
        Stage stage = new Stage();
        stage.setResizable(true);
        stage.setAlwaysOnTop(false);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
        if (closeHandler != null) {
            stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, closeHandler);
        }
        stage.getScene().getWindow().getProperties().putAll(windowProperties);
    }

    public static void setConnectionId(String connectionId) {
        SysTopicViewController.connectionId = connectionId;
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }
}

