package com.coding4people.mosquitoreport.api.controllers;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.coding4people.mosquitoreport.api.indexers.FocusIndexer;
import com.coding4people.mosquitoreport.api.models.Focus;
import com.coding4people.mosquitoreport.api.repositories.FocusRepository;

@Path("/focus")
public class FocusController {
    @Inject FocusIndexer focusIndexer;
    @Inject FocusRepository focusRepository;

    @POST
    @Path("/query")
    @Consumes("application/json")
    @Produces("application/json;charset=UTF-8")
    public Object query(@Valid FocusQueryInput input) {
        return focusIndexer.search(input.getLatlonnw(), input.getLatlonse());
    }
    
    @POST
    @Path("/query-center")
    @Consumes("application/json")
    @Produces("application/json;charset=UTF-8")
    public Object queryCenter(@Valid FocusCenterInput input) {
        return focusIndexer.searchCenter(input.getLatlon());
    }
    
    @GET
    @Path("{guid}")
    @Produces("application/json;charset=UTF-8")
    public Focus details(@PathParam("guid") String guid) {
        return focusRepository.loadOrNotFound(guid);
    }

    public static class FocusQueryInput {
        @NotNull
        private String latlonnw;
        
        @NotNull
        private String latlonse;

        public String getLatlonnw() {
            return latlonnw;
        }

        public void setLatlonnw(String latlonnw) {
            this.latlonnw = latlonnw;
        }

        public String getLatlonse() {
            return latlonse;
        }

        public void setLatlonse(String latlonse) {
            this.latlonse = latlonse;
        }
    }
    
    public static class FocusCenterInput {
        @NotNull
        private String latlon;

        public String getLatlon() {
            return latlon;
        }

        public void setLatlon(String latlon) {
            this.latlon = latlon;
        }
    }
}
