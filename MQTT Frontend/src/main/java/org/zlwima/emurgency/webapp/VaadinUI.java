package org.zlwima.emurgency.webapp;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WrappedSession;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.zlwima.emurgency.backend.model.EmrCaseData;
import org.zlwima.emurgency.backend.model.EmrLocation;
import org.zlwima.emurgency.backend.model.EmrVolunteer;
import org.zlwima.emurgency.webapp.Publisher.UIListener;

import static org.zlwima.emurgency.webapp.StartupServlet.CASES;
import static org.zlwima.emurgency.webapp.StartupServlet.PROFILER;
import static org.zlwima.emurgency.webapp.StartupServlet.PUBLISHER;

import org.zlwima.emurgency.webapp.gmap.Gmap;


@SuppressWarnings("serial")
@Theme("stylefix")
@PreserveOnRefresh
@Push(PushMode.AUTOMATIC)
public class VaadinUI extends UI implements ClickListener, UIListener {
    private static final Logger logger = Logger.getLogger(VaadinUI.class.getName());
	
	private boolean IS_ADMIN_INSTANCE = false;

    private VerticalSplitPanel splitGmapLayout;
    private TreeTable activeCaseTable;
    private TabSheet tabs;
    private Gmap gmap;

    private Window caseLocator;

    private Button caseButton;
    private WrappedSession userSession;

    private final TextField latitude = new TextField();
    private final TextField longitude = new TextField();
    private final TextField address = new TextField();
    private final TextField notes = new TextField();
    private final TextField targetId = new TextField();
    private final Label servertime = new Label();

    public void buttonClick(ClickEvent event) {
        if (event.getButton() == caseButton) {
            try {
                double lat = Double.parseDouble(latitude.getValue());
                double lon = Double.parseDouble(longitude.getValue());

                EmrCaseData newCaseData = new EmrCaseData(
                        userSession.getId(),
                        System.currentTimeMillis(),
                        address.getValue(),
                        notes.getValue(),
                        new EmrLocation(lat, lon));
				
                newCaseData.setCaseTimeOutValue(300000);
		
                if(targetId.getValue().equals("emradmin")){
                    CASES.add(newCaseData);
                    //adding users to caseData object
                    PROFILER.setVolunteersByRadius(newCaseData);
                    
                    //published to users in caseData object
                    PUBLISHER.broadcastCaseData(newCaseData, true);

                    addCaseToTable(newCaseData, true);

                    gmap.updateCase(newCaseData);
                }else if (!targetId.getValue().isEmpty()){
                    CASES.add(newCaseData);
                    
                    List<String> targets = Arrays.asList(targetId.getValue().replaceAll("\\s+","").split(",")); 
                    PROFILER.setVolunteersByTargets(newCaseData, targets);
                    
                    //published to users in caseData object
                    PUBLISHER.broadcastCaseData(newCaseData, true);

                    addCaseToTable(newCaseData, true);

                    gmap.updateCase(newCaseData);
                }
                
                
            } catch (Exception ex) {
                System.out.println("BUTTONCLICK EXCEPTION " + ex.getMessage());
            }
        }
    }

	@Override
    public void detach() {
		PUBLISHER.removeUIListener( this );		
        super.detach();
    }
	
    @Override
    protected void init(VaadinRequest request) {
        System.out.println("INIT CALLED [" + "] for instance: " + (this.toString()));
		
        userSession = VaadinSession.getCurrent().getSession();

		IS_ADMIN_INSTANCE = ( request.getParameter("admin") != null );
		System.out.println( userSession.getId() + " IS ADMIN INSTANCE: " + IS_ADMIN_INSTANCE );
		
		StartupServlet.checkForCaseTimeouts();
		
        gmap = new Gmap("map-canvas");
        gmap.setSizeFull();

        PUBLISHER.addUIListener(this);		
        
        splitGmapLayout = new VerticalSplitPanel();
        splitGmapLayout.setSizeFull();

        splitGmapLayout.setFirstComponent(gmap);
        splitGmapLayout.setSecondComponent(initActiveCaseTable());

        refreshSplitByTableSize();

        initWindow();

        // final tab creation
        //tabs = new TabSheet();
        //tabs.setSizeFull();
        //tabs.addTab(splitGmapLayout, "Case Initializer", null);
        
        MenuBar menubar = new MenuBar();
        menubar.setSizeFull();
        
        VerticalSplitPanel fullWindow = new VerticalSplitPanel();
        fullWindow.setSizeFull();
        fullWindow.setSplitPosition( 25.0f , Unit.PIXELS);
        fullWindow.setLocked(true);
        
        fullWindow.addComponent(menubar);
        fullWindow.addComponent(splitGmapLayout);

        menubar.addItem("Mongo DB Monitor", null, new MenuBar.Command(){
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                 getUI().getPage().open("http://emurgency.tk/mongoowl/", "_self");
            }
        });
        
