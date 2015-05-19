window.org_zlwima_emurgency_webapp_gmap_Gmap = function() {
    
        var gmap = new Gmap(this.getElement());
            gmap.init();
    
        // Handle changes from the server-side
        this.onStateChange = function() {
            console.log("Statechange:");
            console.log(this.getState());
            
            var caseData = this.getState().caseData;

            if(caseData != null){
                gmap.updateCase(caseData);
            }
        };
    };