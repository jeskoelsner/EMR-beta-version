package org.zlwima.emurgency.webapp.gmap;

import com.google.gson.Gson;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import org.zlwima.emurgency.backend.model.EmrCaseData;

@JavaScript({ 
    "https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.js",
    "gmap3.min.js", 
    "gmap.js", 
    "gmap_connector.js"
})
public final class Gmap extends AbstractJavaScriptComponent{
    
    public Gmap(String value) {
        getState().setValue(value);
    }
    
    public void updateCase(EmrCaseData caseData){
        String json = new Gson().toJson(caseData);
        getState().setCaseData(json);
    }

    public void setValue(String value) {
        getState().setValue(value);
    }

    @Override
    protected GmapState getState() {
        return (GmapState) super.getState();
    }
    
}
