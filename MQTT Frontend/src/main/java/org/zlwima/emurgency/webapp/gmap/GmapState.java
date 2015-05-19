package org.zlwima.emurgency.webapp.gmap;

import com.vaadin.shared.ui.JavaScriptComponentState;

public class GmapState extends JavaScriptComponentState {
    private String value;
    private String caseData;
    
    public void setCaseData(String caseData){
        this.caseData = caseData;
    }
    
    public String getCaseData(){
        return this.caseData;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
}