        menubar.addItem("Mongo DB Admin", null, new MenuBar.Command(){
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                 getUI().getPage().open("http://137.226.188.142:50029", "_self");
            }
        });
        menubar.addItem("Tomcat Server", null, new MenuBar.Command(){
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                 getUI().getPage().open("http://tomcat.emurgency.tk/", "_self");
            }
        });
        
        //tabs.addTab(linkMonitor, "Mongo DB Monitor", null);
        //tabs.addTab(linkAdmin, "Mongo DB Admin", null);
        
        setContent(fullWindow);
        //setContent(tabs);

        caseLocator.setPositionX(80);
        caseLocator.setPositionY(115);
    }

    private void initWindow() {

        // Create the window
        caseLocator = new Window("Case Location Initializer");
        caseLocator.setClosable(false);
        caseLocator.setDraggable(true);
        caseLocator.setResizable(false);

        // Configure the windws layout; by default a VerticalLayout
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);

        FormLayout caseInitFormLayout = new FormLayout();
        caseInitFormLayout.setMargin(true);

        servertime.setImmediate(true);
        servertime.setWidth("100%");

        servertime.setValue("Servertime: " + new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis())));
        caseInitFormLayout.addComponent(servertime);

        latitude.setImmediate(true);
        latitude.setWidth("100%");
        latitude.setId("latitude");
        latitude.setCaption("Latitude");
        caseInitFormLayout.addComponent(latitude);

        longitude.setImmediate(true);
        longitude.setWidth("100%");
        longitude.setId("longitude");
        longitude.setCaption("Longitude");
        caseInitFormLayout.addComponent(longitude);

        address.setImmediate(true);
        address.setWidth("100%");
        address.setId("address");
        address.setCaption("Address");
        caseInitFormLayout.addComponent(address);

        notes.setImmediate(true);
        notes.setWidth("100%");
        notes.setId("notes");
        notes.setCaption("Notes");
        caseInitFormLayout.addComponent(notes);

        caseInitFormLayout.addComponent(new Label("<hr />", ContentMode.HTML));

        targetId.setImmediate(true);
        targetId.setWidth("100%");
        targetId.setId("init-id");
        targetId.setCaption("Initializer ID");
        caseInitFormLayout.addComponent(targetId);

        caseButton = new Button("Start New Case");
        caseButton.setImmediate(true);
        caseButton.addClickListener(this);
        caseInitFormLayout.addComponent(caseButton);

        layout.addComponent(caseInitFormLayout);

        caseLocator.setContent(layout);

        if (caseLocator.getParent() != null) {
            // window is already showing
        } else {
            // Open the subwindow by adding it to the parent window
            addWindow(caseLocator);
        }

    }

    private Table initActiveCaseTable() {
        activeCaseTable = new TreeTable("");
        activeCaseTable.setWidth("100%");
        activeCaseTable.setSelectable(true);

        activeCaseTable.addContainerProperty("Initializer", String.class, "UNDEFINED");		
        activeCaseTable.addContainerProperty("CaseId", String.class, "UNDEFINED");
        activeCaseTable.addContainerProperty("Address", String.class, "UNDEFINED");
        activeCaseTable.addContainerProperty("Volunteers", String.class, "-");
        activeCaseTable.addContainerProperty("Case Running-Time", Button.class, null);

        activeCaseTable.addItemSetChangeListener(new Container.ItemSetChangeListener() {
            public void containerItemSetChange(Container.ItemSetChangeEvent event) {
                refreshSplitByTableSize();
            }
        });

        activeCaseTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            public void itemClick(ItemClickEvent event) {
                if (event.getButton() == MouseButton.LEFT) {
                    String caseId = (String) event.getItem().getItemProperty("CaseId").getValue();
                    gmap.updateCase(PUBLISHER.getCaseByCaseId(caseId));
                }
            }
        }
        );

        for (EmrCaseData aCase : CASES) {
            addCaseToTable(aCase, false);
        }

        return activeCaseTable;
    }

    private void refreshSplitByTableSize() {
        int count = activeCaseTable.getItemIds().size() + 1;
        activeCaseTable.setPageLength(count);
        activeCaseTable.refreshRowCache();
        
        splitGmapLayout.setSplitPosition(20 + (count * 33), Sizeable.Unit.PIXELS, true);
    }

    private void addCaseToTable(EmrCaseData caseData, boolean select) {
		if( !IS_ADMIN_INSTANCE && !caseData.getCaseInitializerId().equals( userSession.getId() )) {
			return;
		}
		
        List<EmrVolunteer> volunteers = caseData.getVolunteers();
        List<String> volunteerEmails = new ArrayList<String>();
        for (EmrVolunteer volunteer : volunteers) {
            volunteerEmails.add(volunteer.getEmail());
        }

        Button clock = new Button();
        String timestamp = new SimpleDateFormat("mm:ss").format(new Date(caseData.getCaseRunningTimeMillis()));
        clock.setCaption(timestamp);

        Object[] caseObj = new Object[]{
			caseData.getCaseInitializerId(),
            caseData.getCaseId(),
            caseData.getCaseAddress(),
            volunteerEmails.toString(),
            clock
        };

        activeCaseTable.addItem(caseObj, caseData.getCaseId());
        if (select) {
            activeCaseTable.select(caseData.getCaseId());
        }
    }

    public void onRefresh(Publisher.CaseReply reply, EmrCaseData caseData) {
        System.out.println("-> VaadinUI.onRefresh() : " + caseData.getCaseId());
        boolean SELECTED = false;
        if (caseData.getCaseId().equals(activeCaseTable.getValue())) {
            SELECTED = true;
            gmap.updateCase(caseData);
        }

        if (reply.equals(Publisher.CaseReply.CLOSE_CASE)) {
            activeCaseTable.removeItem(caseData.getCaseId());
        } else {
            activeCaseTable.removeItem(caseData.getCaseId());
            addCaseToTable(caseData, SELECTED);
            push();
        }

    }

}
